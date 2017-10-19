package mainframecontrol;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;

import Data.Data;
import Data.DataSet;
import communication.TCPClient;
import plotdisplay.PlotArea;

/**
 * the plot control area class
 * contains trigger control, status
 * and graph control
 * @author Michael Connor
 *
 */
@SuppressWarnings("serial")
public class PlotAreaControl extends JPanel {
	
	//Essential Objects
	private DecimalFormat df = new DecimalFormat("#.####");
	private TCPClient connection;
	private PlotArea plotArea;
	
	//Plot Area control objects
	private JLabel voltageAxis = new JLabel("Voltage Per Division (V)");
	private JLabel timeAxis = new JLabel("Time Per Division (ms)");
	private JLabel sampleLabel = new JLabel("No of Samples");
	private JLabel voltOrigin = new JLabel("Voltage Axis Origin");
	private JLabel sampleRateLabel = new JLabel("Sampling Rate (Hz)");
	private JSpinner voltagePerDivision;
	private JButton setVoltage = new JButton("Set");
	private JButton setTime = new JButton("Set");
	private JButton setSample = new JButton("Set");
	private JButton toggleBits = new JButton("12 Bits");
	private JSpinner timePerDivision;
	private JSpinner noOfSample;
	private JSpinner voltageAxisOrigin;
	private JSpinner sampleRate;
	private SpinnerNumberModel voltageOrigin = new SpinnerNumberModel(0, -5.0, 5.0, 0.01);
	private SpinnerNumberModel voltageDivision = new SpinnerNumberModel(0.5, 0.001, 5.0, 0.001);
	private SpinnerNumberModel timeDivision = new SpinnerNumberModel(2, 0.001, 500, 0.001);
	private SpinnerNumberModel sampleNumber = new SpinnerNumberModel(25000, 25000, 40000, 1);
	private SpinnerNumberModel sampleRateValue = new SpinnerNumberModel(1000000, 1, 99999999, 1);
	
	//plot status display area objects
	private JLabel sampleVoltLabel = new JLabel("Selected Sample Voltage (V): ");
	private JLabel sampleChannelLabel = new JLabel("Selected Sample in: ");
	private JLabel hardwareSampleRateLabel = new JLabel("Hardware Sampling Rate (Hz): ");
	private JLabel sampleTimeLabel = new JLabel("Selected Sample at time (ms): ");
	private JTextField hardwareSampleRate = new JTextField("1000000");
	private JTextField sampleTime = new JTextField();
	private JTextField sampleVoltage = new JTextField();
	private JTextField sampleChannel = new JTextField();
	
	//trigger control and display objects and variable
	private String trigStatus = "Armed";
	private String trigMode = "Auto";
	private String trigType = "Rising";
	private JLabel triggerModeLabel = new JLabel("Trigger Mode");
	private JLabel triggerTypeLabel = new JLabel("Trigger Type");
	private JLabel triggerThresholdLabel = new JLabel("Trigger Threshold (V)");
	private JComboBox triggerMode = new JComboBox();
	private JComboBox triggerType = new JComboBox();
	private JSpinner triggerThreshold;
	private SpinnerNumberModel threshold = new SpinnerNumberModel(0, -5.000, 5.000, 0.005);
	private JButton toggleSourceChannel = new JButton("Channel A");
	private JButton setTrigThreshold = new JButton("Set");
	private JButton forceTrigger = new JButton("Force Trigger");
	private JButton rearmTrigger = new JButton("Re-arm Trigger");
	private JLabel statusDisplay;
	private int mode = 0;
	private boolean singleMode = false;
	private boolean triggered = false;
	private boolean trigSourceA = true;
	
	//resolution tracking variable
	private String bits = "0";
	
	/**
	 * construct a plot area control
	 * initialize objects
	 * @param plotArea
	 * @param connection
	 */
	public PlotAreaControl (PlotArea plotArea, TCPClient connection) {
		this.connection = connection;
		this.plotArea = plotArea;
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		df.setRoundingMode(RoundingMode.CEILING);
		triggerMode.addItem("Auto");
		triggerMode.addItem("Normal");
		triggerMode.addItem("Single");
		triggerType.addItem("Rising");
		triggerType.addItem("Falling");
		triggerType.addItem("Level");
		triggerThreshold = new JSpinner(threshold);
		((JSpinner.DefaultEditor)triggerThreshold.getEditor()).getTextField().setColumns(3);
		statusDisplay = new JLabel("Trigger Mode - " + trigMode + " | " + 
				"Trigger Type - " + trigType + " | " + 
						"Trigger Threshold - " + triggerThreshold.getValue().toString() + " | Armed");
		rearmTrigger.setEnabled(false);
		setUpSpinners();
		addActionListener();
		addItems();
	}
	
