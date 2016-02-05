//Ben Chin
import java.net.Socket;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.util.Random;


public class Ipv4Client{

	public static void main(String[] args)throws IOException{
		try(Socket socket = new Socket("cs380.codebank.xyz", 38003)){
			InputStream is = socket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			OutputStream out = socket.getOutputStream();
			byte[] packet;
			for(int i = 1; i < 13; i++){
				int dataSize = (int)Math.pow(2.0, i);
				short totalLength = (short)(20 + dataSize);
				packet = new byte[totalLength];
				new Random().nextBytes(packet); //assign random data first
				//now header
				packet[0] = 0b01000101; //version 4 and HLen 5
				packet[1] = 0; //TOS
				packet[2] = (byte)((totalLength & 0xFF00)>>>8); //first byte of length
				packet[3] = (byte)(totalLength & 0x00FF); //second byte of length
				packet[4] = 0; //first byte of Ident
				packet[5] = 0; //second byte of Ident
				packet[6] = (byte)0x40; //flags and offset
				packet[7] = 0; //offset cont'd
				packet[8] = 50; //TTL
				packet[9] = 6; //protocol
				packet[10] = 0; //assume checksum 0 first
				packet[11] = 0; //assume checksum 0 first
				for(int j = 12; j < 16; j++) //all 0s for sourceAddr
					packet[j] = 0;
				byte[] destAddr = socket.getInetAddress().getAddress();
				for(int j = 0; j < 4; j++) //destAddr
					packet[j+16] = destAddr[j];
				short checksum = checksum(packet); //calc checksum
				packet[10] = (byte)((checksum & 0xFF00)>>>8); //first byte of checksum
				packet[11] = (byte)(checksum & 0x00FF); //second byte of checksum
				out.write(packet);
				String message = br.readLine();
				System.out.println("packet size: " + dataSize);
				System.out.println(message);
				if(!message.equals("good"))
					System.exit(1);
			}//end for
		}//end try
	}

	public static short checksum(byte[] header){
		long sum = 0;
		for(int i = 0; i < 20; i+=2){
			int thisInt = header[i] & 0xFF;
			thisInt <<= 8;
			thisInt |= header[i+1];
			sum += thisInt;

			if((sum & 0xFFFF0000)!=0){
				sum &= 0x0000FFFF;
				sum++;
			}
		}
		return (short)~sum;
	}

}
