import java.net.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.io.*;

public class server {
	
	private Connection connection; //HashMap<Integer, Connection> connections = new HashMap<>(5);			//THIS MAY NEED TO BE 1 CONNECTION!!!
	//private static short rcvWind;	
	private static final int TIMEOUT = 3000;
	private static final int MAXTRIES = 5;
	private static Random rand = new Random();

	public static void main(String[] args) {
		if (args.length != 4) {
			System.out.println("Incorrect number of arguments.  Please enter arguments as\nFxA-server <src port> <dst IP> <dst port>");
			System.exit(1);
		}

		server serverUser = new server();
		
		int ownPort = -1;
		InetAddress clientIP = null;
		int clientPort = -1;
		DatagramSocket socket = null;

		try {
			ownPort = Integer.parseInt(args[1]);
		} catch (NumberFormatException n) {
			System.out.println("Port number must be an int.");
			System.exit(1);
		}
		
		try {
			clientIP = InetAddress.getByName(args[2]);
		} catch (UnknownHostException u) {
			System.out.println("Destination IP address was improperly formatted.");
			System.exit(1);
		}

		try {
			clientPort = Integer.parseInt(args[3]);
		} catch (NumberFormatException n2) {
			System.out.println("Port number must be an int.");
			System.exit(1);
		}
		
		try {
			socket = new DatagramSocket(ownPort);		//NEED TO INCLUDE IP ADDRESS???  
		} catch (SocketException s) {
			System.out.println("Couldn not create socket.");
			System.exit(1);
		}

		System.out.println("Server binding to " + ownPort + " and sending to " + clientIP + ":" + clientPort);
		
		//connect to client
		if (serverUser.connect(clientIP, clientPort, socket)) {
			System.out.println("Successfully connected to client application.");
		} else {
			System.out.println("Could not connect to client application. Please try again.");
			System.exit(1);
		}
		
		Scanner scan = new Scanner(System.in);	//create scanner for reading in commands
		
		//check scanner input
		
		//FINISH MAIN METHOD CODE HERE
	}


	public server() {
		//do nothing
	}
	