	/**
	 * add items into the control area categorizing them from top to bottom
	 */
	private void addItems () {
		JPanel control = new JPanel();
		JPanel trigControl = new JPanel();
		JPanel trigStatus = new JPanel();
		JPanel display = new JPanel();
		control.add(sampleLabel);
		control.add(noOfSample);
		control.add(sampleRateLabel);
		control.add(sampleRate);
		control.add(setSample);
		control.add(toggleBits);
		control.add(voltOrigin);
		control.add(voltageAxisOrigin);
		control.add(voltageAxis);
		control.add(voltagePerDivision);
		control.add(setVoltage);
		control.add(timeAxis);
		control.add(timePerDivision);
		control.add(setTime);
		trigControl.add(toggleSourceChannel);
		trigControl.add(triggerModeLabel);
		trigControl.add(triggerMode);
		trigControl.add(triggerTypeLabel);
		trigControl.add(triggerType);
		trigControl.add(triggerThresholdLabel);
		trigControl.add(triggerThreshold);
		trigControl.add(setTrigThreshold);
		trigControl.add(forceTrigger);
		trigControl.add(rearmTrigger);
		trigStatus.add(statusDisplay);
		sampleVoltage.setColumns(5);
		sampleChannel.setColumns(15);
		hardwareSampleRate.setColumns(5);
		hardwareSampleRate.setEditable(false);
		sampleVoltage.setEditable(false);
		sampleChannel.setEditable(false);
		sampleTime.setColumns(5);
		sampleTime.setEditable(false);
		display.add(hardwareSampleRateLabel);
		display.add(hardwareSampleRate);
		display.add(sampleTimeLabel);
		display.add(sampleTime);
		display.add(sampleVoltLabel);
		display.add(sampleVoltage);
		display.add(sampleChannelLabel);
		display.add(sampleChannel);
		this.add(control);
		this.add(display);
		this.add(trigControl);
		this.add(trigStatus);		
	}
	
	/**
	 * setup spinners and assign them with the correct spinner model
	 */
	private void setUpSpinners () {
		voltageAxisOrigin = new JSpinner(voltageOrigin);
		voltagePerDivision = new JSpinner(voltageDivision);
		timePerDivision = new JSpinner(timeDivision);
		noOfSample = new JSpinner(sampleNumber);
		sampleRate = new JSpinner(sampleRateValue);
	}
	
	/**
	 * toggle minimum sample size based on current resolution
	 * reset current sample size to the new minimum sample size
	 */
	public void setMinSampleSize () {
		if (!plotArea.isEightBits()) {
			sampleNumber.setValue(25000);
			sampleNumber.setMinimum(25000);
		} else {
			sampleNumber.setValue(50000);
			sampleNumber.setMinimum(50000);
		}
		setSamples();
	}
	
	/**
	 * set the maximum sample size to the parsed in parameter
	 * @param maxSampleSize
	 */
	public void setMaxSampleSize (int maxSampleSize) {
		sampleNumber.setMaximum(maxSampleSize);
	}
	
	/**
	 * set the display for hardware sample rate to the new rate
	 * @param rate
	 */
	public void setHardwareSample (int rate) {
		hardwareSampleRate.setText(Integer.toString(rate));
	}
	
	/**
	 * return the current hardware sample rate
	 * @return
	 */
	public int getHardwareSampleRate () {
		return Integer.parseInt(hardwareSampleRate.getText());
	}
	
	/**
	 * toggle the current resolution between 8 and 12 bits
	 * this function will change the sample size to acquire
	 */
	public void toggleBits () {
		if (plotArea.isEightBits()) {
			plotArea.enableEightBits(false);
			toggleBits.setText("12 Bits");
			toggleBits.repaint();
			bits = "0";
			setMaxSampleSize(40000);
		} else {
			plotArea.enableEightBits(true);
			toggleBits.setText("08 Bits");
			toggleBits.repaint();
			bits = "1";
			setMaxSampleSize(80000);
		}
		setMinSampleSize();
	}
	
	/**
	 * set the current spinner values from the respective spinners as the sampling size and rate
	 */
	public void setSamples () {
		plotArea.setSampleSize((int) noOfSample.getValue(), (int) sampleRate.getValue());
		plotArea.getChartPanel().repaint();
		if ((int) sampleRate.getValue()<=1000000) {
			setHardwareSample((int) sampleRate.getValue());
		} else {
			setHardwareSample(1000000);
		}
	}
	
	/**
	 * update the spinner value of sample size and rate to the parsed in values
	 * @param sampleValue
	 * @param rateValue
	 */
	public void updateSampleValue (int sampleValue, int rateValue) {
		noOfSample.setValue(sampleValue);
		sampleRate.setValue(rateValue);
		this.repaint();
	}
	
