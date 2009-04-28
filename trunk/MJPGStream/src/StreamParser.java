import java.io.IOException;
import java.io.InputStream;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

/**
 * This class to parse the MJPEG stream developed from examples on sun forum thread
 * credit to Carl Gould for providing much of the methodology of parsing the MJPEG stream from the Axis Camera
 * used in the following code.
 */
public class StreamParser {
	private static final byte[] JPEG_START = new byte[] { (byte) 0xFF, (byte) 0xD8 };
	private static final int INITIAL_BUFFER_SIZE = 4096;
	
	InputStream in;
	byte[] boundary;
	byte[] segment;
	byte[] buffer;
	int cur, len;
	
	boolean canceled = false;
	
	/**
	 * Stores ChangeListeners listening for when there is a new segment to be processed
	 */
	protected EventListenerList listenerList = new EventListenerList();
	
	/**
	 *** Constructor
	 * @param in - input stream to parse
	 * @param boundary - The boundary marker for this MJPEG stream.
	 */
	public StreamParser(InputStream in, String boundary) {
		this.in = in;
		this.boundary = boundary.getBytes();
		buffer = new byte[INITIAL_BUFFER_SIZE];
		cur = 0;
		len = INITIAL_BUFFER_SIZE;
	}
	
	/**
	 * parses input stream for jpeg segments. When jpeg segment is found, all
	 * registered change listeners are notified. They can retrieve the latest segment via getSegment(). 
	 */
	public void parse(){
		int b;
		try{
		while ((b = in.read()) != -1 && !canceled) {
			append(b);
			if (checkBoundary()) {
				// We found a boundary marker. Process the segment to find the JPEG image in it
				processSegment();
				// And clear out our internal buffer.
				cur = 0;
			}
		}
		}//end try
		catch(IOException e){
			System.out.println("Error Parsing: "+e);
		}
		
	}
	
	/**
	 *** Processes the current byte buffer. Ignores the last len(BOUNDARY) bytes in the buffer. Searches through the buffer
	 * for the start of a JPEG. If a JPEG is found, the bytes comprising the JPEG are copied into the
	 * segment field. If no JPEG is found, nothing is done. ***
	 */
	protected void processSegment() {
		// First, look through the new segment for the start of a JPEG
		boolean found = false;
		int i;
		for (i = 0; i < cur - JPEG_START.length; i++) {
			if (segmentsEqual(buffer, i, JPEG_START, 0, JPEG_START.length)) {
				found = true;
				break;
			}
		}
		if (found) {
			int segLength = cur - boundary.length - i;
			segment = new byte[segLength];
			System.arraycopy(buffer, i, segment, 0, segLength);
			fireChange();
		}
	}
 
	/**
	 * @return The last JPEG segment found in the MJPEG stream.
	 */
	public byte[] getSegment() {
		return segment;
	}
	
	/**
	 *** compares sections of the two buffers that are input ***
	 * @param b1 - first buffer
	 * @param b1Start - where to start
	 * @param b2 - second buffer
	 * @param b2Start - where to start
	 * @param len - how far past start to check
	 * @return true if the sections are equal
	 * 		   false if the sections are !equal
	 */
	protected boolean segmentsEqual(byte[] b1, int b1Start, byte[] b2, int b2Start, int len) {
		if (b1Start < 0 || b2Start < 0 || b1Start + len > b1.length || b2Start + len > b2.length) {
			return false;
		} else {
			for (int i = 0; i < len; i++) {
				if (b1[b1Start + i] != b2[b2Start + i]) {
					return false;
				}
			}
			return true;
		}
	}
	
	/**
	 * @return true if if the end of the buffer matches the boundary
	 */
	protected boolean checkBoundary() {
		return segmentsEqual(buffer, cur - boundary.length, boundary, 0, boundary.length);
	}
	
	/**
	 * @return the length of the internal image buffer in bytes
	 */
	public int getBufferSize() {
		return len;
	}
	
	/**
	 *** Appends the given byte into the internal buffer. If it won't fit, buffer size is doubled ***
	 * 
	 * @param i - the byte to append onto the internal byte buffer
	 */
	protected void append(int i) {
		if (cur >= len) {
			// make buffer bigger
			byte[] newBuf = new byte[len * 2];
			System.arraycopy(buffer, 0, newBuf, 0, len);
			buffer = newBuf;
			len = len * 2;
		}
		buffer[cur++] = (byte) i;
	}
 
	public void addChangeListener(ChangeListener l) {
		listenerList.add(ChangeListener.class, l);
	}
 
	public void removeChangeListener(ChangeListener l) {
		listenerList.remove(ChangeListener.class, l);
	}
 
	public ChangeListener[] getChangeListeners() {
		return listenerList.getListeners(ChangeListener.class);
	}
 
	protected void fireChange() {
		Object[] listeners = listenerList.getListenerList();
		ChangeEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ChangeListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new ChangeEvent(this);
				((ChangeListener) listeners[i + 1]).stateChanged(e);
			}
		}
	}
 
	/**
	 *** canceled getter method ***
	 * @return true if canceled
	 * 		   false if !canceled
	 */
	public boolean isCanceled() {
		return canceled;
	}
 
	/**
	 *** canceled setter method ***
	 * @param canceled
	 */
	public void setCanceled(boolean setCan) {
		this.canceled = setCan;
		if (canceled) {
			try {
				in.close();
			} 
			catch (IOException e) {
				System.out.println("Closing input Stream: "+e);
			}
		}
	}
	
	

}//end StreamParser
