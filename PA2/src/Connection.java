import java.net.*;
										//CHECK WHICH GET/SET AND OTHER METHODS ARE NEEDED
public class Connection {

	//instance variables for a connection object
	private int sessionID;
	private int seqNum;
	private int ackNum;
	private InetAddress address;
	private int port;
	private byte[] buffer;				//CHECK SIZE AND TYPE OF THIS VARIABLE

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
	 * add data to the buffer
	 * @param data to add
	 */
	public void addData(byte[] data) {
		for (int i = 0; i < Math.min(buffer.length, data.length); i++) {
			buffer[i] = data[i];
		}
	}

	/**
	 * add data to the buffer, starting at the specified index
	 * @param data to add
	 * @param index to start adding at
	 */
	public void addDataAtIndex(byte[] data, int index) {
		for (int i = 0; i < Math.min((buffer.length - index), data.length); i++) {
			buffer[index + i] = data[i];
		}
	}

	/**
	 * get data
	 * @return data
	 */
	public byte[] getData() {
		return this.buffer;
	}

	/**
	 * get data in amount specified
	 * @return data
	 */
	public byte[] getData(int size) {
		byte[] temp = new byte[size];
		for (int i = 0; i < size; i++) {
			temp[i] = buffer[i];
		}
		return temp;
	}
}