	/**
	 * set time per division to what is currently on the timePerDivision spinner value
	 * this function will affect the visible data so measurement values will update
	 */
	public void setTimePerDivision () {
		plotArea.setTimeDivision((double) timePerDivision.getValue());
		plotArea.getChartPanel().repaint();
		plotArea.getDisplayData().refreshAllChannelMeasurement();
	}
	
	/**
	 * set volts per division to what is currently on the timePerDivision spinner value
	 */
	public void setVoltPerDivision () {
		plotArea.setVoltageDivision((double) voltagePerDivision.getValue(), (double) voltageAxisOrigin.getValue());
		plotArea.getChartPanel().repaint();
	}
	
	/**
	 * updates the voltagePerDivision spinner with the parsed in value then set it
	 * @param value
	 */
	public void updateVoltageValue (double value) {
		voltagePerDivision.setValue(value);
		this.repaint();
		setVoltPerDivision();
	}
	
	/**
	 * updates the timePerDivision spinner with the parsed in value then set it
	 * @param value
	 */
	public void updateTimeValue (double value) {
		timePerDivision.setValue(value);
		this.repaint();
		setTimePerDivision();
	}
	
	/**
	 * change the source channel to depending are parsed in value
	 * "BC" will change source channel to B and anything else will change to channel A
	 * @param channel
	 */
	public void setTrigSource (String channel) {
		if (channel == "BC") {
			trigSourceA = false;
			toggleSourceChannel.setText("Channel B");
		} else {
			trigSourceA = true;
			toggleSourceChannel.setText("Channel A");
		}
		connection.sendMessage((channel + 'S'));
	}
	
	/**
	 * toggle the trigger source between channel A and channel B
	 */
	private void toggleTrigSource () {
		if (trigSourceA) {
			trigSourceA = false;
			toggleSourceChannel.setText("Channel B");
			connection.sendMessage("BCS");
		} else {
			trigSourceA = true;
			toggleSourceChannel.setText("Channel A");
			connection.sendMessage("ACS");
		}
		
	}
	
	/**
	 * set trigger mode to the parameter mode
	 * @param mode
	 */
	public void setTrigMode (String mode) {
		trigMode = mode;
		switch (trigMode) {
			case "Auto":
				triggerMode.setSelectedIndex(0);
				singleMode = false;
				rearmTrigger.setEnabled(false);
				break;
			case "Normal":
				triggerMode.setSelectedIndex(1);
				singleMode = false;
				rearmTrigger.setEnabled(false);
				break;
			case "Single":
				triggerMode.setSelectedIndex(2);
				singleMode = true;
				rearmTrigger.setEnabled(true);
				break;
			default:
				break;
		}
		updateStatus();
	}
	
	/**
	 * set trigger type to the parameter type
	 * @param type
	 */
	public void setTrigType (String type) {
		trigType = type;
		switch (trigType) {
			case "Rising":
				triggerType.setSelectedIndex(0);
				break;
			case "Falling":
				triggerType.setSelectedIndex(1);
				break;
			case "Level":
				triggerType.setSelectedIndex(2);
				break;
			default:
				break;
		}
		updateStatus();
	}
	
	/**
	 * set trigger threshold to the parameter threshold
	 * @param threshold
	 */
	public void setTrigThreshold (double threshold) {
		triggerThreshold.setValue(threshold);
		triggerThreshold.repaint();
	}
	
	/**
	 * update the status display with the current trigger status
	 */
	private void updateStatus () {
		statusDisplay.setText("Trigger Mode - " + trigMode + " | " + 
				"Trigger Type - " + trigType + " | " + 
				"Trigger Threshold - " + triggerThreshold.getValue().toString() + " | " + trigStatus);
		statusDisplay.repaint();
	}
	
	/**
	 * set trigger status to the parameter status
	 * value may differ to the set status depending on current trigger mode
	 * @param status
	 */
	public void setTrigStatus (String status) {
		trigStatus = status;
		switch (trigStatus) {
			case "Stopped":
				break;
			case "Armed":
				triggered = false;
				break;
			case "Triggered":
				triggered = true;
				if (trigMode == "Single") {
					trigStatus = "Stopped";
				}
				break;
			default:
				break;
		}
		updateStatus();
	}
	
