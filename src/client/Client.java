package client;

import java.lang.Runnable;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import java.util.Vector;

import message.Message;
import message.MessageTypes;
import message.MessageComparator;
import clock.VectorClock;
import queue.PriorityQueue;


public class Client {

	volatile VectorClock clock;

	public static void main(String[] args) {

		System.out.println();
		if (args.length != 4) {
			System.err.println("Invalid input. Args: userName clientPort serverAddress serverPort");
			System.exit(1);
		}

		String userName = args[0];

		InetAddress clientAddress = null;
		try {
			clientAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			System.err.println("Unknown client host");
			System.exit(1);
		}
		int clientPort = Integer.parseInt(args[1]);

		InetAddress serverAddress = null;
		try {
			serverAddress = InetAddress.getByName(args[2]);
		} catch (UnknownHostException e) {
			System.err.println("Invalid server host");
			System.exit(1);
		}
		int serverPort = Integer.parseInt(args[3]);

		VectorClock clock = new VectorClock();

		Message registerMessage = new Message(MessageTypes.REGISTER, userName, 0, clock, "");


		DatagramSocket tempSocket = null;
		try {
			tempSocket = new DatagramSocket();
		} catch (SocketException e) {
			System.err.println("Send Socket initialization error");
			System.exit(1);
		}

		final DatagramSocket socket = tempSocket;


		Message.sendMessage(registerMessage, socket, serverAddress, serverPort);

		Message registerReceipt = Message.receiveMessage(socket);

		System.out.println("==== FROM SERVER ====");
		System.out.println(registerReceipt.message);
		System.out.println();

		int pid = 0;

		if (registerReceipt.type == MessageTypes.ERROR) {
			System.exit(1);
		} else if (registerReceipt.type == MessageTypes.ACK) {
			System.out.println("Register receipt: " + registerReceipt.toString());
			pid = registerReceipt.pid;
			clock.setClock(registerReceipt.ts);
		} else {
			System.err.println("Server error");
			System.exit(1);
		}


		// ===========================================================


		// Kick off thread to handle receiving
		ListenRunnable runnable = new ListenRunnable(socket, clock);
		Thread listenThread = new Thread(runnable);
		listenThread.start();
		Scanner scanner = new Scanner(System.in);

		// Shutdown hook for socket and thread
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				socket.close();
				scanner.close();
				System.exit(0);
			}
		});

		while (true) {
			System.out.println("Message:");
			String text = scanner.nextLine();
			clock.tick(pid);
			Message message = new Message(MessageTypes.CHAT_MSG, userName, pid, clock, text);
			Message.sendMessage(message, socket, serverAddress, serverPort);
			System.out.println();

			if (Thread.interrupted())
				break;
		}
	}
}


class ListenRunnable implements Runnable {

	DatagramSocket socket; 
	VectorClock clock;


	public ListenRunnable(DatagramSocket socket, VectorClock clock) {
		this.socket = socket;
		this.clock = clock;
	}


	public void run() {
		try {
			System.out.println("Listening on " + InetAddress.getLocalHost() + ":" + this.socket.getLocalPort());
		} catch (UnknownHostException e) {
			System.out.println("Listening on unknown host");
		}
		PriorityQueue<Message> messageQueue = new PriorityQueue<>(new MessageComparator());

		try {
			while (!Thread.interrupted()) {
				Message message = Message.receiveMessage(this.socket);
				if (message == null)
					throw new SocketException("Interuppted");
				messageQueue.add(message);
				message = messageQueue.poll();
				clock.update(message.ts);

				System.out.println("\n==== FROM SERVER ====");
				System.out.println(message.sender + " " + message.ts.toString() + ": " + message.message);
				System.out.println();
				System.out.println("Message:");
			}
		} catch (SocketException e) {
			System.out.println("Exiting listener thread");
			return;
		}
	}
}