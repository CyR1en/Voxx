package com.cyr1en.voxx.server.command;

import com.cyr1en.voxx.server.VoxxServer;

import java.util.Scanner;

public class CommandListener implements Runnable {
    private volatile boolean running;
    private final VoxxServer voxxServer;

    public CommandListener(VoxxServer voxxServer) {
        this.voxxServer = voxxServer;
    }

    public void onCommand(String command) {
        if (command.equalsIgnoreCase("exit")) {
            running = false;
            voxxServer.close();
        }
    }


    public void run() {
        running = true;
        var scanner = new Scanner(System.in);

        while (running) {
            String command = scanner.nextLine();
            System.out.printf("\033[%dA", 1);
            System.out.print("\033[2K");
            VoxxServer.LOGGER.info("Command: " + command);
            onCommand(command);
        }

        scanner.close();
    }

    public void stop() {
        running = false;
    }
}
