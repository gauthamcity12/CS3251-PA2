import java.nio.ByteBuffer;
import java.security.MessageDigest;
											//FIGURE OUT WHICH METHODS ARE ACTUALLY NEEDED
public class Packet {  //DAMAGE LINE
	
	//instance variables of header components////
	private int sessionID;
	private int seqNum;
	private int ackNum;
	private byte GET;
	private byte POST;
	private byte FIN;
	private byte SYN;
	private byte ACK;
	private int rcvWind;
	private int dataSize;
	private static MessageDigest hash;
	private byte[] digest = new byte[16];
	private byte[] data = new byte[MAXDATASIZE];					//CHECK SIZE AND TYPE FOR THIS VARIABLE
	protected static final int MAXPACKETSIZE = 65500;
	protected static final int MAXDATASIZE = MAXPACKETSIZE - 41;
	private boolean isSent;
	private boolean isOld;

	//General constructor
	public Packet(int id, int sNum, int aNum, byte G, byte P, byte F, byte S, byte A, int rWind, byte[] dataToSend, int dSize) {
		this.sessionID = id;
		this.seqNum = sNum;
		this.ackNum = aNum;
		this.GET = G;
		this.POST = P;
		this.FIN = F;
		this.SYN = S;
		this.ACK = A;
		this.rcvWind = rWind;
		this.dataSize = dSize;
		this.data = dataToSend;
		try {
			hash = MessageDigest.getInstance("MD5");
		} catch (java.security.NoSuchAlgorithmException e) {
			System.exit(1);
		}
		ByteBuffer temp = ByteBuffer.allocate(25);
		temp.putInt(this.sessionID);
		temp.putInt(this.seqNum);
		temp.putInt(this.ackNum);
		temp.put(this.GET);
		temp.put(this.POST);
		temp.put(this.FIN);
		temp.put(this.SYN);
		temp.put(this.ACK);
		temp.putInt(this.rcvWind);
		temp.putInt(this.dataSize);
		byte[] anotherTemp = temp.array();
		//Taken from http://stackoverflow.com/questions/5513152/easy-way-to-concatenate-two-byte-arrays
		byte[] aboutToHash = new byte[anotherTemp.length + dataSize];
		System.arraycopy(anotherTemp, 0, aboutToHash, 0, anotherTemp.length);
		System.arraycopy(data, 0, aboutToHash, anotherTemp.length, dataSize);
		this.digest = hash.digest(aboutToHash);
		this.isOld = false;
		this.isSent = false;
	}
	
	public Packet(byte[] packet) {
		byte[] temp = new byte[4];
		System.arraycopy(packet, 0, temp, 0, 4);
		this.sessionID = java.nio.ByteBuffer.wrap(temp).getInt();	//cite http://stackoverflow.com/questions/5616052/how-can-i-convert-a-4-byte-array-to-an-integer
		System.arraycopy(packet, 4, temp, 0, 4);
		this.seqNum = java.nio.ByteBuffer.wrap(temp).getInt();
		System.arraycopy(packet, 8, temp, 0, 4);
		this.ackNum = java.nio.ByteBuffer.wrap(temp).getInt();
		this.GET = packet[12];
		this.POST = packet[13];
		this.FIN = packet[14];
		this.SYN = packet[15];
		this.ACK = packet[16];
		System.arraycopy(packet, 17, temp, 0, 4);
		this.rcvWind = java.nio.ByteBuffer.wrap(temp).getInt();
		System.arraycopy(packet, 21, temp, 0, 4);
		this.dataSize = java.nio.ByteBuffer.wrap(temp).getInt();
		System.arraycopy(packet, 25, this.digest, 0, 16);
		System.arraycopy(packet, 41, this.data, 0, Math.min(data.length, packet.length - 41));
		
	}

	public byte[] toArray() {
		ByteBuffer temp = ByteBuffer.allocate(MAXPACKETSIZE);
		temp.putInt(this.sessionID); //index 0-3
		temp.putInt(this.seqNum); //index 4-7
		temp.putInt(this.ackNum); //index 8-11
		temp.put(this.GET); //index 12
		temp.put(this.POST); //index 13
		temp.put(this.FIN); //index 14
		temp.put(this.SYN); //index 15
		temp.put(this.ACK); //index 16
		temp.putInt(this.rcvWind); //index 17-20
		temp.putInt(this.dataSize); //index 21-24
		temp.put(this.digest); //index 25-40
		temp.put(this.data); //index 41 -->
		return temp.array();
	}
	/**
	 * Sets new session ID
	 * @param id for new session ID
	 */
	public void setSessionID(int id) {
		this.sessionID = id;
	}