	/**
	 * add functions to all buttons in control panel
	 */
	private void addActionListener () {
		
		//Data point selection to display command
		plotArea.getChartPanel().addChartMouseListener(new ChartMouseListener() {
			
			@Override
			public void chartMouseMoved(ChartMouseEvent arg0) {
			}
			
			@Override
			public void chartMouseClicked(ChartMouseEvent event) {
				ChartEntity entity = event.getEntity();
				
				if (entity == null) {
					return;
				} else {
					try {
						String tooltip = ((XYItemEntity)entity).getToolTipText();
						XYDataset dataset = ((XYItemEntity)entity).getDataset();
						int seriesIndex = ((XYItemEntity)entity).getSeriesIndex();
						int item = ((XYItemEntity)entity).getItem();
						
						Data series = (Data) ((DataSet)dataset).getSeries(seriesIndex);
						XYDataItem data = series.getDataItem(item);
						
						
						sampleVoltage.setText(df.format(data.getYValue()));
						sampleTime.setText(df.format(data.getXValue()));
						
						if (seriesIndex == 0) {
							sampleChannel.setText("Channel A");
						} else if (seriesIndex == 1) {
							sampleChannel.setText("Channel B");
						} else if (seriesIndex == 2) {
							sampleChannel.setText("Filter Channel");
						} else {
							sampleChannel.setText("Math Channel");
						}
					} catch (Exception selectionError) {
						JOptionPane.showMessageDialog(null, "Please click on a valid data point on the graph!", 
    							"Unable to detect valid data", JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		});
		
		//toggle resolution command
		toggleBits.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleBits();
				connection.sendMessage(bits + "R");
			}
		});
		
		//set voltage per division and voltage origin
		setVoltage.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setVoltPerDivision();
			}
		});
		
		//set time per division and send value to firmware
		setTime.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setTimePerDivision();
				connection.sendVariablePack("T", Integer.valueOf(Double.valueOf((double)timePerDivision.getValue()*1000).intValue()));
			}
		});
		
		//set sample size and sample rate and send value to firmware
		setSample.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setSamples();
				int[] values = new int[] {
						(int)noOfSample.getValue(),
						(int)sampleRate.getValue()
				};
				connection.sendMultiVariablePack("S", values);
			}
		});
		
		//select trigger mode and send value to firmware
		triggerMode.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (trigMode == triggerMode.getSelectedItem().toString()) {
					return;
				}
				setTrigStatus("Armed");
				setTrigMode(triggerMode.getSelectedItem().toString());
				if (triggerMode.getSelectedItem().toString() == "Normal") {
					if (trigSourceA) {
						connection.sendMessage("ACTMN");
					} else {
						connection.sendMessage("BCTMN");
					}
				} else if (triggerMode.getSelectedItem().toString() == "Single") {
					if (trigSourceA) {
						connection.sendMessage("ACTMS");
					} else {
						connection.sendMessage("BCTMS");
					}
				} else {
					if (trigSourceA) {
						connection.sendMessage("ACTMA");
					} else {
						connection.sendMessage("BCTMA");
					}
				}
			}
		});
		
		//set trigger threshold to the value of the spinner and send it to firmware
		setTrigThreshold.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (trigStatus != "Stopped") {
					setTrigStatus("Armed");
				}
				if (trigSourceA) {
					connection.sendVariablePack("ACTH", Double.valueOf((double)triggerThreshold.getValue()*1000).intValue());
				} else {
					connection.sendVariablePack("BCTH", Double.valueOf((double)triggerThreshold.getValue()*1000).intValue());
				}
				
			}
		});
		
		//force the trigger and send packet to firmware
		forceTrigger.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setTrigStatus("Triggered");
				if (trigSourceA) {
					connection.sendMessage("ACTF");
				} else {
					connection.sendMessage("BCTF");
				}
			}
		});
		
		//send the selected trigger type to firmware
		triggerType.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setTrigType(triggerType.getSelectedItem().toString());
				if (triggerType.getSelectedItem().toString() == "Falling") {
					if (trigSourceA) {
						connection.sendMessage("ACTTF");
					} else {
						connection.sendMessage("BCTTF");
					}
				} else if (triggerType.getSelectedItem().toString() == "Level") {
					if (trigSourceA) {
						connection.sendMessage("ACTTL");
					} else {
						connection.sendMessage("BCTTL");
					}
				} else {
					if (trigSourceA) {
						connection.sendMessage("ACTTR");
					} else {
						connection.sendMessage("BCTTR");
					}	
				}
				if (trigStatus != "Stopped") {
					setTrigStatus("Armed");
				}
			}
		});
		
		//rearm trigger and send message to firmware
		rearmTrigger.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setTrigStatus("Armed");
				if (trigSourceA) {
					connection.sendMessage("ACTR");
				} else {
					connection.sendMessage("BCTR");
				}
			}
		});
		
		//toggle source channel command
		toggleSourceChannel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleTrigSource();
				if (trigStatus != "Stopped") {
					setTrigStatus("Armed");
				}
			}
		});
	}
}
