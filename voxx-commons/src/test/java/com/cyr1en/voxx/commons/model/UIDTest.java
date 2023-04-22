package com.cyr1en.voxx.commons.model;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class UIDTest {

    @Test
    public synchronized void testUnique() throws InterruptedException {
        var list = new ArrayList<UID>();

        for (int i = 0; i < 20; i++) {
            var exec = Executors.newSingleThreadExecutor();
            exec.execute(() -> {
                var uid = UID.Generator.generate();
                list.add(uid);
                exec.shutdown();
            });
            if (i % 3 == 0) Thread.sleep(1000);
        }

        // Allow the UID generation to finish
        Thread.sleep(1000);

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
}
