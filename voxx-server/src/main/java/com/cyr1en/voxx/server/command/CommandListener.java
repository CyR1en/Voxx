package com.cyr1en.voxx.server.command;

import com.cyr1en.voxx.server.VoxxServer;

import java.util.Scanner;

public class CommandListener implements Runnable {
    private volatile boolean running;
    private final VoxxServer voxxServer;
    private final Scanner in;

    public CommandListener(VoxxServer voxxServer) {
        this.voxxServer = voxxServer;
        in = new Scanner(System.in);
    }

    public void onCommand(String command) {
        if (command.equalsIgnoreCase("exit")) {
            running = false;
            in.close();
            voxxServer.close();
            System.exit(0);
        }
    }


    public void run() {
        running = true;
        while (running) {
            String command = in.nextLine();
            System.out.printf("\033[%dA", 1);
            System.out.print("\033[2K");
            VoxxServer.LOGGER.info("Command: " + command);
            onCommand(command);
        }
        in.close();
    }

    public void stop() {
        running = false;
    }
}
