package com.iti.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

import com.iti.call.CallController;

public class Mobile {
	
	private String MSISDN;
	private CallController caller;
	Socket socket = null;
	InputStreamReader inputReader = null;
	OutputStreamWriter outputWriter = null;
	BufferedReader bufferedReader = null;
	BufferedWriter bufferedWriter = null;
	
	public Mobile(String MSISDN) {
		this.MSISDN=MSISDN;
		caller = new CallController();
		}
	
	public void connectCall()
	 
	{

		try {

			socket = new Socket("localhost", 1234);
			bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

				bufferedWriter.write(MSISDN);
				bufferedWriter.newLine();
				bufferedWriter.flush();

				String status = bufferedReader.readLine();
				System.out.println("GOT CLient HE2323RE3225");

				if(status.equalsIgnoreCase("success"))
				{
					System.out.println("YAAAY CONNECTED");
				}
				else {
					System.out.println(status);

				}
				
			
		}
		catch (IOException E)
		{
			E.getMessage();
		}
		finally {
			try {
				socket.close();
		//		outputWriter.close();
		//		inputReader.close();
				bufferedReader.close();
				bufferedWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public double disconnectCall()
	{
		return 0.0;
	}

	public static void main(String[] args) {
		//String MSISDN=args[0];
		String MSISDN="01101126698";
		Mobile mobile = new Mobile(MSISDN);
		mobile.connectCall();
		mobile.disconnectCall();

	}

}
