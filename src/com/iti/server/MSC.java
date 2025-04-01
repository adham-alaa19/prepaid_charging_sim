package com.iti.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.iti.models.CallDetailsRecord;


public class MSC {
	CDR_Generator  cdrg ;
	private ServerSocket serverSocket;
	private BufferedReader reader;
	private BufferedWriter writer;
	private SDP sdp_server ;

	public MSC(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
		sdp_server = new SDP();
	}

	public void startServer() {
		System.out.println("Server is running ....");
		try {
			while(!serverSocket.isClosed())
			{
				Socket clientSocket = serverSocket.accept();
				reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
				String MSISDN = reader.readLine();
				System.out.println(MSISDN);
				boolean exists = sdp_server.userExists(MSISDN); 
				boolean enough = sdp_server.checkBalance(MSISDN);

				if(!exists)
				{
					CDR_Generator.writeCDR(new CallDetailsRecord(MSISDN,"Doesnt Exist"));
					writer.write("User MSISDN Not Found");
					writer.newLine();
					writer.flush();
				}
				else if(!enough)
				{
					CDR_Generator.writeCDR(new CallDetailsRecord(MSISDN,"Balance Not Enough"));
					writer.write("User Balance Not Enough To make a call");
					writer.newLine();
					writer.flush();
				}
				else {
					System.out.println("Server success");
					writer.write("success");
					writer.newLine();
					writer.flush();
				}

			}
		} catch (IOException e)
		{
			System.err.println(e.getMessage());
		}
		finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}

		}
	}
	
	public void closeServerSocket() throws IOException {
		serverSocket.close();
	}
	
	
	public static void main(String ... args)
	{
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(1234);
			MSC server= new MSC(serverSocket);
			server.startServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
