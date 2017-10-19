package mainframecontrol;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import communication.TCPClient;
import plotdisplay.PlotArea;

/**
 * Channel A Control class
 * used to control data in channel A
 * @author Michael Connor
 *
 */
public class ChannelAControl extends JPanel {
	
	//Class Essentials Objects
	private JPanel mainControl = new JPanel();
	private JPanel fxGenControl = new JPanel();
	private PlotArea plotArea;
	private MeasurementDisplay measurements;
	private TCPClient connection;
	private PlotAreaControl plotAreaControl;
	
	//Channel A Control Objects
	private boolean simulating = false;
	private boolean couplingAC = true;
	private JButton visibility = new JButton("Off");
	private JButton coupling = new JButton("AC Coupling");
	private JButton setChannelOffset = new JButton("Set");
	private JSpinner chOffset;
	private SpinnerNumberModel voltageOffset = new SpinnerNumberModel(0, -2.5, 2.5, 0.005);
	private SpinnerNumberModel voltagePerDivisionModel = new SpinnerNumberModel(0.5, 0.001, 1.0, 0.001);
	private JButton noiseGen = new JButton("Generate Noise");
	private JLabel voltagePerDivisionLabel = new JLabel ("Volts per Division");
	private JSpinner voltagePerDivision;
	private JButton setVoltagePerDivision = new JButton ("Set");
	
	//function generator Objects
	private JSpinner fxGenFrequency;
	private JSpinner p2pVoltage;
	private JSpinner fxOffset;
	private SpinnerNumberModel frequencyValue = new SpinnerNumberModel(1000, 1, 25000, 1);
	private SpinnerNumberModel vFxOffset = new SpinnerNumberModel(0, -2.5, 2.5, 0.005);
	private SpinnerNumberModel p2pVoltageModel = new SpinnerNumberModel(2, 0.2, 4.0, 0.1);
	private JButton inHouseToggle = new JButton("Software Function Generator");
	private JButton fxGeneratorOnOff = new JButton("Funtion Generator On");
	private JButton setFxParams = new JButton("Set");
	private JLabel freqeuncyLabel = new JLabel("Fx Gen Frequency (Hz)");
	private JLabel p2pVoltageLabel = new JLabel("Peak to Peak Voltage (V)");
	private JLabel fxOffsetLabel = new JLabel("Fx Gen Offset (V)");
	private JLabel chOffsetLabel = new JLabel("Channel Offset (V)");
	private JComboBox fxWaveType = new JComboBox();
	private boolean inHouseFxGen = true;

	//pass option Object
	private JButton passFilter = new JButton("Low Pass Filtering");
	
	/**
	 * Construct a Channel A control class
	 * initialise objects and adding them into the panel
	 * @param plotArea
	 * @param connection
	 * @param plotControl
	 */
	public ChannelAControl (PlotArea plotArea, TCPClient connection, PlotAreaControl plotControl) {
		this.connection = connection;
		this.plotArea = plotArea;
		this.plotAreaControl = plotControl;
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		chOffset = new JSpinner(voltageOffset);
		p2pVoltage = new JSpinner(p2pVoltageModel);
		fxOffset = new JSpinner(vFxOffset);
		fxWaveType.addItem("Sine");
		fxWaveType.addItem("Square");
		fxWaveType.addItem("Triangle");
		fxWaveType.addItem("Ramp");
		fxWaveType.addItem("Noise");
		fxGenFrequency = new JSpinner(frequencyValue);
		voltagePerDivision = new JSpinner(voltagePerDivisionModel);
		measurements = plotArea.getDisplayData().getChannelAData().getMeasurementPanel();
		mainControl.add(visibility);
		mainControl.add(coupling);
		mainControl.add(chOffsetLabel);
		mainControl.add(chOffset);
		mainControl.add(setChannelOffset);
		mainControl.add(noiseGen);
		mainControl.add(passFilter);
		fxGenControl.add(inHouseToggle);
		fxGenControl.add(fxGeneratorOnOff);
		fxGenControl.add(fxWaveType);
		fxGenControl.add(p2pVoltageLabel);
		fxGenControl.add(p2pVoltage);
		fxGenControl.add(fxOffsetLabel);
		fxGenControl.add(fxOffset);
		fxGenControl.add(freqeuncyLabel);
		fxGenControl.add(fxGenFrequency);
		fxGenControl.add(setFxParams);
		this.add(mainControl);
		this.add(fxGenControl);
		this.add(measurements);
		toggleFxGenComponents(false);
		addActionListener();
	}
	
