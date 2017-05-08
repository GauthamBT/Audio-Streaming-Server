package RTP;

//class RTPpacket
//Server
public class RTPpacket {

	// size of the RTP headerFields:
	static int HEADERSIZE = 12;

	// Fields that compose the RTP headerFields
	public int Version;
	public int Padding;
	public int x;
	public int CC;
	public int m;
	public int PayloadType;
	public int SequenceNumber;
	public int TimeStamp;
	public int Ssrc;

	// Bitstream of the RTP headerFields
	public byte[] headerFields;

	// size of the RTP payload
	public int payload_size;
	// Bitstream of the RTP payload
	public byte[] payload;

	// Constructor of an RTPpacket object from headerFields fields and payload
	// bitstream
	public RTPpacket(int PType, int Framenb, int Time, byte[] data, int data_length) {
		// fill by default headerFields fields:
		Version = 128;// Version 2
		Padding = 0;
		x = 0;
		CC = 0;
		m = 0;
		Ssrc = 0;

		// fill changing headerFields fields:
		SequenceNumber = Framenb;
		TimeStamp = Time;
		PayloadType = PType;

		// build the headerFields bistream:
		// --------------------------
		headerFields = new byte[HEADERSIZE];

		// headerFields[0] = ...
		// .....
		int res = 0;

		res = Version | Padding | x | CC;
		headerFields[0] = (byte) res; // Assigning Version, Padding, x and CC.
		res = CC | PayloadType;
		headerFields[1] = (byte) res;// Assigning CC and Payloadtype

		headerFields[2] = (byte) 0;// Sequence number MSB
		headerFields[3] = (byte) SequenceNumber;// SequenceNumber LSB

		headerFields[4] = (byte) 0;// Timestamp
		headerFields[5] = (byte) 0;// Timestamp
		headerFields[6] = (byte) 0;// Timestamp
		headerFields[7] = (byte) 0;// Timestamp

		headerFields[8] = (byte) 0; // SSRC
		headerFields[9] = (byte) 0; // SSRC
		headerFields[10] = (byte) 0; // SSRC
		headerFields[11] = (byte) 0; // SSRC

		// fill the payload bitstream:
		// --------------------------
		payload_size = data_length;
		payload = new byte[data_length];

		// Initializing the class variable.
		payload = data;

	}

	// Constructor of an RTPpacket object from the packet bistream
	public RTPpacket(byte[] packet, int packet_size) {
		// fill default fields:
		Version = 2;
		Padding = 0;
		x = 0;
		CC = 0;
		m = 0;
		Ssrc = 0;

		// check if total packet size is lower than the headerFields size
		if (packet_size >= HEADERSIZE) {
			// get the headerFields bitsream:
			headerFields = new byte[HEADERSIZE];
			for (int i = 0; i < HEADERSIZE; i++)
				headerFields[i] = packet[i];

			// get the payload bitstream:
			payload_size = packet_size - HEADERSIZE;
			payload = new byte[payload_size];
			for (int i = HEADERSIZE; i < packet_size; i++)
				payload[i - HEADERSIZE] = packet[i];

			// interpret the changing fields of the headerFields:
			PayloadType = headerFields[1] & 127;
			SequenceNumber = unsigned_int(headerFields[3]) + 256 * unsigned_int(headerFields[2]);
			TimeStamp = unsigned_int(headerFields[7]) + 256 * unsigned_int(headerFields[6])
					+ 65536 * unsigned_int(headerFields[5]) + 16777216 * unsigned_int(headerFields[4]);
		}
	}

	// getpayload: return the payload bistream of the RTPpacket and its size
	public int getpayload(byte[] data) {

		// System.out.println("Making a packet");
		// System.out.println("RTP Packet length = " + data.length);
		// System.out.println("Data Byte Size = " + payload_size);

		for (int i = 0; i < payload_size; i++)
			data[i] = payload[i];

		return (payload_size);
	}

	// getpayload_length: return the length of the payload
	public int getpayload_length() {
		return (payload_size);
	}

	// getlength: return the total length of the RTP packet
	public int getlength() {
		return (payload_size + HEADERSIZE);
	}

	// getpacket: returns the packet bitstream and its length
	public int getRTPpacket(byte[] packet) {

		System.out.println("Making a packet: From the server.");
		System.out.println("RTP Packet length = " + packet.length);
		System.out.println("Data Byte Size = " + payload_size);
		System.out.println();

		// construct the packet = headerFields + payload
		for (int i = 0; i < HEADERSIZE; i++)
			packet[i] = headerFields[i];
		for (int i = 0; i < payload_size; i++)
			packet[i + HEADERSIZE] = payload[i];

		// return total size of the packet
		return (payload_size + HEADERSIZE);
	}

	// gettimestamp
	public int gettimestamp() {
		return (TimeStamp);
	}

	// getsequencenumber
	public int getsequencenumber() {
		return (SequenceNumber);
	}

	// getpayloadtype
	public int getpayloadtype() {
		return (PayloadType);
	}

	// print headerFieldss without the SSRC
	public void printheaderFields() {
		// TO DO: uncomment

		System.out.println("Version + Padding + X + M = " + headerFields[0]);
		System.out.println("CC + PayloadType = " + headerFields[1]);
		System.out.println("Sequence Number = " + headerFields[2] + headerFields[3]);
		System.out.println("TimeStamp = " + headerFields[4] + headerFields[5] + headerFields[6] + headerFields[7]);
		System.out.println("SSRC = " + headerFields[8] + headerFields[9] + headerFields[10] + headerFields[11]);

		System.out.println();
	}

	// return the unsigned value of 8-bit integer nb
	static int unsigned_int(int nb) {
		if (nb >= 0)
			return (nb);
		else
			return (256 + nb);
	}

}