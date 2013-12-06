package com.example.rdcsc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class Client{
	private Socket socket;
	
	final private Activity mainActivity;
	
    private DataInputStream in;
    private DataOutputStream out;
	
	public Client(InetAddress serverAddress, Activity mainActivity) throws IOException{
		this.mainActivity = mainActivity;
		this.socket = new Socket(serverAddress, Constants.DATA_EXCHANGE_PORT);
		try {
        	in = new DataInputStream(socket.getInputStream());
        	Log.i(Constants.TAG,"Input stream connected");
        	out = new DataOutputStream(socket.getOutputStream());
			Log.i(Constants.TAG,"Output data stream connected");
        } catch (IOException e) {
        	Log.e(Constants.TAG, "Stream Creation Error", e);
        }
	}
	
	// return false if error occurred
	//
	public boolean sendMessage(String message){
		try {
			Log.i(Constants.TAG, "Sending message");
			out.writeUTF(message);
			out.flush();
			Log.i(Constants.TAG, "Sending message... Done");
		} catch (IOException e) {
			Log.e(Constants.TAG, "Write-to-Output-Stream Error", e);
			return false;
		}
		return true;
	}
	
	// close all connections
	public void close(){
		if(in != null){
			try {
				in.close();
			} catch (IOException e) {
	        	Log.e(Constants.TAG, "Couldn't close input stream", e);
			}
		}if(out != null){
			try {
				out.close();
			} catch (IOException e) {
	        	Log.e(Constants.TAG, "Couldn't close output stream", e);
			}
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				Log.e(Constants.TAG, "Couldn't close socket", e);
			}
		}
	}
}
