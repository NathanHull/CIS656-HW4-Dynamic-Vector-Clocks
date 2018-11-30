package client;

import java.lang.Runnable;

import java.io.IOException;
import java.net.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Vector;

import org.json.JSONObject;

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

		
		// Registration with server
		Message.sendMessage(registerMessage, socket, serverAddress, serverPort);
		Message registerReceipt = Message.receiveMessage(socket);

		System.out.println("==== FROM SERVER ====");
		System.out.println(registerReceipt.message);
		System.out.println();

		int pid = 0;

		if (registerReceipt.type == MessageTypes.ERROR) {
			System.err.println("Error registering with server");
			System.exit(1);
		} else if (registerReceipt.type == MessageTypes.ACK) {
			pid = registerReceipt.pid;
			clock.setClockFromString("{\"" + pid + "\":0}");
		} else {
			System.err.println("Ambiguous server error");
			System.exit(1);
		}


		// ===========================================================


		// Kick off thread to handle receiving
		ListenRunnable runnable = new ListenRunnable(socket, clock);
		Thread listenThread = new Thread(runnable);
		listenThread.start();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		// Shutdown hook for socket and thread
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				socket.close();
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		});

		String text = "";
		System.out.println("Type quit to exit");
		while (true) {
			System.out.println("Message:");
			try {
				while (!br.ready()) {
					Thread.sleep(20);
				}
				text = br.readLine();
			} catch (IOException e) {
				System.err.println("Read error");
				continue;
			} catch (InterruptedException e) {
				break;
			}
			if (text.equals("quit"))
				System.exit(0);
			clock.tick(pid);
			Message message = new Message(MessageTypes.CHAT_MSG, userName, pid, clock, text);
			Message.sendMessage(message, socket, serverAddress, serverPort);
			System.out.println();
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
		PriorityQueue<Message> messageQueue = new PriorityQueue<>(new MessageComparator());

		try {
			while (true) {
				Message message = Message.receiveMessage(this.socket);
				if (message == null)
					throw new SocketException("Interrupted");
				messageQueue.add(message);
				message = messageQueue.peek();

				while (message != null) {
					// Queue means it's the smallest clock,
					// but needs to fulfill the other condition
					boolean isNext = false;

					// Is first message from that process
					if (clock.getTime(message.pid) == -1 && message.ts.getTime(message.pid) == 1) {
						isNext = true;
					}
					// Is next expected message from that process
					else if (message.ts.getTime(message.pid) == clock.getTime(message.pid) + 1) {
						isNext = true;
					}

					JSONObject jobject = new JSONObject(message.ts.toString());
					for (String key : jobject.keySet()) {
						if (!key.equals(message.pid)) {
							if (jobject.getInt(key) < clock.getTime(Integer.parseInt(key)))
								isNext = false;
						}
					}
					
					if (isNext) {
						clock.update(message.ts);

						System.out.println("\n==== FROM SERVER ====");
						System.out.println(message.sender + " " + message.ts.toString() + ": " + message.message);
						System.out.println();
						System.out.println("Message:");

						messageQueue.poll();
						message = messageQueue.peek();
					}
					else {
						message = null;
					}
				}
			}
		
		} catch (SocketException e) {
			System.out.println("Exiting listener thread");
			return;
		}
	}
}