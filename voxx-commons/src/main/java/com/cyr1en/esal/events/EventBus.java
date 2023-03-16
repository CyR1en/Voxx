package com.cyr1en.esal.events;

import com.cyr1en.esal.events.annotation.EventListener;

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
 * and make a function with the annotation {@link EventListener} and have its parameter
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
     * Subscribe all of the {@link EventListener} in the class.
     *
     * @param listener Object with functions annotated with {@link EventListener}
     */
    public void subscribeListeners(Listener listener) {
        iterateAnnotatedFunctions(listener, (clazz, method) -> {
            List<ListenerMethod> eventListeners = listenerMap.computeIfAbsent(clazz, k -> new ArrayList<>());
            eventListeners.add(new ListenerMethod(listener, method));
        });
    }

    /**
     * unsubscribe all of the {@link EventListener} in the class.
     *
     * @param listener Object with functions annotated with {@link EventListener}
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
     * the annotation {@link EventListener}
     * <p>
     * This uses reflection that looks at the declared methods in the class and filters methods
     * with the {@link EventListener} annotation. After that it checks if it exactly has one parameter
     * because we are only trying to have one event to listen to for this function. If that condition
     * is satisfied, it will accept the {@link BiConsumer} (as a callback function) passing the type of the
     * parameter and the {@link Method} itself.
     *
     * @param listener {@link Listener} object that we're going to iterate through
     * @param onVisit  the callback {@link BiConsumer} to accept.
     */
    private void iterateAnnotatedFunctions(Listener listener, BiConsumer<Class<?>, Method> onVisit) {
        var methods = listener.getClass().getDeclaredMethods();
        Arrays.stream(methods).filter(method -> method.isAnnotationPresent(EventListener.class))
                .forEach(filtered -> {
                    if (filtered.getParameterCount() > 1) return;
                    onVisit.accept(filtered.getParameterTypes()[0], filtered);
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
    public <T> void post(T event) {
        Class<?> eventType = event.getClass();
        var eventListeners = listenerMap.get(eventType);
        if (Objects.isNull(eventListeners)) return;

        var executor = executorServiceSupplier.get();
        var isThreadPool = executor instanceof ThreadPoolExecutor;
        for (var listener : eventListeners) {
            if (!isThreadPool) executor = executorServiceSupplier.get();
            executor.execute(() -> {
                try {
                    listener.invoke(event);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
            if (!isThreadPool) executor.shutdown();
        }
        if (isThreadPool) executor.shutdown();
    }


    public static class ListenerMethod {

        private final Listener listenerInstance;
        private final Method method;

        public ListenerMethod(Listener listenerInstance, Method method) {
            this.listenerInstance = listenerInstance;
            this.method = method;
        }

        public void invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
            method.invoke(listenerInstance, args);
        }
    }

    public interface Listener {
    }

    static class OnConnectionEvent {
        public void printHello(String message) {
            System.out.println("Hello! " + message);
        }
    }

    static class SomeListener implements Listener {

        @EventListener
        public void handle(OnConnectionEvent event) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            event.printHello("World");
        }

        @EventListener
        public void handle1(OnConnectionEvent event) {
            event.printHello("Boobs");
        }

        @EventListener
        public void handle2(OnConnectionEvent event) {
            event.printHello("Pogger");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        var eventBus = new EventBus();
        eventBus.setExecutorServiceSupplier(Executors::newCachedThreadPool);
        eventBus.subscribeListeners(new SomeListener());

        eventBus.post(new OnConnectionEvent());
        Thread.sleep(100);
        System.out.println("Lit");
        eventBus.post(new OnConnectionEvent());
    }

}
