package com.cyr1en.voxx.commons.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * A class that represents a uniquely identifiable descriptor.
 * <p>
 * We can use this on any objects we want and still have it so that we are able to sort it (based on time)
 * if we choose to present it in an orderly matter or store it on a persistent data storage.
 */
public class UID {

    // An arbitrary date that I picked.
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
     * Get a {@link LocalDateTime} representation of the timestamp of this UID.
     *
     * @return LDT of the millis timestamp
     */
    public LocalDateTime getLDT() {
        var offSetMillis = getTimestamp();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(offSetMillis), ZoneId.systemDefault());
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
        var formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
        return String.format("UID: %d (%d)(%d) (ts: %s)", asLong(), timestamp, id, getLDT().format(formatter));
    }

    /**
     * A thread safe utility class to generate {@link UID} across multiple threads.
     * <p>
     * This should allow for when multiple threads requests to generate a UID on the same timestamp and
     * using the first 12 bits of the 54 bits that's used to store their incremental count.
     */
    public static class Generator {

        private static long lastGen;
        private static int incremental = 0;

        /**
         * A synchronized method that generates UID for absolute unique identifiable descriptor
         * <p>
         * Since time only moves forward and last 52 bits is set to that bits. We just need to check
         * if we're still generating within the same timestamp. If we are, we increment the incremental
         * part of the UID which is the first 12 bits. Allowing us to have up to 4095 (0xFFF) incremental
         * descriptors for a timestamp.
         * <p>
         * It's highly unlikely that we will punch through 4095 (0xFFF) incrementation with our application. Even
         * if 4095 clients connect to the server, not all of them will request a UID generation simultaneously.
         * <p>
         * Without the synchronized keyword, this function should still generate UIDs without collisions, but to be
         * thread-safe, I've decided to use synchronized.
         *
         * @return A generated {@link  UID}.
         */
        public synchronized static UID generate() {
            var timestamp = System.currentTimeMillis() - TIME_EPOCH;
            incremental = lastGen == timestamp ? incremental + 1 : 0;
            var uidLong = (timestamp << 12) | (0xFFF & incremental);
            var out = UID.of(uidLong);
            lastGen = timestamp;
            return out;
        }
    }

}
