import java.net.*;
import java.util.HashMap;
import java.util.Random;
import java.io.*;
import java.util.*;

public class server {
	
	private static HashMap<Integer, Connection> connections = new HashMap<>(5);
	private static short rcvWind;	
	private static final int TIMEOUT = 3000;
	private static final int MAXTRIES = 5;
	private static Random rand = new Random();
	
	public boolean connect(InetAddress address, int port, DatagramSocket socket){
		Packet SYNACKDataPacket = new Packet(0, rand.nextInt(), 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 1, rcvWind, new byte[0]);
		DatagramPacket SYNrcvPacket = new DatagramPacket(new byte[SYNACKDataPacket.toArray().length], SYNACKDataPacket.toArray().length);
		DatagramPacket SYNACKPacket = new DatagramPacket(SYNACKDataPacket.toArray(), SYNACKDataPacket.toArray().length, address, port);
		
		boolean receivedResponse = tryReceive(socket, SYNrcvPacket, address);
		if(receivedResponse){ 
			byte[] rcvData = SYNrcvPacket.getData();
			byte checkVal = (byte) 1;
			if (rcvData[15] == checkVal) { // if SYN is received
				Packet ACKDataPacket = new Packet(0, rand.nextInt(), 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, rcvWind, new byte[0]);
				DatagramPacket ACKrcvPacket = new DatagramPacket(new byte[ACKDataPacket.toArray().length], ACKDataPacket.toArray().length);
				
				receivedResponse = trySend(socket, SYNACKPacket, ACKrcvPacket, address);
				if(receivedResponse){ // if ACK is received
					byte[] rcvData2 = ACKrcvPacket.getData();
					checkVal = (byte) 1;
					if(rcvData[16] == checkVal){
						return true;
					}
				}
			}
		}
		socket.close();
		System.out.println("Socket connection failed!");
		return false;
	
	}
	
	private boolean tryReceive(DatagramSocket socket, DatagramPacket rcvP, InetAddress address) {
		boolean receivedResponse = false;
		int tries = 0;
		do {
			try {
				socket.setSoTimeout(TIMEOUT);
				//socket.send(sendP);
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
	
	private boolean trySend(DatagramSocket socket, DatagramPacket sendP, InetAddress address) {
		boolean receivedResponse = false;
		int tries = 0;
		do {
			try {
				socket.setSoTimeout(TIMEOUT);
				socket.send(sendP);
				receivedResponse = true;
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
	
	public boolean close(int ID, DatagramSocket socket){ // FIN has been received first
		Connection conn = connections.get(ID);
		InetAddress address = conn.getAddress();
		int port = conn.getPort();
		
		Packet FINACKDataPacket = new Packet(ID, conn.getSeqNum(), conn.getAckNum(), (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 1, rcvWind, new byte[0]);
		DatagramPacket FINACKpacket = new DatagramPacket(FINACKDataPacket.toArray(), FINACKDataPacket.toArray().length, address, port);
		DatagramPacket ACKrcvPacket = new DatagramPacket(new byte[FINACKDataPacket.toArray().length], FINACKDataPacket.toArray().length);
	
		boolean receivedResponse = trySend(socket, FINACKpacket, ACKrcvPacket, address);
		if(receivedResponse){ // if ACK packet received
			byte[] rcvData = ACKrcvPacket.getData();
			byte checkVal = (byte) 1;
			if(rcvData[16]==checkVal){
				// Terminate connection
				socket.close();
				System.out.println("Session Finished");
				return true;
			}
		}
		return false;
	}
	
}
