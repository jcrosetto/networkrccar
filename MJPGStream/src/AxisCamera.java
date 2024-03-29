import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import sun.misc.BASE64Encoder;


/**
 * Notes: resource from java.sun.com forum thread
 * http://forum.java.sun.com/thread.jspa?threadID=494920&start=15&tstart=0
 * and Carl Gould's explanation and submission of code at the same source
 * 
 * our camera is set to not allow anonymous viewing so a username and password are hard-coded in and required
 * in our case, we have created a user, 'demo' with minimal privileges to allow viewing
 * 
 * Seth Schwiethale
 *
 */
public class AxisCamera extends JComponent implements Runnable, ChangeListener {

	/*
	 *** set up mjpeg stream's url and login info ***
	 */
	//public String hostName = "127.0.0.1:2000";
	//public String hostName = "mcltcam.plu.edu";
	public String hostName = "152.117.205.34"; //will use this as URL for server connection in Cockpit also
	public String mjpegStream = "/axis-cgi/mjpg/video.cgi?resolution=320x240"; //this will be changable in Cockpit GUI
	private String fullURL;
	String user = "demo";
	String pass = "demo";
	String base64authorization = null; //initialized to null in case user and pass are null, we know they're not now.

	/*
	 *** various images and booleans *** 
	 */
	private File noFeedImage = new File("noFeed.png");
	private Image image = null;
	boolean connected = false;
	private boolean initCompleted = false;
	boolean killFeed;

	//connect to camera for video feed with this
	HttpURLConnection huc = null;

	//for updating when a complete image is found
	Runnable updater;

	//used to calculate average framerate
	long[] fRates = new long[10];

	//instance of StreamParser used to get JPEG segments
	StreamParser parser; 

	//default constructor for startup with hardcoded addresses and stream source
	public AxisCamera() {
		killFeed = false;
		// only use authorization if all informations are available
		if (user != null && pass != null) {
			base64authorization = encode(user, pass);
		}
		setForeground(Color.WHITE);
		setFont(new Font("Dialog", Font.BOLD, 12));

	}//end constructor
	
	//overloaded constructor
	public AxisCamera(String gotHost, String gotStream, String gotUser, String gotPass) {
		killFeed = false;
		hostName = gotHost; //will use this as URL for server connection in Cockpit also
		mjpegStream = gotStream; //this will be changable in Cockpit GUI
		user = gotUser;
		pass = gotPass;
		// only use authorization if all informations are available
		if (user != null && pass != null) {
			base64authorization = encode(user, pass);
		}
		setForeground(Color.WHITE);
		setFont(new Font("Dialog", Font.BOLD, 12));

	}//end constructor


	/*** encodes user and pass in Base64-encoding ***
	 * @param usernm
	 * @param passwd
	 ***
	 */
	private String encode(String usernm, String passwd) {
		String s = usernm + ":" + passwd;
		String encs = (new BASE64Encoder()).encode(s.getBytes());
		return "Basic " + encs;
	}

	/**
	 *** set up the display ***
	 */
	private void initDisplay() {

		if (image != null) {
			//set preferred size of the image
			Dimension imageSize = new Dimension(360, 240);//image.getWidth(this)
			setPreferredSize(imageSize);
			SwingUtilities.getWindowAncestor(this).pack();
			initCompleted = true;
		}	
	}//end initDisplay


	/**
	 *** method to start updater, create url connection, create parser ***
	 */
	public void connect() {
		try {

			/**
			 * See the stateChanged() function
			 */
			updater = new Runnable(){

				public void run() {
					if (!initCompleted) {
						initDisplay();
					}
					repaint();
					for (int i = 0; i < fRates.length - 1; i++) {
						fRates[i] = fRates[i + 1];
					}
					fRates[fRates.length - 1] = System.currentTimeMillis();	

				}//end run
			};//new Runnable

			fullURL = "http://"+hostName+mjpegStream; //hostName and mjpegStream are changable in GUI
			URL u = new URL(fullURL);
			huc = (HttpURLConnection) u.openConnection();

			// if authorization is required set up the connection with the encoded authorization-information
			if (base64authorization != null) {
				huc.setDoInput(true);
				huc.setRequestProperty("Authorization", base64authorization);
				huc.connect();
			}

			/*
			 * resource: Carl Gould
			 * the boundary used by the Axis 207w
			 */
			String boundary = "--myboundary"; //from VAPIX_3_HTTP_API_3_00.pdf section 5.2.4.4
			String contentType = huc.getContentType();
			Pattern pattern = Pattern.compile("boundary=(.*)$");
			Matcher matcher = pattern.matcher(contentType); //Creates a matcher the will match content type against pattern
			try {
				matcher.find();
				boundary = matcher.group(1); //returns subsequence captured by given group by previous match operation
			} catch (Exception e) {
				System.out.println("Error Matching Pattern: "+e);
			}

			InputStream is = huc.getInputStream(); //input stream of bytes from HttpURLConnection
			connected = true;
			parser = new StreamParser(is, boundary); //new instance for MJPEGParser
			parser.addChangeListener(this); //add listener to parser
		} 
		// in case no connection exists wait and try again, instead of printing the error
		catch (IOException e) { 
			try {
				huc.disconnect();
				Thread.sleep(60);
			} catch (InterruptedException ie) {
				huc.disconnect();
			}
			connect(); //jump back up to the top and try again
		} 
		catch (Exception e) {
			System.out.println("oops: "+e); //another exception is caught...
		}
	}//end connect

	/**
	 *** stop updater, stop parser ***
	 */
	public void disconnect() {
		try {
			if (connected) {
				updater = null;
				connected = false;
				parser.canceled = true;
				System.out.println("Parser Canceled in disconnect: "+parser.canceled);
			}
		} catch (Exception e) {
			System.out.println("Error disconnecting: "+e);
		}
	}//end disconnect

	NumberFormat decFormat = DecimalFormat.getNumberInstance(); //general-purpose number in decimal form

	/**
	 *** used to set the image on the panel ***
	 */
	public void paintComponent(Graphics g) { 
		super.paintComponent(g);
		int center = (getWidth()/2-160);
		if (image != null) {
			g.drawImage(image, center, 0, this);
		}

		/* Get and display the frame rate -- disabled for regular use ***
		long timeBetweenFrames = 0;
		for (int i = 0; i < fRates.length - 1; i++) {
			timeBetweenFrames += fRates[i + 1] - fRates[i];
		}
		timeBetweenFrames /= fRates.length - 1;
		g.setColor(getForeground());
		g.setFont(getFont());
		if (timeBetweenFrames > 0) {
			String fps = decFormat.format(1000.0 / timeBetweenFrames) + " fps";
			g.drawString(fps, 0, g.getFontMetrics().getHeight());
		}*/
	}

	/**
	 *** Listener of parser ***
	 */
	public void stateChanged(ChangeEvent e) {
		byte[] segment = parser.getSegment();
		if (segment.length > 0) {
			try {
				image = ImageIO.read(new ByteArrayInputStream(segment));
				EventQueue.invokeLater(updater);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}//end stateChanged

	public void run() {
		//System.out.println("RUN is happening!"); test print
		connect();
		System.out.println("after connect;");
		if(connected){
			parser.canceled = false;
			parser.parse();
		}
		// kill the thread if connection is lost
		if(killFeed){
			//System.out.println("KILL RUN"); test print
			huc.disconnect();
			parser.removeChangeListener(this);
			image = null;
			return;
		}
	}


}//end Class
