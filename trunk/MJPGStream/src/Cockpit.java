import java.awt.BorderLayout;
import javax.swing.OverlayLayout;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;

public class Cockpit implements KeyListener{
	
	//private final String HOST = "152.117.117.170";
	private final String HOST = "127.0.0.1";
	private final int PORT = 5432;
	
	private int speed = 0;
	private int key;
	private int	turnKeyPressed = 0; //if turn key pressed wait for release
	private int state = 0000; //really 0 but 0000 to show that figuratively we are using
							  //state represented by a 4-bit binary number
	private char lastChar;
	private boolean isConnected = false;
	
	private JFrame jframe = new JFrame();
	private AxisCamera axPanel = new AxisCamera();
	
	Socket controller;
	BufferedReader input;
    DataOutputStream output;
	
	/**
	 *** Constructor ***
	 * set up jframe then open socket connection
	 * @param host
	 * @param port
	 */
	public Cockpit(){
		JButton button1 = new JButton("Arrow Listener");
		
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		button1.setFocusable(true);
		button1.addKeyListener(this);
		new Thread(axPanel).start();
		jframe.getContentPane().add(axPanel, BorderLayout.PAGE_START);
		jframe.getContentPane().add(button1);

		jframe.setSize(320, 240);
		jframe.setTitle("Big Dog Cockpit");
		jframe.setVisible(true);
		
		//open socket and io streams
		openSocket();	
		
		
	}
	
	/**
	 *** opens a socket and corresponding input and output streams ***
	 */
	private void openSocket(){
		
	    try {
	           controller = new Socket(HOST, PORT);
	           //Socket connection created
	           System.out.println("Connected to: "+HOST+" --> on port: "+PORT+"\n'q' to close connection");
	           input = new BufferedReader(new InputStreamReader(controller.getInputStream()));
	           output = new DataOutputStream(controller.getOutputStream());
	           isConnected = true;
	    }
	    catch (IOException e) {
	        System.out.println("Error opening socket: "+HOST+" on port "+PORT+e);
	    }
		
	}

	/**
	 *** close the program gracefully ***
	 */
	private void close(){
		try {
	           output.close();
	           input.close();
	           controller.close();
	           isConnected = false;
	           System.out.println("bye");
	    } 
	    catch (IOException e) {
	       System.out.println(e);
	    }
	}

	/**
	 *** main ***
	 * @param args
	 */
	public static void main(String[] args) {
		Cockpit show = new Cockpit();
	}

	 /** Handle the key pressed event from the text field. */
    public void keyPressed(KeyEvent e) {
    	key=e.getKeyCode();
    	//only pay attention if a turn is not already pressed
    	if(key==37 || key==39){
    		if(turnKeyPressed==0){
    			turnKeyPressed = key;
    			sendOut();
    		}
    	}
    	if(key==38 && speed!=4){
    		speed++;
    		sendOut();
    	}
    	if(key==40 && speed!=0){
    		speed--;
    		sendOut();
    	}
        
    }
    
    /** Handle the key released event from the text field. */
    public void keyReleased(KeyEvent e) {
    	key=e.getKeyCode();
    	if(turnKeyPressed > 0 && turnKeyPressed == key){
    		turnKeyPressed = 0;
    		sendOut();
    	}
    }
	
	private void sendOut() {
		// HERE will be where we calculate the correct state to send out and send it
		//System.out.println("Speed: "+speed);
		//System.out.println("Direction: "+turnKeyPressed);
		
		//set state
		setState();
		
		//System.out.println("State: "+state);
		
		//should never happen...
		if(state == 1111){
			System.out.println("invalid state!");
			close();
			System.exit(0);
		}
		if(isConnected){
			try{
				output.writeBytes("data\0");
				String responseLine;
				while ((responseLine = input.readLine()) != null) {
				    System.out.print("Server: " + responseLine+"\n");
				}
			}
			catch(IOException e){
				isConnected = false;
				System.out.println("Failed to send command: "+e);
			}
		}
	}

	private void setState() {
		// TODO Auto-generated method stub
		if(speed == 0){
			if (turnKeyPressed == 0)
				state = 0000;
			if (turnKeyPressed == 37)
				state = 1;
			if (turnKeyPressed == 39)
				state = 10;
		}
		
		else if(speed == 1){
			if (turnKeyPressed == 0)
				state = 11;
			if (turnKeyPressed == 37)
				state = 100;
			if (turnKeyPressed == 39)
				state = 101;
		}
		
		else if(speed == 2){
			if (turnKeyPressed == 0)
				state = 110;
			if (turnKeyPressed == 37)
				state = 111;
			if (turnKeyPressed == 39)
				state = 1000;
		}
		
		else if(speed == 3){
			if (turnKeyPressed == 0)
				state = 1001;
			if (turnKeyPressed == 37)
				state = 1010;
			if (turnKeyPressed == 39)
				state = 1011;
		}
		
		else if(speed == 4){
			if (turnKeyPressed == 0)
				state = 1100;
			if (turnKeyPressed == 37)
				state = 1101;
			if (turnKeyPressed == 39)
				state = 1110;
		}
		
		else
			state = 1111;
	}

	
	/**
	 *** for now just close connection when 'q' is typed ***
	 */
	public void keyTyped(KeyEvent e) {
		lastChar = e.getKeyChar();
		System.out.print(lastChar);
		if(lastChar=='q'){
			System.out.println("\nClosing connection...");
			close();
		}
		
	}

	
}