	/**
	 * Gets session ID
	 * @return session ID
	 */
	public int getSessionID() {
		return this.sessionID;
	}

	/**
	 * Sets new sequence number
	 * @param sn for new sequence number
	 */
	public void setSeqNum(int sn) {
		this.seqNum = sn;
	}

	/**
	 * Gets sequence number
	 * @return sequence number
	 */
	public int getSeqNum() {
		return this.seqNum;
	}

	/**
	 * Sets new acknowledgement number
	 * @param an for new acknowledgement number
	 */
	public void setAckNum(int an) {
		this.ackNum = an;
	}

	/**
	 * Gets acknowledgement number
	 * @return acknowledgement number
	 */
	public int getAckNum() {
		return this.ackNum;
	}

	/**
	 * Sets GET bit
	 * @param g for GET value
	 */
	public void setGET(byte g) {
		this.GET = g;
	}

	/**
	 * Gets GET bit
	 * @return GET bit value
	 */
	public byte getGET() {
		return this.GET;
	}

	/**
	 * Sets POST bit
	 * @param p for POST value
	 */
	public void setPOST(byte p) {
		this.POST = p;
	}

	/**
	 * Gets POST bit
	 * @return POST bit value
	 */
	public byte getPOST() {
		return this.POST;
	}

	/**
	 * Sets SYN bit
	 * @param s for SYN value
	 */
	public void setSYN(byte s) {
		this.SYN = s;
	}

	/**
	 * Gets SYN bit
	 * @return SYN bit value
	 */
	public byte getSYN() {
		return this.SYN;
	}

	/**
	 * Sets ACK bit
	 * @param a for ACK value
	 */
	public void setACK(byte a) {
		this.ACK = a;
	}

	/**
	 * Gets ACK bit
	 * @return ACK bit value
	 */
	public byte getACK() {
		return this.ACK;
	}

	/**
	 * Sets FIN bit
	 * @param f for FIN value
	 */
	public void setFIN(byte f) {
		this.FIN = f;
	}

	/**
	 * Gets FIN bit
	 * @return FIN bit value
	 */
	public byte getFIN() {
		return this.FIN;
	}

	/**
	 * Sets new receive window
	 * @param rwind for new receive window
	 */
	public void setRcvWind(int rwind) {
		this.rcvWind = rwind;
	}

	/**
	 * Gets receive window
	 * @return receive window
	 */
	public int getRcvWind() {
		return this.rcvWind;
	}
	
	/**
	 * Sets data size
	 * @param size for data
	 */
	public void setDataSize(int size) {
		this.dataSize = size;
	}

	/**
	 * Gets data size
	 * @return data size
	 */
	public int getDataSize() {
		return this.dataSize;
	}

	public byte[] recalculateHash() {
		ByteBuffer temp = ByteBuffer.allocate(25);
		temp.putInt(this.sessionID);
		temp.putInt(this.seqNum);
		temp.putInt(this.ackNum);
		temp.put(this.GET);
		temp.put(this.POST);
		temp.put(this.FIN);
		temp.put(this.SYN);
		temp.put(this.ACK);
		temp.putInt(this.rcvWind);
		temp.putInt(this.dataSize);
		byte[] anotherTemp = temp.array();
		//Taken from http://stackoverflow.com/questions/5513152/easy-way-to-concatenate-two-byte-arrays
		byte[] aboutToHash = new byte[anotherTemp.length + dataSize];
		System.arraycopy(anotherTemp, 0, aboutToHash, 0, anotherTemp.length);
		System.arraycopy(data, 0, aboutToHash, anotherTemp.length, dataSize);
		this.digest = hash.digest(aboutToHash);
		return this.digest;
	}

	/**
	 * Gets message digest
	 * @return message digest
	 */
	public byte[] getHash() {
		return this.digest;
	}

	/**
	 * Sets new data
	 * @param d for new data
	 */
	public void setData(byte[] d) {
		this.data = d;
	}

	/**
	 * Gets data
	 * @return data
	 */
	public byte[] getData() {
		return this.data;
	}

	public void packetIsSent() {
		this.isSent = true;
	}
	
	public void needToSend() {
		this.isSent = false;
	}

	public boolean isSent() {
		return this.isSent;
	}
	
	public boolean isOld() {
		return this.isOld;
	}
	
	public void setOld() {
		this.isOld = true;
	}
}
