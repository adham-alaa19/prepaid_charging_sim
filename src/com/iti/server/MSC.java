 package com.iti.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;

import com.iti.call.CallController;
import com.iti.models.CallDetailsRecord;

public class MSC {
    private CDR_Generator cdrg;
    private ServerSocket serverSocket;
    private CallController caller;
    private BufferedReader reader;
    private BufferedWriter writer;
    private CallDetailsRecord cdr;
    private String MSISDN;
    private volatile boolean callActive;
    private SDP sdp_server;
    private Socket clientSocket;

    public MSC(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.sdp_server = new SDP();
        this.callActive = false;
    }

    public void startServer() {
        System.out.println("Server is running ....");
        System.out.println("Waiting for voice call Signaling start message via TCP");

        try {
            while (!serverSocket.isClosed()) {
                clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            closeServerSocket();
        }
    }

    private void handleClient(Socket clientSocket) {
        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            
            MSISDN = reader.readLine();
            System.out.println("Received MSISDN: " + MSISDN);
            
            boolean exists = sdp_server.userExists(MSISDN);
            boolean enough = sdp_server.checkBalance(MSISDN);

            if (!exists) {
                CDR_Generator.writeCDR(new CallDetailsRecord(MSISDN, "Doesnt Exist"));
                writer.write("User MSISDN Not Found");
                writer.newLine();
                writer.flush();
            } else if (!enough) {
                CDR_Generator.writeCDR(new CallDetailsRecord(MSISDN, "Balance Not Enough"));
                writer.write("User Balance Not Enough To make a call");
                writer.newLine();
                writer.flush();
            } else {
                System.out.println("Accept Voice call start signaling message from MSISDN " + MSISDN);
                Integer port = PortAssigner.getAvailablePort();
                
                writer.write("success");
                writer.newLine();
                writer.write(port.toString());
                writer.newLine();
                writer.flush();
                
                cdr = new CallDetailsRecord(MSISDN);
                cdr.setBalanceBefore(sdp_server.getBalance(MSISDN));
                
                callActive = true;
                
                CountDownLatch callEndLatch = new CountDownLatch(1);
                
                Thread voiceThread = startVoiceCall(port);
                Thread receiveThread = startReceiveSignals(callEndLatch);
                Thread controlThread = startControlCall(callEndLatch);
                
                try {
                    callEndLatch.await();
                    
                    callActive = false;
                    
                    if (voiceThread.isAlive()) voiceThread.join(2000);
                    if (receiveThread.isAlive()) receiveThread.join(2000);
                    if (controlThread.isAlive()) controlThread.join(2000);
                    
                    endCall();
                } catch (InterruptedException e) {
                    System.err.println("Thread waiting interrupted: " + e.getMessage());
                    endCall();
                }
            }
        } catch (IOException e) {
            System.err.println("Client handling error: " + e.getMessage());
        } finally {
            try {
                if (reader != null) reader.close();
                if (writer != null) writer.close();
                if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
    }

    private Thread startVoiceCall(int port) throws IOException {
        caller = new CallController(port);
        Thread voiceThread = new Thread(() -> {
            try {
                System.out.println("Starting voice reception on port " + port);
                caller.receive();
            } catch (IOException e) {
                System.err.println("Voice reception error: " + e.getMessage());
            }
        });
        voiceThread.setName("VoiceThread");
        voiceThread.start();
        return voiceThread;
    }

    private Thread startReceiveSignals(CountDownLatch endLatch) {
        Thread receiveThread = new Thread(() -> {
            try {
                String message = "0";
                while (callActive && !(message != null && message.equalsIgnoreCase("e"))) {
                    if (reader.ready()) {
                        message = reader.readLine();
                        System.out.println("Received message: " + message);
                    }
                    
                    Thread.sleep(50);
                }
                
                if (callActive) {
                    cdr.setBillingStatus("Normal call Clearing");
                    endCall();
                    System.out.println("Call ended normally by client signal");
                    endLatch.countDown();

                }
            } catch (IOException e) {
                System.err.println("Signal reception error: " + e.getMessage());
                cdr.setBillingStatus("Error Occured Call Cut");
                endCall();
                endLatch.countDown();
            } catch (InterruptedException e) {
                System.err.println("Signal reception interrupted: " + e.getMessage());
            }
        });
        receiveThread.setName("ReceiveSignalsThread");
        receiveThread.start();
        return receiveThread;
    }

    private Thread startControlCall(CountDownLatch endLatch) {
        Thread controlThread = new Thread(() -> {
            try {
                int time = 0;
                boolean balanceFinished = false;
                
                while (callActive && !balanceFinished) {
                    if (!sdp_server.checkBalance(MSISDN)) {
                        balanceFinished = true;
                        break;
                    }
                    
                    Thread.sleep(60000);
                    
                    if (callActive) {
                        sdp_server.deductBalance(MSISDN);
                        time++;
                        
                        writer.write(time + " minutes elapsed");
                        writer.newLine();
                        writer.flush();
                        System.out.println("Sent to client: " + time + " minutes elapsed");
                    }
                }
                
                if (balanceFinished && callActive) {
                    writer.write(time + " your balance has Finished please recharge");
                    writer.newLine();
                    writer.write("end");
                    writer.newLine();
                    writer.flush();
                    cdr.setBillingStatus("balance got Finished");
                    System.out.println("Call ended due to insufficient balance");
                    endCall();
                    endLatch.countDown();
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Control thread error: " + e.getMessage());
                cdr.setBillingStatus("Error Occured Call Cut");
                endCall();
                endLatch.countDown();

            }
        });
        controlThread.setName("ControlThread");
        controlThread.start();
        return controlThread;
    }

    public synchronized void endCall() {
            if(callActive) {
            callActive = false;
            System.out.println("Call ending, generating CDR...");
            
            if (caller != null) {
                caller.stop();
            }
            
            if (cdr != null) {
                cdr.setBalanceAfter(sdp_server.getBalance(MSISDN));
                cdr.setCallEndTime(Instant.now());
                CDR_Generator.writeCDR(cdr);
                System.out.println("CDR generated for MSISDN: " + MSISDN);
            } 
        }
    }

    public static void main(String... args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(1234);
            MSC server = new MSC(serverSocket);
            server.startServer();
        } catch (IOException e) {
            System.err.println("Server startup error: " + e.getMessage());
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.err.println("Failed to close server socket: " + e.getMessage());
                }
            }
        }
    }
}