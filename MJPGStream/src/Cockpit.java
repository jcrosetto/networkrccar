import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;

import net.java.games.input.ControllerEvent;
import net.java.games.input.ControllerListener;

public class Cockpit implements KeyListener, ActionListener, ControllerListener {

	// private final String HOST = "152.117.117.170";
	// private String host = "127.0.0.1";
	private int port = 5432;
	final String VIEWPANEL = "View"; // name of main camera view tab
	final String SETUPPANEL = "Setup"; // name of setup cam
	final private String[] SPEEDPATHS = { "GUIspeed0.png", "GUIspeed1.png",
			"GUIspeed2.png", "GUIspeed3.png", "GUIspeed4.png", "GUIspeed5.png" };
	final private String[] ARROWPATHS = { "GUILeftOff.png", "GUILeftOn.png",
			"GUIRightOff.png", "GUIRightOn.png" };
	final private String[] LOGOPATHS = {"GUIlogoOff.png" , "GUIlogoOn.png"};

	private int speed = 0;
	private int key;
	private int turnKeyPressed = 0; // if turn key pressed wait for release
	// using
	// state represented by a 4-bit binary number
	private char lastChar;
	private boolean isConnected = false;

	//main containers
	private JFrame frame = new JFrame("Big Dog Cockpit");
	private JPanel view = new JPanel(); //first tabbed pane
	private JPanel setup = new JPanel(); //second tabbed pane
	
	//sub-containers
	private JPanel indicatorPane = new JPanel(); //
	private JPanel feedPane = new JPanel();
	private JPanel controlPane = new JPanel();
	private JPanel graphics = new JPanel();
	
	//textfields to go in the "Setup" panel
	private JTextField hostnameField, mjpgStreamField, userField, passField;
	private JTextField portField = new JTextField(Integer.toString(port), 4); // refers to port server
																			  // is listening on
	//two buttons needed
	private JButton button1 = new JButton("Connect");
	private JButton button2 = new JButton("Update");
	private JButton useConButton = new JButton("Use Controller");
	
	//icon images are loaded in here and toggled based on state
	private ImageIcon[] speeds;
	private ImageIcon[] directions;
	private ImageIcon[] logos;
	private Thread feed;
	private Thread cont;
	private Control c;
	
	//JLabels used to hold icons
	private JLabel speedGauge = new JLabel();
	private JLabel leftInd = new JLabel();
	private JLabel rightInd = new JLabel();
	private JLabel logo = new JLabel();
	private AxisCamera axPanel = new AxisCamera();

	//communication with car's server necessities
	Socket controller;
	BufferedReader input;
	DataOutputStream output;
	Control con;
	int steer=3;
	
	SendCommand sc;

	/**
	 *** Constructor *** 
	 * set up jframe then open socket connection
	 * 
	 * @param host
	 * @param port
	 */
	public Cockpit() {
		// as soon as AxisCamera is instantiated, we can get these
		hostnameField = new JTextField(axPanel.hostName, 16);
		mjpgStreamField = new JTextField(axPanel.mjpegStream, 29);
		userField = new JTextField(axPanel.user, 5);
		passField = new JTextField(axPanel.pass, 5);
		
		//grab all of the indicatorPane images
		speeds = createImageIcon(SPEEDPATHS, "speed");
		directions = createImageIcon(ARROWPATHS, "direction");
		logos = createImageIcon(LOGOPATHS, "logo");
		
		if (speeds != null && directions != null && logos != null) {
			speedGauge.setIcon(speeds[0]);
			leftInd.setIcon(directions[0]);
			rightInd.setIcon(directions[2]);
			logo.setIcon(logos[0]);
		}

		// start out disconnected from mjpeg stream
		// axPanel.disconnect();
		System.out.println(axPanel.connected);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addComponentsToPane(frame.getContentPane());
		frame.setBackground(new Color(0xA7A7A7));
		frame.setVisible(true);
		//frame.setResizable(false);

		// open socket and io streams
		// openSocket();
		//cont = new Thread(c);
		c = new Control(speeds, directions, speedGauge, leftInd, rightInd);

	}

