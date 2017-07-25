import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import RTP.RTPpacket;

public class UDP implements Runnable {

	/**
	 * @param args
	 * @throws IOException
	 */
	// UDP class members.

	UDP(UDP var) {
		server = var.server;
		UDPrecv = var.UDPrecv;
		// filename = var.filename;
	}

	public UDP() {
		// TODO Auto-generated constructor stub
		// System.out.println("Default Constructor");
	}

	public void run() {
		// Running in a new thread.
		System.out.println("Thread Number is: " + Thread.currentThread().getName());

		try {
			// getFile(this.server, this.UDPrecv, this.filename);
			getFile(server, UDPrecv, filename);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public DatagramSocket server;
	public DatagramPacket UDPrecv;
	static String filename = "sample1.wav";
	static int blockSize = 65000;
	static int filesize;
	static int threadPort = 1026;
	static int x = 1;

	public synchronized void getFile(DatagramSocket server, DatagramPacket recv, String filename)
			throws InterruptedException {
		try {
			// System.out.println("Inside getFile");

			// Fetching file format.
			AudioFormat soundFormat = (AudioSystem.getAudioInputStream(new File(filename))).getFormat();

			// Send Format
			sendFormat(server, recv, filename);

			/**********************************************************/
			// Communicating to new thread.
			DatagramSocket newSocket = new DatagramSocket(threadPort++);

			byte[] threadReceive = new byte[12];

			DatagramPacket threadPacket = new DatagramPacket(threadReceive, threadReceive.length);

			newSocket.receive(threadPacket);

			String str = new String(threadPacket.getData());

			System.out.println("From inside new thread: " + str);
			System.out.println();

			// Fetching IP address.
			InetAddress addr = recv.getAddress();

			// System.out.println("addr = " + addr.toString());

			// Fetching Port
			int port = recv.getPort();

			// System.out.println("port = " + port);
			/**********************************************************/

			// Initiating Dataline information.
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, soundFormat);

			// To write data to.
			SourceDataLine source = (SourceDataLine) AudioSystem.getLine(info);

			// Opening Audio Line.
			source.open(soundFormat, blockSize);

			// Start to fetch data from inputstream(from file).
			source.start();

			AudioInputStream ais = AudioSystem.getAudioInputStream(new File(filename));

			AudioInputStream serverStream = AudioSystem.getAudioInputStream(soundFormat.getEncoding(), ais);

			/***********************************************************/

			// To store the read data.
			byte[] read = new byte[blockSize];

			while (serverStream.available() > blockSize) {

				// Reading from file
				serverStream.read(read);

				// Sending to client.
				sendData(newSocket, addr, port, read, soundFormat);

				// source.write(read, 0, read.length); //To test the bytes
				// received.
			}

			// System.out.println("The file is sent to client");
		} catch (UnsupportedAudioFileException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendFormat(DatagramSocket server, DatagramPacket recv, String filename)
			throws IOException, InterruptedException, UnsupportedAudioFileException {
		// TODO Auto-generated method stub

		// Getting AudioFromat
		AudioFormat af = AudioSystem.getAudioInputStream(new File(filename)).getFormat();

		// Sending channels
		byte[] channels = ByteBuffer.allocate(4).putInt(af.getChannels()).array();

		DatagramPacket channel = new DatagramPacket(channels, channels.length, recv.getAddress(), recv.getPort());

		server.send(channel);

		// System.out.println("Sent channels = " + af.getChannels());

		// Sending Framesize
		byte[] frameSize = ByteBuffer.allocate(4).putInt(af.getFrameSize()).array();

		DatagramPacket fr = new DatagramPacket(frameSize, frameSize.length, recv.getAddress(), recv.getPort());

		server.send(fr);

		// System.out.println("Sent framesize = " + af.getFrameSize());

		// Sending Samplesizeinbits
		byte[] sampleSize = ByteBuffer.allocate(4).putInt(af.getSampleSizeInBits()).array();

		DatagramPacket sS = new DatagramPacket(sampleSize, sampleSize.length, recv.getAddress(), recv.getPort());

		server.send(sS);

		// System.out.println("Sent SampleSizeInBits = " +
		// af.getSampleSizeInBits());

		// Sending Framerate
		byte[] frameRate = ByteBuffer.allocate(4).putFloat(af.getFrameRate()).array();

		DatagramPacket fR = new DatagramPacket(frameRate, frameRate.length, recv.getAddress(), recv.getPort());

		server.send(fR);

		// System.out.println("Sent FrameRate = " + af.getSampleRate());

		// Sending SampleRate
		byte[] sampleRate = ByteBuffer.allocate(4).putFloat(af.getSampleRate()).array();

		DatagramPacket sR = new DatagramPacket(sampleRate, sampleRate.length, recv.getAddress(), recv.getPort());

		server.send(sR);

		// System.out.println("Sent sampleRate = " + af.getSampleRate());

		// Check Endian and send int.
		if (af.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) {
			byte[] encoding = ByteBuffer.allocate(4).putInt(1).array();

			DatagramPacket encoded = new DatagramPacket(encoding, encoding.length, recv.getAddress(), recv.getPort());

			server.send(encoded);

			Thread.sleep(af.getSampleSizeInBits());
		} else {
			byte[] encoding = ByteBuffer.allocate(4).putInt(2).array();

			DatagramPacket encoded = new DatagramPacket(encoding, encoding.length, recv.getAddress(), recv.getPort());

			server.send(encoded);
		}

		// System.out.println("Sent encoding = " + af.getEncoding());

		// Sending Endian
		if (af.isBigEndian() == true) {
			byte[] bigEndian = ByteBuffer.allocate(4).putInt(1).array();

			DatagramPacket bE = new DatagramPacket(bigEndian, bigEndian.length, recv.getAddress(), recv.getPort());

			server.send(bE);
		} else {
			byte[] bigEndian = ByteBuffer.allocate(4).putInt(2).array();

			DatagramPacket bE = new DatagramPacket(bigEndian, bigEndian.length, recv.getAddress(), recv.getPort());

			server.send(bE);
		}

		// System.out.println("Sent endian = " + af.isBigEndian());

		filesize = (int) (new File(filename)).length();

		// To Send filesize.
		byte[] fileByte = ByteBuffer.allocate(4).putInt((int) filesize).array();

		DatagramPacket fileS = new DatagramPacket(fileByte, fileByte.length, recv.getAddress(), recv.getPort());

		server.send(fileS);

		// System.out.println("Sent filesize = " + ((int) (new
		// File(filename)).length()));

		// Sending Thread Port Address
		byte[] portSend = ByteBuffer.allocate(4).putInt((int) threadPort).array();

		DatagramPacket portPacket = new DatagramPacket(portSend, portSend.length, recv.getAddress(), recv.getPort());

		server.send(portPacket);

		// System.out.println("Port Sent = " + threadPort);

		System.out.println("Format Sent = " + af.toString());
	}

	public synchronized void sendData(DatagramSocket newSocket, InetAddress addr, int port, byte[] send,
			AudioFormat af) {

		byte[] rtpSend = new byte[send.length + 12];

		RTPpacket rtp = new RTPpacket(0, x++, (int) System.currentTimeMillis() / 1000, send, blockSize);

		// To get RTP packet
		// int i = rtp.getRTPpacket(rtpSend);
		
		rtp.getRTPpacket(rtpSend);

		// Creating Datagram packet to send to client.
		DatagramPacket dataSnd = new DatagramPacket(rtpSend, rtpSend.length, addr, port);

		// Printing the received RTP packet
		rtp.printheaderFields();

		try {
			newSocket.send(dataSnd);

			// System.out.println("Sending to Client");
			Thread.sleep(blockSize / 100);
			newSocket.receive(new DatagramPacket(new byte[4], 4));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		// Initiating UDP object. ex = Example Object.
		UDP udpSocket = new UDP();

		// Creating UDP socket
		udpSocket.server = new DatagramSocket(1025, InetAddress.getByName("127.0.0.1"));

		// Enabling to reuse the port address
		udpSocket.server.setReuseAddress(true);

		// Byte to be received from client.
		byte[] rcvData = new byte[blockSize];

		System.out.println("Server Running.");
		System.out.println();

		while (true) {
			// Defining DatagramPacket
			udpSocket.UDPrecv = new DatagramPacket(rcvData, rcvData.length);

			// Receiving from client.
			udpSocket.server.receive(udpSocket.UDPrecv);

			UDP newCon = new UDP(udpSocket);

			// Threads implements Thread which creates new threads for
			// execution.
			Thread t = new Thread(newCon);
			// Starting Thread. This will execute the statements in run()
			t.start();

		}

		// Server has to be listening for new connections.
		// UDPserver.close();
	}
}