	public boolean connect(InetAddress address, int port, DatagramSocket socket){
		int session2 = rand.nextInt();
		Packet SYNACKDataPacket = new Packet(0, session2, 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 1, 32000, new byte[0]);
		DatagramPacket SYNrcvPacket = new DatagramPacket(new byte[SYNACKDataPacket.toArray().length], SYNACKDataPacket.toArray().length);
		
		boolean receivedResponse = tryReceive(socket, SYNrcvPacket, address);
		
		if ((receivedResponse) && (verifyAck(SYNrcvPacket).getAckNum() == 0) && (checkHash(SYNrcvPacket))) { 
			byte[] rcvData = SYNrcvPacket.getData();
			byte checkVal = (byte) 1;
			if (rcvData[15] == checkVal) { // if SYN is received
				int session1 = verifyAck(SYNrcvPacket).getSeqNum();
				SYNACKDataPacket.setAckNum(session1);
				DatagramPacket SYNACKPacket = new DatagramPacket(SYNACKDataPacket.toArray(), SYNACKDataPacket.toArray().length, address, port);
				DatagramPacket ACKrcvPacket = new DatagramPacket(new byte[SYNACKDataPacket.toArray().length], SYNACKDataPacket.toArray().length);
				
				receivedResponse = trySend(socket, SYNACKPacket, ACKrcvPacket, address);
				if ((receivedResponse) && (verifyAck(ACKrcvPacket).getAckNum() == session2) && (checkHash(ACKrcvPacket))) { // if ACK is received
					byte[] rcvData2 = ACKrcvPacket.getData();
					checkVal = (byte) 1;
					if(rcvData2[16] == checkVal){
						Packet ACKDataPacket2 = new Packet(0, session2 + 1, verifyAck(ACKrcvPacket).getSeqNum(), (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, 32000, new byte[0]);
						DatagramPacket ACKPacket = new DatagramPacket(ACKDataPacket2.toArray(), ACKDataPacket2.toArray().length, address, port);
						if (trySend(socket, ACKPacket)) {
							this.connection = new Connection(session1 + session2, session2 + 2, session1 + 1, address, port);
							System.out.println("Connection successful!");
							return true;
						}
					}
				}
			}
		}
		socket.close();
		System.out.println("Socket connection failed!");
		return false;
	}

	private static Packet verifyAck(DatagramPacket pack) {
		return new Packet(pack.getData());
	}
	
	private static boolean checkHash(DatagramPacket pack) {
		Packet tempPack = verifyAck(pack);
		byte[] rcvHash = tempPack.getHash();
		
		MessageDigest hash;
		try {
			hash = MessageDigest.getInstance("MD5");
		} catch (java.security.NoSuchAlgorithmException e) {
			return false;
		}
		ByteBuffer temp = ByteBuffer.allocate(21);
		temp.putInt(tempPack.getSessionID());
		temp.putInt(tempPack.getSeqNum());
		temp.putInt(tempPack.getAckNum());
		temp.put(tempPack.getGET());
		temp.put(tempPack.getPOST());
		temp.put(tempPack.getFIN());
		temp.put(tempPack.getSYN());
		temp.put(tempPack.getACK());
		temp.putInt(tempPack.getRcvWind());
		byte[] anotherTemp = temp.array();
		//Taken from http://stackoverflow.com/questions/5513152/easy-way-to-concatenate-two-byte-arrays
		byte[] aboutToHash = new byte[anotherTemp.length + tempPack.getData().length];
		System.arraycopy(anotherTemp, 0, aboutToHash, 0, anotherTemp.length);
		System.arraycopy(tempPack.getData(), 0, aboutToHash, anotherTemp.length, tempPack.getData().length);
		byte[] checkHash = hash.digest(aboutToHash);
		return Arrays.equals(rcvHash, checkHash);
	}
	
	private static boolean tryReceive(DatagramSocket socket, DatagramPacket rcvP, InetAddress address) {
		try {
			socket.receive(rcvP);
			if (!rcvP.getAddress().equals(address)) { //Check source for received packet
				throw new IOException("Received packet was from unknown source");
			}
			return true;
		} catch (Exception f) {
			System.out.println("There was a problem receiving: " + f);
			return false;
		}
	}
	
	private static boolean trySend(DatagramSocket socket, DatagramPacket sendP, DatagramPacket rcvP, InetAddress address) {
		boolean receivedResponse = false;
		int tries = 0;
		do {
			try {
				socket.setSoTimeout(TIMEOUT);
				socket.send(sendP);
				socket.receive(rcvP);
				if (!rcvP.getAddress().equals(address)) { //Check source for received packet
					throw new IOException("Received packet was from unknown source");
				}
				receivedResponse = true;
			} catch (InterruptedIOException e) {
				tries += 1;
				System.out.println("Timed out, " + (MAXTRIES - tries) + " more tries.");
			} catch (Exception f) {
				return false;
			}
		} while ((!receivedResponse) && (tries < MAXTRIES));
		return receivedResponse;
	}
	
	private static boolean trySend(DatagramSocket socket, DatagramPacket sendP) {
		boolean receivedResponse = false;
		int tries = 0;
		do {
			try {
				socket.setSoTimeout(TIMEOUT);
				socket.send(sendP);
				receivedResponse = true;
			} catch (InterruptedIOException e) {
				tries += 1;
				System.out.println("Timed out, " + (MAXTRIES - tries) + " more tries.");
			} catch (Exception f) {
				System.out.println(f);
				return false;
			}
		} while ((!receivedResponse) && (tries < MAXTRIES));
		return receivedResponse;
	}
	
	public int send(){
		return 0;
	}
	
	public int receive(){
		return 0;
	}
	
	//CHECK SEQ/ACK NUMS and MD5
		public boolean close(int ID, DatagramSocket socket) {	
			InetAddress address = connection.getAddress();
			int port = connection.getPort();
			
			Packet FINDataPacket = new Packet(ID, connection.getSeqNum(), connection.getAckNum(), (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0, connection.getRcvWind(), new byte[0]);
			DatagramPacket FINpacket = new DatagramPacket(FINDataPacket.toArray(), FINDataPacket.toArray().length, address, port);
			DatagramPacket rcvPacket = new DatagramPacket(new byte[FINDataPacket.toArray().length], FINDataPacket.toArray().length);
			
			boolean receivedResponse = trySend(socket, FINpacket, rcvPacket, address);
			
			if (receivedResponse) {
				byte[] rcvData = rcvPacket.getData();
				byte checkVal = (byte) 1;
				if (rcvData[16] == checkVal) {
					if (rcvData[14] == checkVal) {
						//send final ack
						return true;
					} else {
						rcvPacket = new DatagramPacket(new byte[FINDataPacket.toArray().length], FINDataPacket.toArray().length);
						try {
							socket.receive(rcvPacket);
							if (!rcvPacket.getAddress().equals(address)) { //Check source for received packet
								throw new IOException("Received packet was from unknown source");
							}
						} catch (Exception f) {
							return false;
						}
						if (rcvData[14] == checkVal) {
							//send final ack
							return true;
						}
					}
				}
			}
			return false;
		}
}