	/**
	 *** Set up both of the tabbed panes and add all the components to it ***
	 * @param pane
	 */
	private void addComponentsToPane(Container pane) {
		
		//holds the view and setup tabs using car layout
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setName("Big Dog Cockpit");
		
		//labels
		JLabel hst = new JLabel("hostname or IP address");
		JLabel prt = new JLabel("port number");
		JLabel mjpg = new JLabel("MJPEG Stream Source");
		JLabel usr = new JLabel("Username");
		JLabel pwd = new JLabel("Password");
		
		//add action descriptors
		button1.setActionCommand("toggleConnect");
		button2.setActionCommand("update");
		useConButton.setActionCommand("toggleController");
		
		//set appropriate layouts for all the panes
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
		setup.setLayout(new FlowLayout(FlowLayout.LEFT));
		indicatorPane.setLayout(new BoxLayout(indicatorPane, BoxLayout.LINE_AXIS));
		graphics.setLayout(new BoxLayout(graphics, BoxLayout.Y_AXIS));

		// view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		button1.setFocusable(true);
		button1.addKeyListener(this);
		button1.addActionListener(this);
		button2.addActionListener(this);
		useConButton.addActionListener(this);
		feed = new Thread(axPanel);
		feed.start();

		// setup indicatorPane
		indicatorPane.add(Box.createRigidArea(new Dimension(15,0)));
		indicatorPane.add(leftInd);
		indicatorPane.add(Box.createHorizontalGlue());
		indicatorPane.add(speedGauge);
		indicatorPane.add(Box.createHorizontalGlue());
		indicatorPane.add(rightInd);
		indicatorPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		indicatorPane.add(Box.createRigidArea(new Dimension(15,0)));
		indicatorPane.setBackground(new Color(0x222222));
		
		//setup feedPane
		feedPane.add(axPanel);
		feedPane.setPreferredSize(new Dimension(360, 260));
		feedPane.setMaximumSize(new Dimension(340, 280));
		feedPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		feedPane.setBackground(new Color(0x3B3B3B));
		
		//setup controlPane
		controlPane.add(button1);
		controlPane.add(useConButton);
		button1.setAlignmentX((float) .5);
		useConButton.setAlignmentX((float)0.6);
		controlPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		//controlPane.setOpaque(false);
		controlPane.setBackground(new Color(0x3B3B3B));
		controlPane.setMaximumSize(new Dimension(340, 30));
		
		//setup graphics pane
		graphics.add(logo);
		graphics.setBackground(new Color(0xFAFAFA));
		graphics.setMaximumSize(new Dimension(340, 360));
		logo.setAlignmentX((float) 0.5);
		
		// add live view, indicatorPane, and button to view tab
		view.add(indicatorPane);
		view.add(graphics);
		view.add(Box.createRigidArea(new Dimension(0,3)));
		view.add(feedPane);
		view.add(Box.createRigidArea(new Dimension(0,3)));
		view.add(controlPane);
		view.add(Box.createVerticalGlue());
		view.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		view.setBackground(new Color(0xFAFAFA));

		// add components to setup tab
		setup.add(hst);
		setup.add(Box.createRigidArea(new Dimension(56, 0)));
		setup.add(prt);
		setup.add(hostnameField);
		setup.add(portField);
		setup.add(Box.createRigidArea(new Dimension(100, 0)));
		setup.add(mjpg);
		setup.add(Box.createRigidArea(new Dimension(20, 0)));
		setup.add(mjpgStreamField);
		setup.add(Box.createRigidArea(new Dimension(30, 0)));
		setup.add(usr);
		setup.add(Box.createRigidArea(new Dimension(20, 0)));
		setup.add(pwd);
		setup.add(Box.createRigidArea(new Dimension(100, 0)));
		setup.add(userField);
		setup.add(passField);
		setup.add(Box.createRigidArea(new Dimension(100, 0)));
		setup.add(button2);
		button2.setAlignmentX(0);

		mjpgStreamField.setEditable(false);
		userField.setEditable(false);
		passField.setEditable(false);

		// add tabs to tabbedPane
		tabbedPane.add(VIEWPANEL, view);
		tabbedPane.add(SETUPPANEL, setup);

		// set the dimension of the tab space
		Dimension paneSize = new Dimension(480, 465);
		pane.setPreferredSize(paneSize);

		// add tabbedpane to the pane, then give the connect button focus
		pane.add(tabbedPane);
		button1.requestFocusInWindow();

	}

	/**
	 *** opens a socket and corresponding input and output streams ***
	 */
	private boolean openSocket() {
		boolean returnee = false;
		try {
			if(!axPanel.connected){
				System.out.println("Re-Connect, connected: "+axPanel.connected);
				axPanel = new AxisCamera(hostnameField.getText(), mjpgStreamField.getText(), userField.getText(), passField.getText());
				feed = new Thread(axPanel);
				feed.start();
				feedPane.add(axPanel);
				feedPane.repaint();
				Thread.currentThread();
				Thread.sleep(3000);

				System.out.println("Re-Connect2, connected: "+axPanel.connected);
				//System.out.println("Re-Connect, canceled: "+axPanel.parser.canceled);
			}
			
			//axPanel = new AxisCamera();
			controller = new Socket(axPanel.hostName, port);
			// Socket connection created
			System.out.println("Connected to: " + axPanel.hostName
					+ " --> on port: " + port + "\n'q' to close connection");
			input = new BufferedReader(new InputStreamReader(controller
					.getInputStream()));
			output = new DataOutputStream(controller.getOutputStream());
			sc = new SendCommand(output);
			c.setOut(sc);
			isConnected = true;
			button1.setText("Disconnect");
			button1.setBackground(Color.red);
			logo.setIcon(logos[1]);
			returnee = true;
			sc.sendOut(100, 0);
		} catch (IOException e) {
			System.out.println("Error opening socket: " + axPanel.hostName
					+ " on port " + port + " - " + e);
		}
		catch(Exception ie){
			System.out.println(ie);
		}

		return returnee;
	}

