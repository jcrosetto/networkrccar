import javax.swing.ImageIcon;
import javax.swing.JLabel;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import net.java.games.input.Rumbler;

/**
 * Implements a thread for a controller.
 * @author James Crosetto
 *
 */
public class Control implements Runnable {
	
	int speed;
	int steer;
	int prevSteer;
	int prevSpeed;

	boolean isConnected = false;
	boolean hasController = false;
	ControllerEnvironment ce;
	SendCommand sc;
	
	private JLabel speedGauge;
	private JLabel leftInd;
	private JLabel rightInd;
	
	private ImageIcon[] speeds;
	private ImageIcon[] directions;
	
	public Control(ImageIcon[] s, ImageIcon[] d, JLabel sg, JLabel li, JLabel ri){
		speeds = s;
		directions = d;
		rightInd = ri;
		leftInd = li;
		speedGauge = sg;
		prevSteer = 3;//straight
		prevSpeed = 0;//no speed
	}

	/**
	 * Initializes a controller
	 */
	public void run() {
		ce = ControllerEnvironment.getDefaultEnvironment();
		Controller[] ca = ce.getControllers();

		Controller pad = null;
		for (int i = 0; i < ca.length; i++) {

			if (ca[i].getName().equals("Logitech RumblePad 2 USB")) {
				pad = ca[i];
				break;
			}
		}

		if (pad == null) {
			System.out.println("Controller not found.");
			return;
		}
		
		hasController=true;
		
		Rumbler[] rumble = pad.getRumblers();

		while (true) {

			pad.poll();
			EventQueue queue = pad.getEventQueue();

			Event event = new Event();

			while (queue.getNextEvent(event)) {
				Component comp = event.getComponent();
				String id = comp.getIdentifier().toString();
				float value = event.getValue();
				if (comp.isAnalog()
						&& (id.equals("x") || id.equals("y"))) {
					//System.out.println(value);
					controllerCommand(id, value);
				} /*
				 * else { if(value==1.0f) { //append("On"); } else {
				 * //buffer.append("Off"); } }
				 */
				// System.out.println(buffer.toString());
			}

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(!hasController)
				return;
		}
		
	}

	public void removeController() {
		hasController = false;
	}
	
	public void resume(){
		hasController = true;
	}
	
	
	public void controllerCommand(String direction, float value){
		//moving backward
		if(isConnected){
			if(direction.equals("x")){
				//full left
				if(value < -0.75){
					steer = 0;
				}
				else if (value < -0.5){
					steer = 1;
				}
				else if (value < -0.25){
					steer = 2;
				}
				else if (value < 0.25){
					steer = 3;
				}
				else if (value < 0.5){
					steer = 4;
				}
				else if (value < 0.75){
					steer = 5;
				}
				else {
					steer = 6;
				}
				
				if(steer != prevSteer){
					sendOut();
					prevSteer = steer;
				}
			}
			
			//moving forward
			else if(direction.equals("y")){
				//stopped
				if(value > -0.2){
					speed = 0;
				}
				else if (value > -0.4){
					speed = 1;
				}
				else if (value > -0.6){
					speed = 2;
				}
				else if (value > -0.8){
					speed = 3;
				}
				else {
					speed = 4;
				}
				if(prevSpeed != speed){
					prevSpeed = speed;
					sendOut();
				}
			}
		}
		
	}
	
	/**
	 * Send the command.
	 */
	private void sendOut() {
		if(steer == 0){
			leftInd.setIcon(directions[0]);
			rightInd.setIcon(directions[2]);
		}
		else if(steer == -1){
			leftInd.setIcon(directions[1]);
		}
		else if(steer == 1){
			rightInd.setIcon(directions[3]);
		}
		speedGauge.setIcon(speeds[speed]);
		sc.sendOut(steer, speed);
	}
	
	/**
	 * Sets the connection variable
	 * @param con connection variable. True if connected to camera, otherwise false.
	 */
	public void setCon(boolean con){
		isConnected = con;
	}
	
	public void setOut(SendCommand s){
		sc=s;		
	}

}
