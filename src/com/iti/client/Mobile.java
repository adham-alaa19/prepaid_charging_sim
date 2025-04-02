 package com.iti.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import com.iti.call.CallController;

public class Mobile {
    private String MSISDN;
    private CallController caller;
    private Socket socket = null;
    private volatile boolean callActive = false;
    private InputStreamReader inputReader = null;
    private OutputStreamWriter outputWriter = null;
    private BufferedReader bufferedReader = null;
    private BufferedWriter bufferedWriter = null;

    public Mobile(String MSISDN) {
        this.MSISDN = MSISDN;
        try {
            caller = new CallController(1111);
        } catch (SocketException e) {
            System.err.println("Cannot set up caller module");
            e.printStackTrace();
        }
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Application shutting down. Terminating call.");
            if (callActive) {
                try {
                    if (bufferedWriter != null) {
                        bufferedWriter.write("e");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }
                    endCall();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    public void connectCall() {
        try {
            socket = new Socket("localhost", 1234);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            System.out.println("Starting voice call as MSISDN " + MSISDN);
            bufferedWriter.write(MSISDN);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            
            String status = bufferedReader.readLine();
            System.out.println("Received status: " + status);
            
            if (status.equalsIgnoreCase("success")) {
                int port = Integer.parseInt(bufferedReader.readLine());
                System.out.println("Connection Succeeded on port: " + port);
                System.out.println("Capturing Voice from Microphone and send via UDP…");
                
                // Set callActive before starting threads
                callActive = true;
                
                // Start threads in correct order
                Thread receiveThread = startReceiveControls();
                Thread voiceThread = startVoiceCall(port);
                Thread sendThread = startSendControls();
                
                // Wait for threads to complete
                try {
                    sendThread.join();
                    receiveThread.join();
                    voiceThread.join();
                } catch (InterruptedException e) {
                    System.err.println("Thread interrupted: " + e.getMessage());
                }
            } else {
                System.out.println(status);
            }
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
        } finally {
            cleanupResources();
        }
    }

    private Thread startVoiceCall(int port) {
        Thread voiceThread = new Thread(() -> {
            try {
                caller.send("localhost", port);
            } catch (IOException e) {
                System.err.println("Voice transmission error: " + e.getMessage());
            }
        });
        voiceThread.setName("VoiceThread");
        voiceThread.start();
        return voiceThread;
    }

    private Thread startReceiveControls() {
        Thread receiveThread = new Thread(() -> {
            try {
                String message = "";
                while (callActive && !message.equalsIgnoreCase("end")) {
                    if (bufferedReader.ready() || bufferedReader.markSupported()) {
                        message = bufferedReader.readLine();
                        if (message != null) {
                            System.out.println(message);
                        }
                    }
                    Thread.sleep(100);
                }
            } catch (IOException e) {
                System.err.println("Error receiving control messages: " + e.getMessage());
            } catch (InterruptedException e) {
                System.err.println("Receive thread interrupted: " + e.getMessage());
            } finally {
                endCall();
            }
        });
        receiveThread.setName("ReceiveControlsThread");
        receiveThread.start();
        return receiveThread;
    }

    private Thread startSendControls() {
        Scanner scan = new Scanner(System.in);
        Thread sendingThread = new Thread(() -> {
            try {
                String signal = "";
                while (callActive && !signal.equalsIgnoreCase("e")) {
                    signal = scan.nextLine();
                    if (callActive) {
                        bufferedWriter.write(signal);
                        bufferedWriter.newLine(); 
                        bufferedWriter.flush();
                    }
                }
            } catch (IOException e) {
                System.err.println("Error sending control messages: " + e.getMessage());
            } finally {
                endCall();
                scan.close();
            }
        });
        sendingThread.setName("SendControlsThread");
        sendingThread.start();
        return sendingThread;
    }

    public synchronized void endCall() {
        if (callActive) {
            System.out.println("Ending call...");
            callActive = false;
            if (caller != null) {
                caller.stop();
            }
        }
    }
    
    private void cleanupResources() {
        try {
            if (bufferedWriter != null) bufferedWriter.close();
            if (bufferedReader != null) bufferedReader.close();
            if (outputWriter != null) outputWriter.close();
            if (inputReader != null) inputReader.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }

    public double disconnectCall() {
        return 0.0;
    }

    public static void main(String[] args) {
        String MSISDN;
        if (args.length > 0) {
            MSISDN = args[0];
        } else {
            MSISDN = "01151126698"; 
            System.out.println("No MSISDN provided, using default: " + MSISDN);
        }
        
        Mobile mobile = new Mobile(MSISDN);
        mobile.connectCall();
    }
}