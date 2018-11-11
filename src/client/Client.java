package client;

import java.lang.Runnable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import message.Message;
import message.MessageTypes;
import queue.PriorityQueue;

public class Client {
	public static void main(String[] args) {
		if (args.length != 4) {
			System.err.println("Invalid input. Args: userName clientPort serverAddress serverPort");
			System.exit(1);
		}

		int clientPort = Integer.parseInt(args[1]);
		InetAddress serverAddress = InetAddress.forString(args[2]);

		Message registerMessage = new Message(MessageTypes.REGISTER, args[0], 0, 0, "");
		DatagramSocket sendSocket = new DatagramSocket();

		Message.sendMessage(registerMessage, )
	}
}

class InputThread implements Runnable {
	public void run() {

	}
}

class ListenThread implements Runnable {
	public void run() {

	}
}