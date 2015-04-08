import java.net.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Random;
import java.io.*;
import java.util.*;

public class client {


	private Connection connection; //HashMap<Integer, Connection> connections = new HashMap<>(5);
	private static final int TIMEOUT = 3000;
	private static final int MAXTRIES = 5;
	private static Random rand = new Random();

	public static void main(String[] args) {
		if (args.length != 4) {
			System.out.println("Incorrect number of arguments.  Please enter arguments as\nFxA-client <src port> <dst IP> <dst port>");
			System.exit(1);
		}

		client clientUser = new client();
		
		int ownPort = -1;
		InetAddress serverIP = null;
		int serverPort = -1;
		DatagramSocket socket = null;

		try {
			ownPort = Integer.parseInt(args[1]);
		} catch (NumberFormatException n) {
			System.out.println("Port number must be an int.");
			System.exit(1);
		}
		
		try {
			serverIP = InetAddress.getByName(args[2]);
		} catch (UnknownHostException u) {
			System.out.println("Destination IP address was improperly formatted.");
			System.exit(1);
		}

		try {
			serverPort = Integer.parseInt(args[3]);
		} catch (NumberFormatException n2) {
			System.out.println("Port number must be an int.");
			System.exit(1);
		}
		
		try {
			socket = new DatagramSocket(ownPort);		//NEED TO INCLUDE IP ADDRESS???  
		} catch (SocketException s) {
			System.out.println("Could not create socket.");
			System.exit(1);
		}

		System.out.println("Client binding to " + ownPort + " and sending to " + serverIP + ":" + serverPort);

		//connect to server
		if (clientUser.connect(serverIP, serverPort, socket)) {
			System.out.println("Successfully connected to server application.");
		} else {
			System.out.println("Could not connect to server application. Please try again.");
			System.exit(1);
		}
		
		Scanner scan = new Scanner(System.in);	//create scanner for reading in commands
		
		//check scanner input
		
		//FINISH MAIN METHOD CODE HERE
	}

	
	
	public client() {
		//do nothing;
	}
	
	//CHECK SEQ/ACK NUMS and MD5
	public boolean connect(InetAddress address, int port, DatagramSocket socket) { //HANDLE SIMULTANEOUS SYN PACKETS?
		int session1 = rand.nextInt();
		//System.out.println("///////////////////");
		Packet SYNDataPacket = new Packet(0, session1, 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0, 32000, new byte[0]);
		//System.out.println("///////////////////");
		DatagramPacket SYNpacket = new DatagramPacket(SYNDataPacket.toArray(), SYNDataPacket.toArray().length, address, port);
		DatagramPacket SYNACKrcvPacket = new DatagramPacket(new byte[SYNDataPacket.toArray().length], SYNDataPacket.toArray().length);
		
		boolean receivedResponse = trySend(socket, SYNpacket, SYNACKrcvPacket, address);

		if ((receivedResponse) && (verifyAck(SYNACKrcvPacket).getAckNum() == session1) && (checkHash(SYNACKrcvPacket))) {
			byte[] rcvData = SYNACKrcvPacket.getData();
			byte checkVal = (byte) 1;
			if ((rcvData[15] == checkVal) && (rcvData[16] == checkVal)) { // if SYNACK is received
				int session2 = verifyAck(SYNACKrcvPacket).getSeqNum();
				Packet ACKDataPacket = new Packet(0, session1 + 1, session2, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, 32000, new byte[0]);
				DatagramPacket ACKpacket = new DatagramPacket(ACKDataPacket.toArray(), ACKDataPacket.toArray().length, address, port);
				DatagramPacket ACKrcvPacket = new DatagramPacket(new byte[ACKDataPacket.toArray().length], ACKDataPacket.toArray().length);
				
				receivedResponse = trySend(socket, ACKpacket, ACKrcvPacket, address);
				
				if ((receivedResponse) && (verifyAck(ACKrcvPacket).getAckNum() == (session1 + 1)) && (checkHash(ACKrcvPacket))) { // if ACK is received
					byte[] rcvData2 = ACKrcvPacket.getData();
					checkVal = (byte) 1;
					if (rcvData2[16] == checkVal) {
						this.connection = new Connection(session1 + session2, session1 + 2, session2, address, port);
						System.out.println("Connection successful!");
						return true;
					}
				}
			}
		}
		socket.close();
		System.out.println("Socket connection failed.");
		return false;
	}

