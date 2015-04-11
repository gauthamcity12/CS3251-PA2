import java.net.*;
										//CHECK WHICH GET/SET AND OTHER METHODS ARE NEEDED
public class Connection {

	//instance variables for a connection object
	private int sessionID;
	private int seqNum;
	private int ackNum;
	private InetAddress address;
	private int port;
	private byte[] buffer = new byte[MAXBUFSIZE];				//CHECK SIZE AND TYPE OF THIS VARIABLE
	private int firstEmptyIndex;
	private static final int MAXBUFSIZE = 65600;	//CHECK HOW TO CALCULATE THE RCVWIND???

	public Connection(int ID, int sn, int an, InetAddress addr, int port) {
		this.firstEmptyIndex = 0;
		this.sessionID = ID;
		this.seqNum = sn;
		this.ackNum = an;
		this.address = addr;
		this.port = port;
	}
	
	/**
	 * set session ID
	 * @param id for new session ID
	 */
	public void setSessionID(int id) {
		this.sessionID = id;
	}

	/**
	 * get session ID
	 * @return session ID
	 */
	public int getSessionID() {
		return this.sessionID;
	}
	
	/**
	 * set seqNum
	 * @param seqNum for new seqNum
	 */
	public void setSeqNum(int seqNum)
	{
		this.seqNum = seqNum;
	}
	
	
	/**
	 * get seqNum
	 * @return seqNum
	 */
	public int getSeqNum()
	{
		return this.seqNum;
	}
	
	/**
	 * set ackNum
	 * @param ackNum for new ackNum
	 */
	public void setAckNum(int ackNum)
	{
		this.ackNum = ackNum;
	}
	
	/**
	 * get ackNum
	 * @return ackNum
	 */
	public int getAckNum()
	{
		return this.ackNum;
	}
	
	
	/**
	 * set address
	 * @param add for address
	 */
	public void setAddress(InetAddress add) {
		this.address = add;
	}

	/**
	 * get address
	 * @return address
	 */
	public InetAddress getAddress() {
		return this.address;
	}

	/**
	 * set port
	 * @param portNum for connection
	 */
	public void setPort(int portNum) {
		this.port = portNum;
	}

	/**
	 * get port
	 * @return port
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * get receive window size
	 * @return receive window size
	 */
	public int getRcvWind() {
		return MAXBUFSIZE - firstEmptyIndex;
	}

	/**
	 * add data to the buffer
	 * @param data to add
	 */
	public void addData(byte[] data) {
		if (firstEmptyIndex >= buffer.length) {
			System.out.println("Receive window is full.");
			return;
		}
		int i;
		for (i = firstEmptyIndex; i < Math.min(buffer.length, data.length + firstEmptyIndex); i++) {
			buffer[i] = data[i - firstEmptyIndex];
		}
		firstEmptyIndex = Math.max(firstEmptyIndex, i);
		System.out.println("Buffer contents " + firstEmptyIndex);
	}

	//********FIX GETDATA() METHODS**************
	
	/**
	 * get data
	 * @return data
	 */
	public byte[] getData() {
		byte[] temp = new byte[firstEmptyIndex];
		System.arraycopy(buffer, 0, temp, 0, firstEmptyIndex);
		buffer = new byte[MAXBUFSIZE];
		firstEmptyIndex = 0;
		return temp;
	}
}
