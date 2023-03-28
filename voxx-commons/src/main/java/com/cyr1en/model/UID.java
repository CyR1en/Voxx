package com.cyr1en.model;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * A class that represents a uniquely identifiable descriptor.
 * <p>
 * We can use this on any objects we want and still have it so that we are able to sort it (based on time)
 * if we choose to present it in an orderly matter or store it on a persistent data storage.
 */
public class UID {

    // A random arbitrary date that I picked.
    // It's important that we change the epoch so that the IDs could also be unique beyond this application.
    public static long TIME_EPOCH = 0x64b62a60;

    // Make instance variable final so that they could not be changed.
    private final long timestamp;
    private final int id;

    /**
     * Private constructor so that we can actually construct it in this scope using
     * the {@link UID#of(long)} static function.
     * <p>
     * We don't want to allow users to just put arbitrary parameters to the constructor to make sure
     * that we actually have a unique identifiable descriptor. Therefore, this constructor is private
     *
     * @param timeStamp timestamp of the UID
     * @param id        incremental ID of the UID
     */
    private UID(long timeStamp, int id) {
        this.timestamp = timeStamp;
        this.id = id;
    }

    /**
     * Timestamp accessor
     *
     * @return timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Incremental ID accessor
     *
     * @return incremental id.
     */
    public int getId() {
        return id;
    }

    /**
     * Override equals function to allow equality tests for this class
     *
     * @param o Object to compare
     * @return return if this class equals o
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UID uid = (UID) o;
        return timestamp == uid.timestamp && id == uid.id;
    }

    /**
     * Override hasCode. This just uses {@link Objects#hash(Object...)} to has
     * the timestamp and id
     *
     * @return hash code for this class.
     */
    @Override
    public int hashCode() {
        return Objects.hash(timestamp, id);
    }

    /**
     * A method to parse a {@link Long} UID into an actual UID object.
     *
     * @param uid the uid to parse.
     * @return a parsed version of the long uid.
     */
    public static UID of(long uid) {
        var timestamp = (uid >> 12) + TIME_EPOCH;
        var id = uid & 0xFFF;
        return new UID(timestamp, (int) id);
    }

    /**
     * Gets the long version of this UID.
     *
     * @return long version of UID.
     */
    public long asLong() {
        return (timestamp << 12) | (0xFFF & id);
    }

    @Override
    public String toString() {
        return String.format("UID: %d (%d)(%d)", asLong(), timestamp, id);
    }

    public static void main(String[] args) throws InterruptedException {
        var list = new ArrayList<UID>();


        for (int i = 0; i < 50; i++) {
            var exec = Executors.newSingleThreadExecutor();
            exec.execute(() -> {
                var uid = UIDGenerator.generate();
                if (Objects.nonNull(uid)) list.add(uid);
                exec.shutdown();
            });
            if (i % 3 == 0) Thread.sleep(500);
        }

        list.sort((o1, o2) -> {
            var diff = (int) (o1.getTimestamp() - o2.getTimestamp());
            return diff == 0 ? o1.getId() - o2.getId() : diff;
        });

        Thread.sleep(1000);
        System.out.println("UID generated count: " + list.size());
        System.out.println();
        list.forEach(uid -> {
            System.out.println(uid);
            System.out.println("UID-TS: " + uid.getTimestamp());
            System.out.println("UID-ID: " + uid.getId());
            System.out.println("--------------------------------");
        });

        list.forEach(uid -> {
            if (list.stream().filter(uid::equals).count() > 1)
                System.out.println(uid + " is not unique");
            else
                System.out.println(uid + " is unique");
        });
    }

    /**
     * A thread safe utility class to generate UID across multiple threads.
     * <p>
     * This should allow for when multiple threads requests to generate a UID on the same timestamp and
     * using the first 12 bits of the 54 bits that's used to store their incremental count.
     */
    public static class UIDGenerator {

        private static final ConcurrentHashMap<Long, Integer> timestampTracker = new ConcurrentHashMap<>();

        /**
         * A non-synchronized method that a thread would call to allow for tracking of timestamps.
         *
         * @return A generated {@link  UID}.
         */
        @Nullable
        public synchronized static UID generate() {
            var timestamp = System.currentTimeMillis() - TIME_EPOCH;
            System.out.println("Generating with timestamp: " + timestamp);
            System.out.println("Thread: " + Thread.currentThread());
            synchronized (timestampTracker) {
                if (timestampTracker.containsKey(timestamp)) {
                    var curr = timestampTracker.get(timestamp);
                    timestampTracker.replace(timestamp, curr + 1);
                } else {
                    timestampTracker.put(timestamp, 0);
                }
            }
            return generateSync(timestamp);
        }

        /**
         * A synchronized way to generate {@link UID} to prevent making the same UID.
         *
         * @param timestamp provide the timestamp to generate the {@link UID} with.
         * @return a purely unique ID.
         */
        private static synchronized UID generateSync(long timestamp) {
            var count = timestampTracker.get(timestamp);
            if (Objects.isNull(count)) return null;
            System.out.println("Inc id: " + (0xFFF & count));
            var uidLong = (timestamp << 12) | (0xFFF & count);
            var uid = UID.of(uidLong);
            timestampTracker.computeIfPresent(timestamp, (k, v) -> v - 1);
            return uid;
        }
    }

}
