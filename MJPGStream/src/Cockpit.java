import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.OverlayLayout;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Cockpit implements KeyListener, ActionListener{
	
	//private final String HOST = "152.117.117.170";
	private String host = "127.0.0.1";
	private int port = 5432;
	final String VIEWPANEL = "View"; //name of main camera view tab
	final String SETUPPANEL = "Setup"; //name of setup cam
	
	private int speed = 0;
	private int key;
	private int	turnKeyPressed = 0; //if turn key pressed wait for release
	private int state = 0000; //really 0 but 0000 to show that figuratively we are using
							  //state represented by a 4-bit binary number
	private char lastChar;
	private boolean isConnected = false;
	
	private JFrame frame = new JFrame("Big Dog Cockpit");
	private JPanel view = new JPanel();
	private JPanel setup = new JPanel();
	private JTextField hostnameField = new JTextField(host,16);
	private JTextField portField = new JTextField(Integer.toString(port),4);
	private JButton button1;
	private JButton button2 = new JButton("Update");
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
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addComponentsToPane(frame.getContentPane());
		
		frame.setVisible(true);
		
		//open socket and io streams
		//openSocket();	
		
		
	}
	
	private void addComponentsToPane(Container pane) {
		JTabbedPane tabbedPane = new JTabbedPane();
		JLabel hst = new JLabel("hostname or IP address");
		JLabel prt = new JLabel("port number");
		button1 = new JButton("Connect");
		button1.setActionCommand("toggleConnect");
		button2.setActionCommand("update");
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
		setup.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		//view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		button1.setFocusable(true);
		button1.addKeyListener(this);
		button1.addActionListener(this);
		button2.addActionListener(this);
		new Thread(axPanel).start();
		
		view.add(axPanel);
		view.add(button1);
		button1.setAlignmentX((float) 0.5);

		tabbedPane.setName("Big Dog Cockpit");
		
		setup.add(hst);
		setup.add(hostnameField);
		setup.add(Box.createRigidArea(new Dimension(60,0)));
		setup.add(prt);
		setup.add(Box.createRigidArea(new Dimension(250,0)));
		setup.add(portField);
		setup.add(Box.createRigidArea(new Dimension(50,0)));
		setup.add(button2);
		button2.setAlignmentX(0);
		
		tabbedPane.add(VIEWPANEL, view);
		tabbedPane.add(SETUPPANEL, setup);
		
		Dimension paneSize = new Dimension(360, 320);
		pane.setPreferredSize(paneSize);
		
		pane.add(tabbedPane);
		
		button1.requestFocusInWindow();
		
		
	}

	/**
	 *** opens a socket and corresponding input and output streams ***
	 */
	private boolean openSocket(){
		boolean returnee = false;
	    try {
	           controller = new Socket(host, port);
	           //Socket connection created
	           System.out.println("Connected to: "+host+" --> on port: "+port+"\n'q' to close connection");
	           input = new BufferedReader(new InputStreamReader(controller.getInputStream()));
	           output = new DataOutputStream(controller.getOutputStream());
	           isConnected = true;
	           button1.setText("Disconnect");
	           button1.setBackground(Color.red);
	           returnee = true;
	    }
	    catch (IOException e) {
	        System.out.println("Error opening socket: "+host+" on port "+port+" - "+e);
	    }
	    
		return returnee;
	}

	/**
	 *** close the connection gracefully ***
	 */
	private boolean closeSocket(){
		boolean returnee = false;
		try {
	           output.close();
	           input.close();
	           controller.close();
	           isConnected = false;
	           returnee = true;
	           button1.setText("Connect");
				button1.setBackground(Color.green);
	           System.out.println("bye");
	    } 
	    catch (IOException e) {
	       System.out.println(e);
	    }
	    return returnee;
	}

	/**
	 *** main ***
	 * @param args
	 */
	public static void main(String[] args) {
		Cockpit show = new Cockpit();
	}

	
	
	 /*********************************************************
	 ****************all code below involves:******************
	 **********************************************************
	 ********************EVENT HANDLING************************
	 *********************************************************/
	
	
	
	 /*** Handle the key pressed event from the text field. ***/
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
    
    /*** Handle the key released event from the text field. ***/
    public void keyReleased(KeyEvent e) {
    	key=e.getKeyCode();
    	if(turnKeyPressed > 0 && turnKeyPressed == key){
    		turnKeyPressed = 0;
    		sendOut();
    	}
    }
	
    /**
     *** send correct signal to server based on arrow-keyed events ***
     */
	private void sendOut() {
		//System.out.println("Speed: "+speed);
		//System.out.println("Direction: "+turnKeyPressed);
		
		//set state, that is the binary signal corresponding to current speed-direction
		setState();
		
		//System.out.println("State: "+state);
		
		//should never happen...
		if(state == 1111){
			System.out.println("invalid state!");
			closeSocket();
			System.exit(0);
		}
		
		if(isConnected){
			try{
				output.writeBytes(state+"\n");
				output.flush();
				String responseLine;
				responseLine = input.readLine();
				    System.out.print("Server Echo: " + responseLine+"\n");
				}
			//}
			catch(IOException e){
				isConnected = false;
				System.out.println("Failed to send command: "+e);
			}
		}
	}

	/**
	 ***based on speed and direction, the correct binary signal is set as the state***
	 */
	private void setState() {
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
			closeSocket();
		}
		
	}

	/**
	 *** Handles the Buttons in the application ***
	 */
	public void actionPerformed(ActionEvent e) {
		if("toggleConnect".equals(e.getActionCommand())){
			if(isConnected){
				System.out.println("Closing connection...");
				closeSocket();
			}
			else
				openSocket();
		}//if connect/disconnect button in 1st pane
		
		else if("update".equals(e.getActionCommand())){
			if(isConnected){
				if(closeSocket()){
					host = hostnameField.getText();
					port = Integer.parseInt(portField.getText());
				}
			}//if isConnected
			openSocket();
		}//if update button in 2nd pane
	}

	
}
