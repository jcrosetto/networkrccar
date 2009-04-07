import javax.swing.JFrame;


public class Cockpit {
	
	private JFrame jframe = new JFrame();
	private AxisCamera axPanel = new AxisCamera();
	
	public Cockpit(){
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		new Thread(axPanel).start();
		jframe.getContentPane().add(axPanel);
		jframe.setSize(320, 240);
		jframe.setTitle("Big Dog Cockpit");
		jframe.setVisible(true);
	}


	public static void main(String[] args) {
		Cockpit show = new Cockpit();
	}
}