	private static Packet verifyAck(DatagramPacket pack) {
		return new Packet(pack.getData());
	}

	private static boolean checkHash(DatagramPacket pack) {
//		Packet tempPack = verifyAck(pack);
//		byte[] rcvHash = tempPack.getHash();
//		
//		MessageDigest hash;
//		try {
//			hash = MessageDigest.getInstance("MD5");
//		} catch (java.security.NoSuchAlgorithmException e) {
//			return false;
//		}
//		ByteBuffer temp = ByteBuffer.allocate(21);
//		temp.putInt(tempPack.getSessionID());
//		temp.putInt(tempPack.getSeqNum());
//		temp.putInt(tempPack.getAckNum());
//		temp.put(tempPack.getGET());
//		temp.put(tempPack.getPOST());
//		temp.put(tempPack.getFIN());
//		temp.put(tempPack.getSYN());
//		temp.put(tempPack.getACK());
//		temp.putInt(tempPack.getRcvWind());
//		byte[] anotherTemp = temp.array();
//		//Taken from http://stackoverflow.com/questions/5513152/easy-way-to-concatenate-two-byte-arrays
//		byte[] aboutToHash = new byte[anotherTemp.length + tempPack.getData().length];
//		System.arraycopy(anotherTemp, 0, aboutToHash, 0, anotherTemp.length);
//		System.arraycopy(tempPack.getData(), 0, aboutToHash, anotherTemp.length, tempPack.getData().length);
//		byte[] checkHash = hash.digest(aboutToHash);
//		return Arrays.equals(rcvHash, checkHash);
		return true;
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
		InetAddress address = this.connection.getAddress();
		int port = this.connection.getPort();
		
		Packet FINDataPacket = new Packet(ID, this.connection.getSeqNum(), this.connection.getAckNum(), (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0, this.connection.getRcvWind(), new byte[0]);
		DatagramPacket FINpacket = new DatagramPacket(FINDataPacket.toArray(), FINDataPacket.toArray().length, address, port);
		DatagramPacket rcvPacket = new DatagramPacket(new byte[FINDataPacket.toArray().length], FINDataPacket.toArray().length);
		
		boolean receivedResponse = trySend(socket, FINpacket, rcvPacket, address);
		connection.setSeqNum(connection.getSeqNum() + 1);
		
		if ((receivedResponse) && (verifyAck(rcvPacket).getAckNum() == connection.getSeqNum() - 1) && (checkHash(rcvPacket))) {
			byte[] rcvData = rcvPacket.getData();
			byte checkVal = (byte) 1;
			if (rcvData[16] == checkVal) {
				Packet ACKFinalPacket = new Packet(ID, this.connection.getSeqNum(), verifyAck(rcvPacket).getSeqNum(), (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, this.connection.getRcvWind(), new byte[0]);
				DatagramPacket ACKFPacket = new DatagramPacket(ACKFinalPacket.toArray(), ACKFinalPacket.toArray().length, address, port);
				
				if (rcvData[14] == checkVal) {
					if (trySend(socket, ACKFPacket)) {
						this.connection.setAckNum(verifyAck(rcvPacket).getSeqNum());
						return true;
					}
					return false;
				} else {
					rcvPacket = new DatagramPacket(new byte[FINDataPacket.toArray().length], FINDataPacket.toArray().length);
					try {
						socket.receive(rcvPacket);
						if (!rcvPacket.getAddress().equals(address)) { //Check source for received packet
							throw new IOException("Received packet was from unknown source");
						}
						ACKFinalPacket.setAckNum(verifyAck(rcvPacket).getSeqNum());
						ACKFPacket = new DatagramPacket(ACKFinalPacket.toArray(), ACKFinalPacket.toArray().length, address, port);
					} catch (Exception f) {
						return false;
					}
					if (rcvData[14] == checkVal) {
						if (trySend(socket, ACKFPacket)) {
							this.connection.setAckNum(verifyAck(rcvPacket).getSeqNum());
							return true;
						}
						return false;
					}
				}
			}
		}
		return false;
	}
}