	/**
	 * Change function to be done on software or hardware
	 */
	private void toggleInHouse () {
		if (inHouseFxGen) {
			inHouseFxGen = false;
			inHouseToggle.setText("Hardware Function Generator");
		} else {
			inHouseFxGen = true;
			inHouseToggle.setText("Software Function Generator");
		}
	}
	
	/**
	 * check if function gen is currently being done on software
	 * @return if function gen is done on hardware
	 */
	public boolean isInHouse() {
		return inHouseFxGen;
	}
	
	/**
	 * set the channel specific voltage per division to given value
	 * @param value
	 */
	public void setVoltagePerDivision (double value) {
		voltagePerDivision.setValue(value);
	}
	
	/**
	 * set the function gen waveform to the given wave
	 * @param wave
	 */
	public void setFxGenWave (String wave) {
		plotArea.getDisplayData().getChannelAData().setFxWave(wave);
		switch (wave) {
			case "Sine":
				fxWaveType.setSelectedIndex(0);
				break;
			case "Square":
				fxWaveType.setSelectedIndex(2);
				break;
			case "Ramp":
				fxWaveType.setSelectedIndex(3);
				break;
			case "Noise":
				fxWaveType.setSelectedIndex(4);
				break;
			case "Triangle":
				fxWaveType.setSelectedIndex(1);
				break;
			default:
				break;
		}
	}
	
	/**
	 * turn function gen on or off depending on value of status
	 * "0" will turn off function gen
	 * @param status
	 */
	public void setFxGenStatus (String status) {
		if (status == "0") {
			toggleFxGenComponents(false);
			plotArea.getDisplayData().getChannelAData().turnOffFxGen();
			fxGeneratorOnOff.setText("Function Generator On");
			fxGeneratorOnOff.repaint();
			
		} else {
			inHouseFxGen = false;
			toggleFxGenComponents(true);
			plotArea.getDisplayData().getChannelAData().turnOnFxGen();
			inHouseToggle.setText("Hardware Function Generator");
			fxGeneratorOnOff.setText("Function Generator Off");
			fxGeneratorOnOff.repaint();
		}
	}
	
	/**
	 * activate the function gen values based on the current value on each control object
	 */
	private void activateFxGenValue () {
		plotArea.getDisplayData().getChannelAData().setFxGenVOffset((double) fxOffset.getValue());
		plotArea.getDisplayData().getChannelAData().setPeakToPeak((double) p2pVoltage.getValue());
		plotArea.getDisplayData().getChannelAData().setFrequency((int) fxGenFrequency.getValue());
		plotArea.getDisplayData().getChannelAData().setFxWave(fxWaveType.getSelectedItem().toString());
	}
	
	/**
	 * Set the function gen values to the arguments parsed in
	 * all arguments should be in string representation of respective values
	 * this function will update the current GUI display and activate the values
	 * @param peak2peak
	 * @param offset
	 * @param frequency
	 */
	public void setFxGenValue (String peak2peak, String offset, String frequency) {
		p2pVoltage.setValue(Double.parseDouble(peak2peak));
		fxOffset.setValue(Double.parseDouble(offset));
		fxGenFrequency.setValue(Integer.parseInt(frequency));
		activateFxGenValue();
	}
	
	/**
	 * enable or disable all components on the panel except channel on/off button
	 * @param status
	 */
	private void toggleAllComponents (boolean status) {
		coupling.setEnabled(status);
		chOffset.setEnabled(status);
		setChannelOffset.setEnabled(status);
		voltagePerDivision.setEnabled(status);
		setVoltagePerDivision.setEnabled(status);
		passFilter.setEnabled(status);
		noiseGen.setEnabled(status);
		inHouseToggle.setEnabled(status);
		fxGeneratorOnOff.setEnabled(status);
		if (plotArea.getDisplayData().getChannelAData().getFxGenStatus()) {
			toggleFxGenComponents(status);
		}
	}
	
	/**
	 * enable or disable all function gen components on the panel except function gen on/off button
	 * @param status
	 */
	private void toggleFxGenComponents (boolean status) {
		fxWaveType.setEnabled(status);
		p2pVoltage.setEnabled(status);
		fxOffset.setEnabled(status);
		fxGenFrequency.setEnabled(status);
		setFxParams.setEnabled(status);
	}
	
