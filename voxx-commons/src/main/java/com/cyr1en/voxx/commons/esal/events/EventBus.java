package com.cyr1en.voxx.commons.esal.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * A simple multithreaded implementation of an event bus pattern.
 *
 * <p>
 * This implementation as mentioned above, is multithreaded, therefore it won't block
 * the main thread. Multithreading is handled through an {@link ExecutorService} that
 * we can set. By default, the executor service is set as a single thread executor.
 * However, if we chose to set a {@link ThreadPoolExecutor} instead, we can do so
 * and the {@link EventBus#post(Object)} function will adapt accordingly.
 * <p>
 * Usage of this map is very simple. Any object can be an event if we chose to do so.
 * To make a listener for that event, we can make a class that implements {@link Listener}
 * and make a function with the annotation {@link com.cyr1en.voxx.commons.esal.events.annotation.EventListener} and have its parameter
 * set as the event type we want to listen to. To invoke the listeners, we need to call
 * {@link EventBus#post(Object)} passing our instance of the event.
 */
public class EventBus {

    private final Map<Class<?>, List<ListenerMethod>> listenerMap;
    private Supplier<ExecutorService> executorServiceSupplier;

    /**
     * Zero argument constructor.
     * <p>
     * For optimal performance and minimal unexpected behavior, the listener map will
     * be set as a {@link ConcurrentHashMap} since we will be accessing this map in a
     * concurrent manner.
     */
    public EventBus() {
        listenerMap = new ConcurrentHashMap<>();
        executorServiceSupplier = Executors::newSingleThreadExecutor;
    }

    /**
     * Allows us to set the {@link Supplier} for our executor service.
     *
     * @param supplier Supplier for the {@link ExecutorService}
     */
    public void setExecutorServiceSupplier(Supplier<ExecutorService> supplier) {
        this.executorServiceSupplier = supplier;
    }

    /**
     * Subscribe all of the {@link com.cyr1en.voxx.commons.esal.events.annotation.EventListener} in the class.
     *
     * @param listener Object with functions annotated with {@link com.cyr1en.voxx.commons.esal.events.annotation.EventListener}
     */
    public void subscribeListeners(Listener listener) {
        iterateAnnotatedFunctions(listener, (clazz, method) -> {
            List<ListenerMethod> eventListeners = listenerMap.computeIfAbsent(clazz, k -> new ArrayList<>());
            eventListeners.add(new ListenerMethod(listener, method));
        });
    }

    /**
     * unsubscribe all of the {@link com.cyr1en.voxx.commons.esal.events.annotation.EventListener} in the class.
     *
     * @param listener Object with functions annotated with {@link com.cyr1en.voxx.commons.esal.events.annotation.EventListener}
     */
    public void unsubscribeListeners(Listener listener) {
        iterateAnnotatedFunctions(listener, (clazz, method) -> {
            List<ListenerMethod> eventListeners = listenerMap.get(clazz);
            if (eventListeners != null) {
                eventListeners.clear();
            }
        });

    }

    /**
     * A helper function to iterate through every function in the {@link Listener} object with
     * the annotation {@link com.cyr1en.voxx.commons.esal.events.annotation.EventListener}
     * <p>
     * This uses reflection that looks at the declared methods in the class and filters methods
     * with the {@link com.cyr1en.voxx.commons.esal.events.annotation.EventListener} annotation. After that it checks if it exactly has one parameter
     * because we are only trying to have one event to listen to for this function. If that condition
     * is satisfied, it will accept the {@link BiConsumer} (as a callback function) passing the type of the
     * parameter and the {@link Method} itself.
     *
     * @param listener {@link Listener} object that we're going to iterate through
     * @param onVisit  the callback {@link BiConsumer} to accept.
     */
    private void iterateAnnotatedFunctions(Listener listener, BiConsumer<Class<?>, Method> onVisit) {
        var methods = listener.getClass().getDeclaredMethods();
        Arrays.stream(methods).filter(method -> method.isAnnotationPresent(com.cyr1en.voxx.commons.esal.events.annotation.EventListener.class))
                .forEach(filtered -> {
                    if (filtered.getParameterCount() > 1) return;
                    onVisit.accept(filtered.getParameterTypes()[0], filtered);
                });
    }

    /**
     * Function that invokes all the listeners for the event T.
     * <p>
     * Overloaded function for post(T event, Runnable callback) that doesn't accept a {@link Runnable}
     *
     * @param event event to post.
     * @param <T>   The type of the event.
     */
    public <T> void post(T event) {
        post(event, () -> {
        });
    }

    /**
     * Function that invokes all the listeners for the event T.
     * <p>
     * This is a generic function that would accept any type T objects as a parameter
     * and see if the class for that type is registered in the listener map.
     * <p>
     * If the Supplier supplies a {@link ThreadPoolExecutor}, this function will take
     * advantage of that instead of making a new single-thread executor for every listener.
     * <p>
     * Since this invokes each listener across multiple threads, use `synchronized` for objects
     * that contains sensitive data or use {@link java.util.concurrent.atomic.AtomicReference}.
     *
     * @param event event to post.
     * @param <T>   The type of the event.
     */
    public <T> void post(T event, Runnable runAfter) {
        Class<?> eventType = event.getClass();
        var eventListeners = listenerMap.get(eventType);
        if (Objects.isNull(eventListeners)) return;

        var executor = executorServiceSupplier.get();
        if (executor instanceof ThreadPoolExecutor) {
            postThreadPool(event, runAfter, eventListeners);
        } else {
            postSingleThread(event, runAfter, eventListeners);
        }
    }

    /**
     * Function that invokes all the listeners for the event T.
     * <p>
     * This function assumes that the {@link ExecutorService} provided by the {@link Supplier} is a {@link ThreadPoolExecutor}.
     *
     * @param event          event to post.
     * @param runAfter       runnable to run after all the listeners are invoked.
     * @param eventListeners list of listeners to invoke.
     * @param <T>            The type of the event.
     */
    private <T> void postThreadPool(T event, Runnable runAfter, List<ListenerMethod> eventListeners) {
        var threadPool = (ThreadPoolExecutor) executorServiceSupplier.get();
        for (var listener : eventListeners) {
            threadPool.execute(() -> {
                try {
                    listener.invoke(event);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        threadPool.shutdown();
        runAfter.run();
    }

    /**
     * Function that invokes all the listeners for the event T.
     * <p>
     * This function assumes that the {@link ExecutorService} provided by the {@link Supplier} is a single-thread executor.
     *
     * @param event          event to post.
     * @param runAfter       runnable to run after all the listeners are invoked.
     * @param eventListeners list of listeners to invoke.
     * @param <T>            The type of the event.
     */
    private <T> void postSingleThread(T event, Runnable runAfter, List<ListenerMethod> eventListeners) {
        var executors = new ArrayList<ExecutorService>();
        for (var listener : eventListeners) {
            var executor = executorServiceSupplier.get();
            executors.add(executor);
            executor.execute(() -> invokeMethod(listener, event));
            executor.shutdown();
        }
        runAfterSingleThread(runAfter, executors);
    }

    /**
     * Function that makes sure that all single thread {@link ExecutorService} are shutdown before running the {@link Runnable}
     * runAfter.
     *
     * @param runAfter  runnable to run after all the listeners are invoked.
     * @param executors list of executors to shut down.
     */
    private void runAfterSingleThread(Runnable runAfter, List<ExecutorService> executors) {
        var runAfterExec = executorServiceSupplier.get();
        runAfterExec.execute(() -> {
            var notFinished = executors.stream().filter(e -> !e.isShutdown()).toList();
            try {
                for (var es : notFinished) {
                    es.shutdown();
                    var done = es.awaitTermination(1, TimeUnit.SECONDS);
                    if (!done) {
                        es.shutdownNow();
                        throw new RuntimeException("ExecutorService did not shutdown in time.");
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                runAfter.run();
            }
        });
    }

    /**
     * Function that invokes a {@link ListenerMethod} using the event T.
     * <p>
     * This is a utility function for both postSingleThread and postThreadPool.
     *
     * @param listener the listener method to invoke.
     * @param event    the event to pass to the listener method.
     * @param <T>      The type of the event.
     */
    private <T> void invokeMethod(ListenerMethod listener, T event) {
        try {
            listener.invoke(event);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A utility class that contains reference to the listener/event listener {@link Method
     * and the
     */
    public static class ListenerMethod {

        private final Listener listenerInstance;
        private final Method method;

        /**
         * Constructor, pass instance reference for the object that reflects the {@link Method}
         *
         * @param listenerInstance Instance of the method
         * @param method           reflected method from the instance
         */
        public ListenerMethod(Listener listenerInstance, Method method) {
            this.listenerInstance = listenerInstance;
            this.method = method;
        }

        /**
         * Reflectively invoke the {@link Method} that was passed in the constructor using
         * the matching {@link Listener} instance.
         * <p>
         *
         * @param args Parameters for the method.
         * @throws InvocationTargetException When the method could not be invoked due to improper param
         * @throws IllegalAccessException    When the method is not accessible through reflection without
         *                                   doing the necessary steps to unlock it.
         */
        public void invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
            method.invoke(listenerInstance, args);
        }
    }


    /**
     * A super basic listener that does not have any methods to implement. It's sole purpose is purely
     * semantics so that in code, it is clear that we're actually dealing with listeners for an event.¬
     */
    public interface Listener {
    }

}
