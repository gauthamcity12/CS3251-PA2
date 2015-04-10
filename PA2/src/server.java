import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.security.MessageDigest;
import java.io.*;
import java.util.*;

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
		
		while (!serverUser.receive(socket, serverUser.connection)) {}
		//System.out.println("Next loop");
		while (!serverUser.receive(socket, serverUser.connection)) {}
		
		
		//check scanner input
		
		//FINISH MAIN METHOD CODE HERE
		
		/*
		 * while loop until close (Then go to idle situation to await connection) {
		 * 		check and interpret scanner input
		 * 		make a non-blocking receive call to check for incoming
		 * 		catch block for timeouts
		 * }
		 */
	}


	public server() {
		//do nothing
	}
	
	public boolean connect(InetAddress address, int port, DatagramSocket socket){
		int session2 = rand.nextInt();
		//System.out.println("///////////////////---");
		Packet SYNACKDataPacket = new Packet(0, session2, 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 1, 32000, new byte[0], 0);
		//System.out.println("///////////////////---");
		DatagramPacket SYNrcvPacket = new DatagramPacket(new byte[SYNACKDataPacket.toArray().length], SYNACKDataPacket.toArray().length);
		
		boolean receivedResponse = tryReceive(socket, SYNrcvPacket, address);
		
		if ((receivedResponse) && (verifyAck(SYNrcvPacket).getAckNum() == 0) && (checkHash(SYNrcvPacket))) { 
			//System.out.println("CHECK1");
			byte[] rcvData = SYNrcvPacket.getData();
			byte checkVal = (byte) 1;
			if (rcvData[15] == checkVal) { // if SYN is received
				int session1 = verifyAck(SYNrcvPacket).getSeqNum();
				SYNACKDataPacket.setAckNum(session1);
				//RECALCULATE HASH
				DatagramPacket SYNACKPacket = new DatagramPacket(SYNACKDataPacket.toArray(), SYNACKDataPacket.toArray().length, address, port);
				DatagramPacket ACKrcvPacket = new DatagramPacket(new byte[SYNACKDataPacket.toArray().length], SYNACKDataPacket.toArray().length);
				
				receivedResponse = trySend(socket, SYNACKPacket, ACKrcvPacket, address);
				if ((receivedResponse) && (verifyAck(ACKrcvPacket).getAckNum() == session2) && (checkHash(ACKrcvPacket))) { // if ACK is received
					//System.out.println("CHECK2");
					byte[] rcvData2 = ACKrcvPacket.getData();
					checkVal = (byte) 1;
					if(rcvData2[16] == checkVal){
						Packet ACKDataPacket2 = new Packet(0, session2 + 1, verifyAck(ACKrcvPacket).getSeqNum(), (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, 32000, new byte[0], 0);
						DatagramPacket ACKPacket = new DatagramPacket(ACKDataPacket2.toArray(), ACKDataPacket2.toArray().length, address, port);
						if (trySend(socket, ACKPacket)) {
							//System.out.println("CHECK3");
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
////		System.out.println("CHECK BB: " + temp.toString());
////		System.out.println("CHECK: " + tempPack.getSessionID());
////		System.out.println("CHECK: " + tempPack.getSeqNum());
////		System.out.println("CHECK: " + tempPack.getAckNum());
////		System.out.println("CHECK: " + tempPack.getGET());
////		System.out.println("CHECK: " + tempPack.getPOST());
////		System.out.println("CHECK: " + tempPack.getFIN());
////		System.out.println("CHECK: " + tempPack.getSYN());
////		System.out.println("CHECK: " + tempPack.getACK());
////		System.out.println("CHECK: " + tempPack.getRcvWind());
////		System.out.println("H': " + anotherTemp);
////		System.out.println("D': " + tempPack.getData());
////		System.out.println("RCV: " + rcvHash);
////		System.out.println("CHE: " + checkHash);
//		return Arrays.equals(rcvHash, checkHash);
		return true;
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
				//System.out.println(verifyAck(sendP).getPOST());
				socket.send(sendP);
				//System.out.println("sent");
				socket.receive(rcvP);
				//System.out.println("rcv");
				if (!rcvP.getAddress().equals(address)) { //Check source for received packet
					throw new IOException("Received packet was from unknown source");
				}
				//System.out.println("addr good");
				receivedResponse = true;
				//System.out.println("rr = true");
			} catch (InterruptedIOException e) {
				tries += 1;
				//System.out.println("t1");
				System.out.println("Timed out, " + (MAXTRIES - tries) + " more tries.");
			} catch (Exception f) {
				System.out.println(f);
				return false;
			}
		} while ((!receivedResponse) && (tries < MAXTRIES));
		//System.out.println("About to return: " + receivedResponse);
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
				//System.out.println("t2");
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
	
	public boolean receive(DatagramSocket socket, Connection connection){
		DatagramPacket genericRcvPacket = new DatagramPacket(new byte[Packet.MAXPACKETSIZE], Packet.MAXPACKETSIZE);
		if ((tryReceive(socket, genericRcvPacket, connection.getAddress())) && (checkHash(genericRcvPacket))) {
			if (verifyAck(genericRcvPacket).getFIN() == (byte) 1) { //client initiated close
				if (closeReceive(socket, verifyAck(genericRcvPacket))) {
					socket.close();
					System.out.println("Closing socket");
					return true;
				} else {
					System.out.println("Failed in attempt to handle client initiated close.");
					return false;
				}
			} else if (verifyAck(genericRcvPacket).getGET() == (byte) 1) {	//client sends GET request packet
				//Packet genericDataPacket = verifyAck(genericRcvPacket);
				ArrayList<Packet> toSend = retrieveFile(verifyAck(genericRcvPacket), connection); //***FINISH THIS***
				for (Packet item : toSend) {
					//System.out.println("POST: " + item.getPOST());
				}
				int count = 0;
				while (!toSend.isEmpty()) {
					Packet temp = toSend.remove(0);
					temp.setSeqNum(connection.getSeqNum());
					connection.setSeqNum(connection.getSeqNum() + 1);
					connection.setAckNum(verifyAck(genericRcvPacket).getSeqNum());
					temp.setAckNum(connection.getAckNum());
					temp.setRcvWind(connection.getRcvWind());
					DatagramPacket packetToSend = new DatagramPacket(temp.toArray(), temp.toArray().length, connection.getAddress(), connection.getPort());
					genericRcvPacket = new DatagramPacket(new byte[Packet.MAXPACKETSIZE], Packet.MAXPACKETSIZE);
//					System.out.println("send from packetstream");
//					System.out.println("DATA = " + verifyAck(packetToSend).getData());
//					System.out.println("Seq: " + verifyAck(packetToSend).getSeqNum());
//					System.out.println("SID: " + verifyAck(packetToSend).getSessionID());
//					System.out.println("POST: " + verifyAck(packetToSend).getPOST());
//					System.out.println("ACK: " + verifyAck(packetToSend).getACK());
//					////
//					System.out.println("BYTE ARRAY");
//					for (int q = 0; q < verifyAck(packetToSend).getDataSize(); q++) {
//						System.out.print(verifyAck(packetToSend).getData()[q]);
//					}
//					System.out.println();
//					////
					while ((!trySend(socket, packetToSend, genericRcvPacket, connection.getAddress())) || (verifyAck(genericRcvPacket).getACK() != (byte) 1) || (verifyAck(genericRcvPacket).getAckNum() != temp.getSeqNum())) {
//						System.out.println((verifyAck(genericRcvPacket).getACK()));// == (byte) 1));
//						System.out.println((verifyAck(genericRcvPacket).getAckNum()));// == temp.getSeqNum()));
//						System.out.println(temp.getSeqNum());
//						System.out.println("loop");
					}
					//System.out.println("COUNT IS " + count);
					count++;
				}
				return true;
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

	private ArrayList<Packet> retrieveFile(Packet rcvPacket, Connection connection) { //seq & ack numbers & rcvWind are not set and should be before they are sent
		ArrayList<Packet> packetStream = new ArrayList<>(); //will be returned
		
		Packet endOfFilePacket = new Packet(rcvPacket.getSessionID(), 0, 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, connection.getRcvWind(), new byte[0], 0);
		
		int dsz = rcvPacket.getDataSize();
		byte[] fnameArray = new byte[dsz];
		System.arraycopy(rcvPacket.getData(), 0, fnameArray, 0, dsz);
		String filename = new String(fnameArray);
		File fnameFile = new File(filename);
		boolean loopflag = true;
		byte[] fileData = new byte[0];
		int sz = 0;
		if (Files.exists(fnameFile.toPath(), LinkOption.NOFOLLOW_LINKS)) {
			//System.out.println("check1");
			while (loopflag) {
				try {
					sz = Files.readAllBytes(fnameFile.toPath()).length;
					fileData = new byte[sz];
					fileData = Files.readAllBytes(fnameFile.toPath());
//					////
//					System.out.println("DATA SIZE = " + sz);
//					System.out.println("BYTE ARRAY");
//					for (int q = 0; q < sz; q++) {
//						System.out.print(fileData[q]);
//					}
//					System.out.println();
//					////
					loopflag = false;
				} catch (Exception e) {
				//	System.out.println("check6 " + e);
				}			//RISK OF INFINITE LOOP!!!!!!!!!!!!!!
			}
			int intFileIndex = 0;
			int maxDataPerPacket = Packet.MAXDATASIZE;
			int numPackets = (sz / maxDataPerPacket) + 1;
			for (int i = 0; i < numPackets; i++) {
				byte[] temp = new byte[Math.min(maxDataPerPacket, sz - intFileIndex)];
				System.arraycopy(fileData, intFileIndex, temp, 0, temp.length);
				intFileIndex += temp.length;
				Packet tempPacket = new Packet(rcvPacket.getSessionID(), 0, 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 1, connection.getRcvWind(), temp, temp.length);
				packetStream.add(tempPacket);
				//System.out.println("pssize " + packetStream.size());
			}
			packetStream.add(endOfFilePacket);
			System.out.println("pssize " + packetStream.size());
			/////////////////
			for (int i = 0; i < packetStream.size(); i++) {
//				System.out.println("SPECIAL BYTE ARRAY");
//				for (int q = 0; q < packetStream.get(i).getDataSize(); q++) {
//					System.out.print(packetStream.get(i).getData()[q]);
//				}
//				System.out.println();
			}
			////////////////
			return packetStream;
		} else {
			//System.out.println("file does not exist");
			packetStream.add(endOfFilePacket);
		//	System.out.println("pssize for no file " + packetStream.size());
			return packetStream;
		}
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
