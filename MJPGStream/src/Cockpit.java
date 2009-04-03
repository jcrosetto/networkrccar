import javax.swing.JFrame;


public class Cockpit {

	public static void main(String[] args) {
		JFrame jframe = new JFrame();
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		AxisCamera axPanel = new AxisCamera();
		new Thread(axPanel).start();
		jframe.getContentPane().add(axPanel);
		jframe.setSize(320, 240);
		jframe.setTitle("Big Dog Cockpit");
		jframe.setVisible(true);
	}
}
