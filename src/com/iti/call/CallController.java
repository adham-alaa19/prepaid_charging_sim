package com.iti.call;

import com.iti.audio.AudioHandler;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class CallController {
    private static final int BUFFER_SIZE = 4096;
    private  DatagramSocket socket;
    private  AudioHandler voiceHandler;
    private boolean active;
    
    
    public CallController()  {

    }
    
    public CallController(int port, AudioHandler voiceHandler) throws SocketException {
        this.socket = new DatagramSocket(port);
        this.voiceHandler = voiceHandler;
        this.active = false;
    }
    
    public void send(String targetIp, int targetPort) throws IOException {
        InetAddress targetAddress = InetAddress.getByName(targetIp);
        InputStream audioStream = voiceHandler.captureAudioStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        active = true;
        
        while (active && (bytesRead = audioStream.read(buffer)) != -1) {
            DatagramPacket packet = new DatagramPacket(buffer, bytesRead, targetAddress, targetPort);
            socket.send(packet);
        }
    }
    
    public void receive() throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        active = true;
        
        while (active) {
            socket.receive(packet);
            voiceHandler.playAudio(packet.getData());
        }
    }
    
    public void stop() {
        active = false;
        socket.close();
    }
}
