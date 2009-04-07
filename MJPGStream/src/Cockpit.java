import java.awt.BorderLayout;
import javax.swing.OverlayLayout;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;

public class Cockpit implements KeyListener{
	
	private final String HOST = "152.117.205.34";
	private final int PORT = 5432;
	
	private int speed = 0;
	private int key;
	private int	turnKeyPressed = 0; //if turn key pressed wait for release
	
	private JFrame jframe = new JFrame();
	private AxisCamera axPanel = new AxisCamera();
	
	Socket controller;
	DataInputStream input;
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
		//openSocket();
				
		
	}
	
	/**
	 *** opens a socket and corresponding input and output streams ***
	 */
	private void openSocket(){
		
	    try {
	           controller = new Socket(HOST, PORT);
	           input = new DataInputStream(controller.getInputStream());
	           output = new DataOutputStream(controller.getOutputStream());
	    }
	    catch (IOException e) {
	        System.out.println("Error opening socket: "+e);
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
    	}
    }
	
	private void sendOut() {
		// HERE will be where we calculate the correct state to send out and send it
		System.out.println("Speed: "+speed);
		System.out.println("Direction: "+turnKeyPressed);
	}

	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	
}
