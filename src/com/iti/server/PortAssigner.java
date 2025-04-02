package com.iti.server;

import java.util.concurrent.ConcurrentLinkedQueue;

public class PortAssigner {
    private static final int START_PORT = 5000;  
    private static final int END_PORT = 5020;   
    private static ConcurrentLinkedQueue<Integer> availablePorts = new ConcurrentLinkedQueue<>();

    static {
        for (int i = START_PORT; i <= END_PORT; i++) {
            availablePorts.add(i);
        }
    }

    public static synchronized Integer getAvailablePort() {
        return availablePorts.poll();  
    }

    public static synchronized void releasePort(int port) {
        availablePorts.add(port);  
    }
}