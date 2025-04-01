package com.iti.audio;

import java.io.IOException;
import java.io.InputStream;

//import java.io.ByteArrayOutputStream;

public interface AudioHandler {
	byte [] captureAudio(int seconds) throws IOException;
	void  playAudio(byte audioData []) throws IOException;
    InputStream captureAudioStream() throws IOException;
    void closeAudioStream();
}
