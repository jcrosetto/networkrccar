import java.io.DataOutputStream;
import java.io.IOException;

public class SendCommand {

	int steer = 0;
	int speed = 0;
	int state = 0;
	DataOutputStream output;

	public SendCommand(DataOutputStream out){
		output = out;
	}
	/**
	 *** send correct signal to server based on arrow-keyed events ***
	 */
	public void sendOut(int st, int sp) {
		// System.out.println("Speed: "+speed);
		// System.out.println("Direction: "+turnKeyPressed);
		speed = sp;
		steer = st;
	
		setState();

		// System.out.println("State: "+state);
		try {
			System.out.println(state);
			output.writeBytes(state + "\n");
			// output.flush();
			//String responseLine;
			// responseLine = input.readLine();
			// System.out.print("Server Echo: " + responseLine+"\n");
		}
		// }
		catch (IOException e) {
			System.out.println("Failed to send command: " + e);
		}
	}

	/**
	 ***based on speed and direction, the correct binary signal is set as the
	 * state***
	 * speeed is high 3 bits steering is low three bits
	 */
	private void setState() {
		state = (speed << 3) + steer;
		// else
		// state = 1111;
	}

}
