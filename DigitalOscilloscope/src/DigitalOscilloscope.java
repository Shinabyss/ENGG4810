import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import GUI.*;
import communication.TCPClient;

public class DigitalOscilloscope {
	
	/**
	 * @author Michael Connor
	 * credits to eichelbe for providing opensource library 
	 * Start DIGIScope GUI
	 * @param args
	 */
	public static void main(String[] args) {
		
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				
				DigitalOscilloscopeGUI digitalOscilloscope = new DigitalOscilloscopeGUI();
				digitalOscilloscope.setVisible(true);
				digitalOscilloscope.pack();
				digitalOscilloscope.repaint();
			}
			
			
		});
	}

}
