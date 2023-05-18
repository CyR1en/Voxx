package com.cyr1en.voxx.commons.model;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class UIDTest {

    private static UID uid;

    @BeforeAll
    public static void init() {
        uid = UID.Generator.generate();
    }

    @Test
    public synchronized void testUnique() throws InterruptedException {
        var list = new ArrayList<UID>();

        var threadPool = Executors.newCachedThreadPool();
        for (int i = 0; i < 20; i++) {
            threadPool.execute(() -> {
                var uid = UID.Generator.generate();
                list.add(uid);
            });
            if (i % 5 == 0) Thread.sleep(1000);
        }
        threadPool.shutdown();
        try {
            var isShutDown = threadPool.awaitTermination(1, java.util.concurrent.TimeUnit.MINUTES);
            if (!isShutDown) throw new RuntimeException("Thread pool did not shut down");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdownNow();
        }

        list.sort((o1, o2) -> {
            var diff = (int) (o1.getTimestamp() - o2.getTimestamp());
            return diff == 0 ? o1.getId() - o2.getId() : diff;
        });

        System.out.println("UID generated count: " + list.size());
        System.out.println();
        list.forEach(uid -> System.out.println(uid + " " + uid.getTimestampString()));

        var nonUnique = list.stream().filter(uid -> list.stream().filter(uid::equals).count() > 1).toList();
        nonUnique.forEach(uid -> System.out.println(uid + " is not unique"));
        Assertions.assertEquals(nonUnique.size(), 0);
    }

    @Test
    public void testStaticCreation() {
        var copy = UID.of(uid.asLong());
        Assertions.assertEquals(uid, copy);
    }
}