	/**
	 *** close the connection gracefully ***
	 */
	private boolean closeSocket() {
		boolean returnee = false;
		try {
			output.flush();
			output.close();
			input.close();
			controller.close();
			isConnected = false;
			feedPane.remove(axPanel);
			axPanel.parser.canceled = true;
			axPanel.disconnect();
			//System.out.println("Thread is alive: "+feed.getState());
			axPanel.killFeed = true;
			Thread.currentThread();
			Thread.sleep(3000);
			System.out.println("After stop: "+feed.isAlive());
			returnee = true;
			button1.setText("Connect");
			button1.setBackground(Color.green);
			System.out.println("bye");
			logo.setIcon(logos[0]);
		} catch (IOException e) {
			System.out.println(e);
		}
		catch(Exception ie){
			System.out.println(ie);
		}
		
		return returnee;
	}

	/**
	 *** Creates an ImageIcon if the path is valid ***
	 * 
	 * @param String
	 *            - resource path
	 * @param String
	 *            - description of the file
	 */
	protected ImageIcon[] createImageIcon(String[] path, String description) {
		ImageIcon[] icons = new ImageIcon[path.length];
		for (int i = 0; i < path.length; i++) {
			java.net.URL imgURL = getClass().getResource(path[i]);
			if (imgURL != null) {
				icons[i] = new ImageIcon(imgURL, description);
			} else {
				System.err.println("Couldn't find file: " + path);
				return null;
			}
		}// for

		return icons;
	}

	/**
	 *** main ***
	 * 
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

		if (isConnected) {
			key = e.getKeyCode();
			// only pay attention if a turn is not already pressed
			if (key == 37 || key == 39) {
				if (turnKeyPressed == 0) {
					turnKeyPressed = key;
					if(key == 37){
						steer = 0; //left
					}
					else{
						steer=6; //right
					}
					
					sendOut();
				}
			}

			// change speed and then update gauge
			if (key == 38 && speed != 4) {
				speed++;
				sendOut();
			}
			if (key == 40 && speed != 0) {
				speed--;
				sendOut();
			}

		}// if isConnected
	}

	/*** Handle the key released event from the text field. ***/
	public void keyReleased(KeyEvent e) {
		if (isConnected) {
			key = e.getKeyCode();
			if (turnKeyPressed > 0 && turnKeyPressed==key) {
				turnKeyPressed = 0;
				steer = 3;
				sendOut();
			}
		}//is Connected
	}

	/**
	 *** send correct signal to server based on arrow-keyed events ***
	 */
	private void sendOut() {
		if(steer == 3){
			leftInd.setIcon(directions[0]);
			rightInd.setIcon(directions[2]);
		}
		else if(steer == 0){
			leftInd.setIcon(directions[1]);
		}
		else if(steer == 6){
			rightInd.setIcon(directions[3]);
		}
		speedGauge.setIcon(speeds[speed]);
		sc.sendOut(steer, speed);
	}

	/**
	 *** for now just close connection when 'q' is typed ***
	 */
	public void keyTyped(KeyEvent e) {
		lastChar = e.getKeyChar();
		System.out.print(lastChar);
		if (lastChar == 'q') {
			System.out.println("\nClosing connection...");
			closeSocket();
		}

	}

	/**
	 *** Handles the Buttons in the application ***
	 */
	public void actionPerformed(ActionEvent e) {
		if ("toggleConnect".equals(e.getActionCommand())) {
			if (isConnected) {
				System.out.println("Closing connection...");
				// axPanel.disconnect();
				speed = 0; steer = 0; sendOut(); //send out a stopped state before disconnecting
				closeSocket();
				speedGauge.setIcon(speeds[0]);
				leftInd.setIcon(directions[0]);
				rightInd.setIcon(directions[2]);
			} 
			else {

				if(axPanel.connected)
					closeSocket();
				openSocket();
			}
			c.setCon(isConnected);
		}// if connect/disconnect button in 1st pane

		else if ("update".equals(e.getActionCommand())) {
			if (isConnected)
				closeSocket();
			port = Integer.parseInt(portField.getText());
			openSocket();
		}// if update button in 2nd pane
		
		else if (e.getActionCommand().equals("toggleController")){
			if(cont != null && cont.isAlive()){
				c.removeController();
				/*try {
					Thread.currentThread();
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}*/
				while(cont.isAlive()){} // wait for thread to terminate
				if(cont.isAlive()){
					System.out.println("Error removing controller");
				}
				else
					useConButton.setText("Use Controller");
			}
			else{
				cont = new Thread(c);
				cont.start();
				useConButton.setText("Remove Controller");
			}
		}
	}

	@Override
	public void controllerAdded(ControllerEvent arg0) {
		if(!cont.isAlive()){
			cont = new Thread(c);
			cont.start();
		}
	}

	@Override
	public void controllerRemoved(ControllerEvent arg0) {
		con.removeController();
	}	
}
	