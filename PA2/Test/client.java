import java.net.*;
import java.nio.*;
import java.nio.file.*;
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
	private static int fileCount = 0;

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
		
		////DELETE THIS LATER//////////
		if (clientUser.send((byte) 1, "test.pdf", socket)) {
			System.out.println("Successful send");
		} else {
			System.out.println("Failed to send");
		}
		/////////////////////////////
		
		/////DELETE THIS LATER//////
		for(int i = 0; i < 1000; i++) {
			System.out.print("");
		}
		if (clientUser.close(clientUser.connection.getSessionID(), socket)) {
			System.out.println("Successfully close with server application.");
		} else {
			System.out.println("Could not close with server application.");
			System.exit(1);
		}
		//////////////////////////
		
		Scanner scan = new Scanner(System.in);	//create scanner for reading in commands
		
		//check scanner input
		
		//FINISH MAIN METHOD CODE HERE
		
		/*
		 * while loop until close (Then go to idle situation to await connection) {
		 * 		check and interpret scanner input
		 * 		make a non-blocking receive call to check for incoming
		 * 		catch block for timeouts (not needed with SNW))
		 * }
		 */
	}

	
	
	public client() {
		//do nothing;
	}
	
	//CHECK SEQ/ACK NUMS and MD5
	public boolean connect(InetAddress address, int port, DatagramSocket socket) { //HANDLE SIMULTANEOUS SYN PACKETS?
		int session1 = rand.nextInt();
		//System.out.println("///////////////////");
		Packet SYNDataPacket = new Packet(0, session1, 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0, 32000, new byte[0], 0);
		//System.out.println("///////////////////");
		DatagramPacket SYNpacket = new DatagramPacket(SYNDataPacket.toArray(), SYNDataPacket.toArray().length, address, port);
		DatagramPacket SYNACKrcvPacket = new DatagramPacket(new byte[SYNDataPacket.toArray().length], SYNDataPacket.toArray().length);
		
		boolean receivedResponse = trySend(socket, SYNpacket, SYNACKrcvPacket, address);

		if ((receivedResponse) && (verifyAck(SYNACKrcvPacket).getAckNum() == session1) && (checkHash(SYNACKrcvPacket))) {
			byte[] rcvData = SYNACKrcvPacket.getData();
			byte checkVal = (byte) 1;
			if ((rcvData[15] == checkVal) && (rcvData[16] == checkVal)) { // if SYNACK is received
				int session2 = verifyAck(SYNACKrcvPacket).getSeqNum();
				Packet ACKDataPacket = new Packet(0, session1 + 1, session2, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, 32000, new byte[0], 0);
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
//		ByteBuffer temp = ByteBuffer.allocate(25);
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
	
	private static boolean tryReceive(DatagramSocket socket, DatagramPacket rcvP, InetAddress address) {
		try {
			socket.receive(rcvP);
			if (!rcvP.getAddress().equals(address)) { //Check source for received packet
				throw new IOException("Received packet was from unknown source");
			}
			return true;
		} catch (Exception f) {
			//System.out.println("There was a problem receiving: " + f);
			//return false;
			return tryReceive(socket, rcvP, address);
		}
	}

	private static boolean trySend(DatagramSocket socket, DatagramPacket sendP, InetAddress address, Connection connect, String filename) {
		DatagramPacket rcvP = new DatagramPacket(new byte[Packet.MAXPACKETSIZE], Packet.MAXPACKETSIZE);
		int lastAck = 0;
		int lastSeq = 0;
		boolean receivedResponse = false;
		int tries = 0;
		do {
			//System.out.println("TEST4");
			try {
				socket.setSoTimeout(TIMEOUT);
				//System.out.println("TESTa");
				trySend(socket, sendP, rcvP, address);
				//System.out.println("TESTb");
				if (!rcvP.getAddress().equals(address)) { //Check source for received packet
					throw new IOException("Received packet was from unknown source");
				}
				//System.out.println("TESTc");
				if (verifyAck(rcvP).getACK() != 1 || verifyAck(rcvP).getPOST() != 1) { // send GET request again
					//System.out.println("TESTd");
					if ((verifyAck(rcvP).getGET() == 0) && (verifyAck(rcvP).getPOST() == 0) && (verifyAck(rcvP).getSYN() == 0) && (verifyAck(rcvP).getFIN() == 0) && (verifyAck(rcvP).getACK() == 0)) {
						System.out.println("File does not exist on the server.");
						return false;	//file does not exist
					}
					while (verifyAck(rcvP).getACK() != 1 || verifyAck(rcvP).getPOST() != 1) {
						if ((verifyAck(rcvP).getGET() == 0) && (verifyAck(rcvP).getPOST() == 0) && (verifyAck(rcvP).getSYN() == 0) && (verifyAck(rcvP).getFIN() == 0) && (verifyAck(rcvP).getACK() == 0)) {
							System.out.println("File does not exist on the server.");
							return false;	//file does not exist
						}
						//System.out.println("TESTe");
						trySend(socket, sendP, rcvP, address);
						//System.out.println("TESTf");
					}
				}
				//System.out.println("TESTg");
				int dsz = verifyAck(rcvP).getDataSize();
				System.out.println("DATA SIZE = " + dsz);
				byte[] data = new byte[dsz];
//				System.out.println("DATA = " + data);
//				System.out.println("Seq: " + verifyAck(rcvP).getSeqNum());
//				System.out.println("SID: " + verifyAck(rcvP).getSessionID());
//				System.out.println("POST: " + verifyAck(rcvP).getPOST());
//				System.out.println("ACK: " + verifyAck(rcvP).getACK());
				System.arraycopy(verifyAck(rcvP).getData(), 0, data, 0, dsz);
//				////
//				System.out.println("BYTE ARRAY");
//				for (int q = 0; q < dsz; q++) {
//					System.out.print(data[q]);
//				}
//				System.out.println();
//				////
//				System.out.println("DATA2 = " + data);
//				System.out.println("TEST345678");
				connect.addData(data);			//CHECK ON LENGTH!!!!!!!!!!!!???????????
				writeDataToFile(filename, connect.getData(), connect);
				//System.out.println("TESTj");
				lastAck = verifyAck(rcvP).getSeqNum(); // seq # of the first data packet
				lastSeq = verifyAck(rcvP).getAckNum(); // 
				//System.out.println("TESTk");
				
				Packet ACKDataPacket = new Packet(verifyAck(sendP).getSessionID(), lastSeq++, lastAck, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, connect.getRcvWind(), new byte[0], 0);
				DatagramPacket ACKpacket = new DatagramPacket(ACKDataPacket.toArray(), ACKDataPacket.toArray().length, address, connect.getPort());
				trySend(socket, ACKpacket);			//WHAT HAPPENS IF THIS DOES NOT ARRIVE???
				//System.out.println("TESTm");
				
				receivedResponse = true;
				//System.out.println("TESTh");
			} catch (InterruptedIOException e) {
				tries += 1;
				//System.out.println("TEST5");
				System.out.println("Timed out, " + (MAXTRIES - tries) + " more tries.");
			} catch (Exception f) {
				//System.out.println("TEST7 " + f);
				return false;
			}
		} while ((!receivedResponse) && (tries < MAXTRIES));
		//System.out.println("TEST6");
		if (tries >= MAXTRIES) return false;
		Packet rcv = null;
		do {
			//System.out.println("TEST8");
			if (tryReceive(socket, rcvP, address)) {
				rcv = verifyAck(rcvP);
				if ((rcv.getGET() == 0) && (rcv.getPOST() == 0) && (rcv.getSYN() == 0) && (rcv.getFIN() == 0) && (rcv.getACK() == 0)) {
					Packet ACKFDataPacket = new Packet(verifyAck(sendP).getSessionID(), lastSeq++, (lastAck = rcv.getSeqNum()), (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, connect.getRcvWind(), new byte[0], 0);
					DatagramPacket ACKFpacket = new DatagramPacket(ACKFDataPacket.toArray(), ACKFDataPacket.toArray().length, address, connect.getPort());
					trySend(socket, ACKFpacket);
					connect.setAckNum(lastAck);				//CHECK ON SETTING THIS AT OTHER RETURNS
					connect.setSeqNum(lastSeq + 1);
					fileCount = 0;
					//System.out.println("TEST9");
					return true;
				} else {
					int dsz = verifyAck(rcvP).getDataSize();
					byte[] data = new byte[dsz];
					System.arraycopy(verifyAck(rcvP).getData(), 0, data, 0, dsz);
					DatagramPacket ACKPacket = null;
					if ((verifyAck(rcvP).getACK() != 1) || (verifyAck(rcvP).getPOST() != 1) || (verifyAck(rcvP).getSeqNum() != lastAck + 1)) { // re-ACK 
						Packet ACKDataPacket = new Packet(verifyAck(rcvP).getSessionID(), lastSeq, lastAck, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, verifyAck(sendP).getRcvWind(), new byte[0], 0);
						ACKPacket = new DatagramPacket(ACKDataPacket.toArray(), ACKDataPacket.toArray().length, address, connect.getPort());
						while ((verifyAck(rcvP).getACK() != 1) || (verifyAck(rcvP).getPOST() != 1) || (verifyAck(rcvP).getSeqNum() != lastAck + 1)) {
							trySend(socket, ACKPacket, rcvP, address);
						}
					}
					connect.addData(data);			//CHECK ON LENGTH!!!!!!!!!!!!???????????
					writeDataToFile(filename, connect.getData(), connect);
					lastAck = verifyAck(rcvP).getSeqNum();
					lastSeq = verifyAck(rcvP).getAckNum(); //verifyAck(ACKPacket).getSeqNum(); // DOUBLE CHECK THIS // last SEQ # that was sent
					Packet ACKDataPacket = new Packet(verifyAck(sendP).getSessionID(), lastSeq++, lastAck, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, connect.getRcvWind(), new byte[0], 0);
					DatagramPacket ACKpacket = new DatagramPacket(ACKDataPacket.toArray(), ACKDataPacket.toArray().length, address, connect.getPort());
					trySend(socket, ACKpacket);
				}
			} else {
				//System.out.println("TEST10");
				return false; //will never happen
			}
		} while (true);
	}
	
	public boolean send(byte flag, String filenameArg, DatagramSocket socket) {
		byte[] data = filenameArg.getBytes();
		if (data.length > Packet.MAXDATASIZE) {
			System.out.println("File name is too long.");	//this would be absurd for a file name
		}
		Packet sendDataPacket = new Packet(this.connection.getSessionID(), this.connection.getSeqNum(), 47, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0, connection.getRcvWind(), data, data.length);
		DatagramPacket sendPacket = new DatagramPacket(sendDataPacket.toArray(), sendDataPacket.toArray().length, connection.getAddress(), connection.getPort());
		this.connection.setSeqNum(this.connection.getSeqNum() + 1);
		
		if (trySend(socket, sendPacket, this.connection.getAddress(), this.connection, filenameArg)) { //DAMAGE LINE
			//System.out.println("TEST1");
			//----------------------------------------------------
			//System.out.println("TEST2");
			return true;
		}
	//	System.out.println("TEST3");
		return false;
	}
	
	private static void writeDataToFile(String filename, byte[] dataToWrite, Connection connection) {
		boolean loopflag = true;
		while (loopflag) {
			try {
				if (fileCount == 0) {
					Files.write((new File(filename)).toPath(), connection.getData(), StandardOpenOption.CREATE);
				} else {
					Files.write((new File(filename)).toPath(), connection.getData(), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
				}
				loopflag = false;
			} catch (Exception e) {
				//System.out.println("error: " + e);
			}			//RISK OF INFINITE LOOP!!!!!!!!!!!!!!
		}
	}
	
	public boolean receive(DatagramSocket socket){
		DatagramPacket genericRcvPacket = new DatagramPacket(new byte[Packet.MAXPACKETSIZE], Packet.MAXPACKETSIZE);
		if ((tryReceive(socket, genericRcvPacket, connection.getAddress())) && (checkHash(genericRcvPacket))) {
			if (verifyAck(genericRcvPacket).getFIN() == (byte) 1) { //server initiated close
				if (closeReceive(socket, verifyAck(genericRcvPacket))) {
					socket.close();
					return true;
				} else {
					System.out.println("Failed in attempt to handle server initiated close.");
					return false;
				}
			}
		}
		/*
		 * if received ACK											//SHOULD ACK PACKETS HAVE A SEQ NUM AND BE ADDED TO THE WINDOW???
		 * 		check ACK against expected number and the window	//SHOULD WE ACK AND RESPOND OR SIMPLY RESPOND TO THINGS LIKE GET???
		 * 		update window variables and window
		 * if received GET
		 * 		retrieve data
		 * 		send data back
		 * if received POST ***implement at the end***
		 * 		read in and store data
		 * 		send ACK
		 */
		return false;
	}

	private boolean closeReceive(DatagramSocket socket, Packet serverACKPack) {
		Packet FINACKDataPacket = new Packet(serverACKPack.getSessionID(), connection.getSeqNum(), serverACKPack.getSeqNum(), (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 1, connection.getRcvWind(), new byte[0], 0);
		DatagramPacket FINACKpacket = new DatagramPacket(FINACKDataPacket.toArray(), FINACKDataPacket.toArray().length, connection.getAddress(), connection.getPort());
		DatagramPacket rcvPacket = new DatagramPacket(new byte[FINACKDataPacket.toArray().length], FINACKDataPacket.toArray().length);
		
		boolean receivedResponse = trySend(socket, FINACKpacket, rcvPacket, connection.getAddress());
		connection.setSeqNum(connection.getSeqNum() + 1);
		
		if ((receivedResponse) && (verifyAck(rcvPacket).getAckNum() == FINACKDataPacket.getSeqNum()) && (checkHash(rcvPacket))){
			if (verifyAck(rcvPacket).getACK() == (byte) 1) {
				return true;
			}
		}
		return false;
	}

	//CHECK SEQ/ACK NUMS and MD5
	public boolean close(int ID, DatagramSocket socket) {	
		InetAddress address = this.connection.getAddress();
		int port = this.connection.getPort();
		
		Packet FINDataPacket = new Packet(ID, this.connection.getSeqNum(), this.connection.getAckNum(), (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0, this.connection.getRcvWind(), new byte[0], 0);
		DatagramPacket FINpacket = new DatagramPacket(FINDataPacket.toArray(), FINDataPacket.toArray().length, address, port);
		DatagramPacket rcvPacket = new DatagramPacket(new byte[FINDataPacket.toArray().length], FINDataPacket.toArray().length);
		
		boolean receivedResponse = trySend(socket, FINpacket, rcvPacket, address);
		connection.setSeqNum(connection.getSeqNum() + 1);
		
		if ((receivedResponse) && (verifyAck(rcvPacket).getAckNum() == connection.getSeqNum() - 1) && (checkHash(rcvPacket))) {
			byte[] rcvData = rcvPacket.getData();
			byte checkVal = (byte) 1;
			if (rcvData[16] == checkVal) {
				Packet ACKFinalPacket = new Packet(ID, this.connection.getSeqNum(), verifyAck(rcvPacket).getSeqNum(), (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, this.connection.getRcvWind(), new byte[0], 0);
				DatagramPacket ACKFPacket = new DatagramPacket(ACKFinalPacket.toArray(), ACKFinalPacket.toArray().length, address, port);
				
				if (rcvData[14] == checkVal) {
					if (trySend(socket, ACKFPacket)) {
						this.connection.setAckNum(verifyAck(rcvPacket).getSeqNum());
						socket.close();
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
							socket.close();
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
