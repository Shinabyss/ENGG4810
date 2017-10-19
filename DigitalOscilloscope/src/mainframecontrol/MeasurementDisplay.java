package mainframecontrol;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import Data.Data;

/**
 * Measurement display panel for storing calculated measurements
 * for each channel
 * @author Michael Connor
 *
 */
public class MeasurementDisplay extends JPanel {
	
	//Essential objects and variables
	private Data data;
	private JLabel maxVoltage = new JLabel("Maximum Voltage (V): ");
	private JLabel minVoltage = new JLabel("Minimum Voltage (V): ");
	private JLabel p2pVoltage = new JLabel("Maximum Peak-to-Peak Voltage (V): ");
	private JLabel aveVoltage = new JLabel("Average Voltage (V): ");
	private JLabel stdVoltage = new JLabel("Standard Deviation of Voltage: ");
	private JLabel frequency = new JLabel("Frequency (Hz): ");
	private JTextField maxVoltValue = new JTextField();
	private JTextField minVoltValue = new JTextField();
	private JTextField p2pVoltValue = new JTextField();
	private JTextField aveVoltValue = new JTextField();
	private JTextField stdVoltValue = new JTextField();
	private JTextField frequencyValue = new JTextField();
	
	/**
	 * construct the measurement panel and initialize the objects
	 * @param Channel
	 */
	public MeasurementDisplay (Data Channel) {
		this.data = Channel;
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		JPanel top = new JPanel();
		JPanel bottom = new JPanel();
		maxVoltValue.setColumns(5);
		minVoltValue.setColumns(5);
		p2pVoltValue.setColumns(5);
		aveVoltValue.setColumns(5);
		stdVoltValue.setColumns(5);
		frequencyValue.setColumns(5);
		maxVoltValue.setEditable(false);
		minVoltValue.setEditable(false);
		p2pVoltValue.setEditable(false);
		aveVoltValue.setEditable(false);
		stdVoltValue.setEditable(false);
		frequencyValue.setEditable(false);
		zeroAllMeasurements();
		top.add(maxVoltage);
		top.add(maxVoltValue);
		top.add(minVoltage);
		top.add(minVoltValue);
		top.add(p2pVoltage);
		top.add(p2pVoltValue);
		bottom.add(aveVoltage);
		bottom.add(aveVoltValue);
		bottom.add(stdVoltage);
		bottom.add(stdVoltValue);
		bottom.add(frequency);
		bottom.add(frequencyValue);
		this.add(top);
		this.add(bottom);
		
	}
	
	/**
	 * zero all measurement values
	 */
	public void zeroAllMeasurements() {
		data.zeroAllMeasurement();
		maxVoltValue.setText(data.getmMaxVoltage());
		minVoltValue.setText(data.getmMinVoltage());
		p2pVoltValue.setText(data.getmP2PVoltage());
		aveVoltValue.setText(data.getmAveVoltage());
		stdVoltValue.setText(data.getmStdVoltage());
		frequencyValue.setText(data.getmFrquency());
		this.revalidate();
		this.repaint();
	}
	
	/**
	 * refresh all measurement values using public function
	 * from data class
	 */
	public void refreshAllValues () {
		data.refreshAllMeasurements();
		maxVoltValue.setText(data.getmMaxVoltage());
		minVoltValue.setText(data.getmMinVoltage());
		p2pVoltValue.setText(data.getmP2PVoltage());
		aveVoltValue.setText(data.getmAveVoltage());
		stdVoltValue.setText(data.getmStdVoltage());
		frequencyValue.setText(data.getmFrquency());
		this.revalidate();
		this.repaint();
	}

}