	/**
	 * set the coupling mode to AC or DC depending on the parameter parsed in
	 * 'A' for AC and 'D' for DC coupling
	 * @param mode
	 */
	public void setCoupling (char mode) {
		if (mode == 'D') {
			coupling.setText("DC Coupling");
			couplingAC = false;
			coupling.repaint();
		} else if (mode == 'A'){
			coupling.setText("AC Coupling");
			couplingAC = true;
			coupling.repaint();
		}
	}
	
	/**
	 * Add Action Listener to all buttons and combo boxes of this panel
	 */
	private void addActionListener() {
		//voltage per division send to firmware for processing - no function in software
		setVoltagePerDivision.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				connection.sendVariablePack("ACD", Integer.valueOf(Double.valueOf(((double) voltagePerDivision.getValue()*1000)).intValue()));
			}
		});
		
		//add change pass filter control
		passFilter.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (plotArea.getDisplayData().getChannelAData().isLowPass()) {
					connection.sendMessage("ACBP");
					plotArea.getDisplayData().getChannelAData().setLowPass(false);
					passFilter.setText("Band Pass Filter");
					plotArea.getDisplayData().getChannelAData().bandPassSignal();
					plotArea.getDisplayData().applyFilter();
					plotArea.getDisplayData().applyMathExpression();
					measurements.refreshAllValues();
					plotArea.getDisplayData().getChannelBData().setSampleRate(plotArea.getDisplayData().getChannelAData().getSampleRate());
					plotAreaControl.updateSampleValue(plotArea.getDisplayData().getChannelAData().getSampleSize(), plotArea.getDisplayData().getChannelAData().getSampleRate());
				} else {
					connection.sendMessage("ACLP");
					plotArea.getDisplayData().getChannelAData().setLowPass(true);
					passFilter.setText("Low Pass Filter");
					plotArea.getDisplayData().setSampleRate(plotAreaControl.getHardwareSampleRate());
					plotAreaControl.updateSampleValue(plotArea.getDisplayData().getChannelAData().getSampleSize(), plotArea.getDisplayData().getChannelAData().getSampleRate());
				}
			}
		});
		
		//noise gen function invoked on current data to simulate data streaming receiving adc data at every 300ms
		noiseGen.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (simulating) {
					simulating = false;
					plotArea.setChATriggerAnnotation(false);
				} else {
					simulating = true;
					Thread simulation = new Thread() {
						public void run () {
							while (simulating) {
								SwingUtilities.invokeLater(new Runnable() {
									
									@Override
									public void run() {
										plotArea.getDisplayData().getChannelAData().applyNoise();
										plotArea.getDisplayData().getChannelAData().fireSeriesChanged();
										plotArea.getDisplayData().applyFilter();
										plotArea.getDisplayData().applyMathExpression();
										plotArea.setChATriggerAnnotation(false);
										plotArea.setChATriggerAnnotation(true);
										measurements.refreshAllValues();
									}
								});
								try {Thread.sleep(300);
								} catch(Exception err) {};
							}
						}
					};
					simulation.start();
				}
			}
		});
		
		//add toggle channel visibility control
		visibility.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (plotArea.getDisplayData().getChannelAData().getChannelVisibility()) {
					visibility.setText("On");
					plotArea.getDisplayData().getChannelAData().setChannelVisibility(false);
					plotArea.getDisplayData().applyFilter();
					plotArea.getDisplayData().applyMathExpression();
					plotArea.getDisplayData().refreshChannel();
					measurements.zeroAllMeasurements();
					plotArea.getChart().fireChartChanged();
					toggleAllComponents(false);
					visibility.repaint();
					if (plotArea.getDisplayData().getChannelBData().getChannelVisibility()) {
						plotAreaControl.setTrigSource("BC");
					}
					
				} else {
					visibility.setText("Off");
					plotArea.getDisplayData().getChannelAData().setChannelVisibility(true);
					plotArea.getDisplayData().applyFilter();
					plotArea.getDisplayData().applyMathExpression();
					plotArea.getDisplayData().refreshChannel();
					measurements.refreshAllValues();
					plotArea.getChart().fireChartChanged();
					toggleAllComponents(true);
					visibility.repaint();
					
				}
			}
		});
		
		//add coupling control - send packet to hardware no direct functionality on software
		coupling.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (couplingAC) {
					coupling.setText("DC Coupling");
					couplingAC = false;
					coupling.repaint();
					connection.sendMessage("ACCD");
				} else {
					coupling.setText("AC Coupling");
					couplingAC = true;
					coupling.repaint();
					connection.sendMessage("ACCA");
				}
			}
		});
		
		//add channel offset control - software controlled.... for now
		setChannelOffset.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				plotArea.getDisplayData().getChannelAData().setVerticalOffset((double) chOffset.getValue());
				plotArea.getDisplayData().applyFilter();
				plotArea.getDisplayData().applyMathExpression();
				plotArea.getDisplayData().refreshChannel();
				measurements.refreshAllValues();
				plotArea.getChart().fireChartChanged();
				connection.sendVariablePack("ACO", Integer.valueOf(Double.valueOf((plotArea.getDisplayData().getChannelAData().getVerticalOffset()*200)).intValue()));
			}
		});
		
		//add toggle inhouse function gen control - extra feature for software
		inHouseToggle.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleInHouse();
			}
		});
		
		//add function gen toggle control
		fxGeneratorOnOff.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (plotArea.getDisplayData().getChannelAData().getFxGenStatus()) {
					fxGeneratorOnOff.setText("Function Generator On");
					fxGeneratorOnOff.repaint();
					plotArea.getDisplayData().getChannelAData().clear();
					plotArea.getDisplayData().getChannelAData().turnOffFxGen();
					plotArea.getDisplayData().applyFilter();
					plotArea.getDisplayData().applyMathExpression();
					plotArea.getDisplayData().refreshChannel();
					measurements.refreshAllValues();
					plotArea.getChart().fireChartChanged();
					if (!inHouseFxGen) {
						connection.sendMessage("ACFG0");
					}
					toggleFxGenComponents(false);
				} else {
					fxGeneratorOnOff.setText("Function Generator Off");
					fxGeneratorOnOff.repaint();
					if (inHouseFxGen) {
						plotArea.getDisplayData().getChannelAData().setFxWave(fxWaveType.getSelectedItem().toString());
						plotArea.getDisplayData().getChannelAData().generateData();
						plotArea.getDisplayData().applyFilter();
						plotArea.getDisplayData().applyMathExpression();
						plotArea.getDisplayData().refreshChannel();
						measurements.refreshAllValues();
						plotArea.getChart().fireChartChanged();
					} else {
						connection.sendMessage("ACFG1");
						plotAreaControl.setTrigSource("AC");
					}
					toggleFxGenComponents(true);
				}
			}
		});
		
		//add wave type selection control
		fxWaveType.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (plotArea.getDisplayData().getChannelAData().getFxGenStatus()) {
					plotArea.getDisplayData().getChannelAData().setFxWave(fxWaveType.getSelectedItem().toString());
					if (inHouseFxGen) {
						plotArea.getDisplayData().getChannelAData().generateData();
						plotArea.getDisplayData().refreshChannel();
						measurements.refreshAllValues();
						plotArea.getChart().fireChartChanged();
					} else {	
						if (fxWaveType.getSelectedItem().toString() == "Sine") {
							connection.sendMessage("ACFWS");
						} else if (fxWaveType.getSelectedItem().toString() == "Triangle") {
							connection.sendMessage("ACFWT");
						} else if (fxWaveType.getSelectedItem().toString() == "Square") {
							connection.sendMessage("ACFWQ");
						} else if (fxWaveType.getSelectedItem().toString() == "Ramp") {
							connection.sendMessage("ACFWR");
						} else {
							connection.sendMessage("ACFWN");
						}
						plotAreaControl.setTrigSource("AC");
					}
				}
			}
		});
		
		//add set function gen parameter control
		setFxParams.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				activateFxGenValue();
				if (inHouseFxGen) {
					plotArea.getDisplayData().getChannelAData().generateData();
					plotArea.getDisplayData().applyFilter();
					plotArea.getDisplayData().applyMathExpression();
					plotArea.getDisplayData().refreshChannel();
					measurements.refreshAllValues();
					plotArea.getChart().fireChartChanged();
				} else {
					int[] values = new int[] {
							Integer.valueOf(Double.valueOf((double)p2pVoltage.getValue()*1000).intValue()),
							Integer.valueOf(Double.valueOf((double) fxOffset.getValue()*1000).intValue()),
							(int)fxGenFrequency.getValue()
					};
					connection.sendMultiVariablePack("ACFV", values);
					plotAreaControl.setTrigSource("AC");
				}
			}
		});
	}

}
