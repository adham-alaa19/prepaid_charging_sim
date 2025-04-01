 package com.iti.audio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class SampledAudioHandler implements AudioHandler {

    private final AudioFormat format;
    private AudioInputStream activeStream;

    public SampledAudioHandler(AudioFormat format) {
        this.format = format;
        this.activeStream = null;
    }
	
    @Override
    public byte[] captureAudio(int seconds) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine line = null;
        
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            byte[] buffer = new byte[4096];
            long endTime = System.currentTimeMillis() + (seconds * 1000);
            
            while (System.currentTimeMillis() < endTime) {
                int bytesRead = line.read(buffer, 0, buffer.length);
                out.write(buffer, 0, bytesRead);
            }
        } catch (LineUnavailableException e) {
            throw new IOException("Microphone unavailable", e);
        } finally {
            if (line != null) {
                line.stop();
                line.close();
            }
            out.close();
        }
        return out.toByteArray(); 
    }

    @Override
    public void playAudio(byte[] audioData) throws IOException {
        if (audioData == null || audioData.length == 0) {
            throw new IOException("No recording found.");
        }
        
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        try (SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(info)) {
            speaker.open(format);
            speaker.start();
            speaker.write(audioData, 0, audioData.length);
            speaker.drain();
        } catch (LineUnavailableException e) {
            throw new IOException("Speaker unavailable", e);
        }
    }

    @Override
    public InputStream captureAudioStream() throws IOException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine line;
        
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            activeStream = new AudioInputStream(line);
        } catch (LineUnavailableException e) {
            throw new IOException("Microphone unavailable", e);
        }
        
        return activeStream;  
    }
    @Override
    public void closeAudioStream() {
        if (activeStream != null) {
            try {
				activeStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
     }
    }
}
