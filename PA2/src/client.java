import java.net.*;
import java.util.Random;
import java.io.*;
import java.util.*;

public class client {

	private static HashMap<Integer, Connection> connections = new HashMap<>(5);
	private static short rcvWind;					//WHAT ARE WE DOING HERE?
	private static final int TIMEOUT = 3000;
	private static final int MAXTRIES = 5;

	//CHECK SEQ/ACK NUMS and MD5
	public boolean connect(InetAddress address, int port, DatagramSocket socket) { //HANDLE SIMULTANEOUS SYN PACKETS?
		Random rand = new Random();
		Packet SYNDataPacket = new Packet(0, rand.nextInt(), 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0, rcvWind, new byte[0]);
		DatagramPacket SYNpacket = new DatagramPacket(SYNDataPacket.toArray(), SYNDataPacket.toArray().length, address, port);
		DatagramPacket SYNACKrcvPacket = new DatagramPacket(new byte[SYNDataPacket.toArray().length], SYNDataPacket.toArray().length);
		
		boolean receivedResponse = trySend(socket, SYNpacket, SYNACKrcvPacket, address);
		
		if (receivedResponse) {
			byte[] rcvData = SYNACKrcvPacket.getData();
			byte checkVal = (byte) 1;
			if ((rcvData[15] == checkVal) && (rcvData[16] == checkVal)) { // if SYNACK is received
				Packet ACKDataPacket = new Packet(0, rand.nextInt(), 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, rcvWind, new byte[0]);
				DatagramPacket ACKpacket = new DatagramPacket(ACKDataPacket.toArray(), ACKDataPacket.toArray().length, address, port);
				DatagramPacket ACKrcvPacket = new DatagramPacket(new byte[ACKDataPacket.toArray().length], ACKDataPacket.toArray().length);
				
				receivedResponse = trySend(socket, ACKpacket, ACKrcvPacket, address);
				
				if (receivedResponse) { // if ACK is received
					byte[] rcvData2 = ACKrcvPacket.getData();
					checkVal = (byte) 1;
					if (rcvData2[16] == checkVal) {
						//create connection !!!!!!!!!!!!!!!!!!!!!
						return true;
					}
				}
			}
		}
		socket.close();
		System.out.println("Socket connection failed.");
		return false;
	}
	
	private boolean trySend(DatagramSocket socket, DatagramPacket sendP, DatagramPacket rcvP, InetAddress address) {
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
	
	public int send(){
		return 0;
	}
	
	public int receive(){
		return 0;
	}

	//CHECK SEQ/ACK NUMS and MD5
	public boolean close(int ID, DatagramSocket socket) {	
		Connection conn = connections.get(ID);
		InetAddress address = conn.getAddress();
		int port = conn.getPort();
		
		Packet FINDataPacket = new Packet(ID, conn.getSeqNum(), conn.getAckNum(), (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0, rcvWind, new byte[0]);
		DatagramPacket FINpacket = new DatagramPacket(FINDataPacket.toArray(), FINDataPacket.toArray().length, address, port);
		DatagramPacket rcvPacket = new DatagramPacket(new byte[FINDataPacket.toArray().length], FINDataPacket.toArray().length);
		
		boolean receivedResponse = trySend(socket, FINpacket, rcvPacket, address);
		
		if (receivedResponse) {
			byte[] rcvData = rcvPacket.getData();
			byte checkVal = (byte) 1;
			if (rcvData[16] == checkVal) {
				if (rcvData[14] == checkVal) {
					//send final ack
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
					}
				}
			}
		}
		return false;
		
	}
	
}