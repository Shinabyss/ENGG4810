package mainframecontrol;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.sun.org.apache.xerces.internal.xs.PSVIProvider;

import communication.TCPClient;
import plotdisplay.PlotArea;

public class ChannelBControl extends JPanel {
	
	//Class Essential Objects
	private JPanel mainControl = new JPanel();
	private JPanel fxGenControl = new JPanel();
	private PlotArea plotArea;
	private MeasurementDisplay measurements;
	private TCPClient connection;
	private PlotAreaControl plotAreaControl;
	
	//Channel B Control Objects
	private boolean couplingAC = true;
	private JButton noiseGen = new JButton("Generate Noise");
	private boolean simulating = false;
	private JButton visibility = new JButton("Off");
	private JButton coupling = new JButton("AC Coupling");
	private JLabel voltagePerDivisionLabel = new JLabel ("Volts per Division");
	private JSpinner voltagePerDivision;
	private JButton setVoltagePerDivision = new JButton ("Set");
	private SpinnerNumberModel voltageOffset = new SpinnerNumberModel(0, -2.5, 2.5, 0.005);
	private JLabel chOffsetLabel = new JLabel("Channel Offset (V)");
	private JButton setChannelOffset = new JButton("Set");
	private JSpinner chOffset;
	
	//function generator Objects
	private JButton fxGeneratorOnOff = new JButton("Funtion Generator On");
	private JButton setFxParams = new JButton("Set");
	private JButton inHouseToggle = new JButton("Software Function Generator");
	private JLabel p2pVoltageLabel = new JLabel("Peak to Peak Voltage (V)");
	private JLabel fxOffsetLabel = new JLabel("Fx Gen Offset (V)");
	private JLabel freqeuncyLabel = new JLabel("Fx Gen Frequency (Hz)");
	private JComboBox fxWaveType = new JComboBox();
	private JSpinner p2pVoltage;
	private JSpinner fxOffset;
	private JSpinner fxGenFrequency;
	private SpinnerNumberModel vFxOffset = new SpinnerNumberModel(0, -2.5, 2.5, 0.005);
	private SpinnerNumberModel p2pVoltageModel = new SpinnerNumberModel(2, 0.2, 4.0, 0.1);
	private SpinnerNumberModel voltagePerDivisionModel = new SpinnerNumberModel(0.5, 0.001, 1.0, 0.001);
	private SpinnerNumberModel frequencyValue = new SpinnerNumberModel(1000, 1, 25000, 1);
	private boolean inHouseFxGen = true;
	
	/**
	 * Construct a Channel B control class
	 * initialise objects and adding them into the panel
	 * @param plotArea
	 * @param connection
	 * @param plotControl
	 */
	public ChannelBControl (PlotArea plotArea, TCPClient connection, PlotAreaControl plotControl) {
		this.connection = connection;
		this.plotArea = plotArea;
		this.plotAreaControl = plotControl;
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		chOffset = new JSpinner(voltageOffset);
		p2pVoltage = new JSpinner(p2pVoltageModel);
		fxOffset = new JSpinner(vFxOffset);
		fxGenFrequency = new JSpinner(frequencyValue);
		fxWaveType.addItem("Sine");
		fxWaveType.addItem("Square");
		fxWaveType.addItem("Triangle");
		fxWaveType.addItem("Ramp");
		fxWaveType.addItem("Noise");
		voltagePerDivision = new JSpinner(voltagePerDivisionModel);
		measurements = plotArea.getDisplayData().getChannelBData().getMeasurementPanel();
		mainControl.add(visibility);
		mainControl.add(coupling);
		mainControl.add(chOffsetLabel);
		mainControl.add(chOffset);
		mainControl.add(setChannelOffset);
		mainControl.add(noiseGen);
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
	 * isolates the function gen control commands from hardware
	 */
	public void toggleInHouse () {
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
		plotArea.getDisplayData().getChannelBData().setFxWave(wave);
		switch (wave) {
			case "Sine":
				fxWaveType.setSelectedIndex(0);
				break;
			case "Square":
				fxWaveType.setSelectedIndex(1);
				break;
			case "Ramp":
				fxWaveType.setSelectedIndex(3);
				break;
			case "Noise":
				fxWaveType.setSelectedIndex(4);
				break;
			case "Triangle":
				fxWaveType.setSelectedIndex(2);
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
			fxGeneratorOnOff.setText("Function Generator On");
			fxGeneratorOnOff.repaint();
			
		} else {
			inHouseFxGen = false;
			toggleFxGenComponents(true);
			inHouseToggle.setText("Hardware Function Generator");
			fxGeneratorOnOff.setText("Function Generator Off");
			fxGeneratorOnOff.repaint();
		}
	}
	
	/**
	 * activate the function gen values based on the current value on each control object
	 */
	private void activateFxGenValue () {
		plotArea.getDisplayData().getChannelBData().setFxGenVOffset((double) fxOffset.getValue());
		plotArea.getDisplayData().getChannelBData().setPeakToPeak((double) p2pVoltage.getValue());
		plotArea.getDisplayData().getChannelBData().setFrequency((int) fxGenFrequency.getValue());
		plotArea.getDisplayData().getChannelBData().setFxWave(fxWaveType.getSelectedItem().toString());
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
		noiseGen.setEnabled(status);
		inHouseToggle.setEnabled(status);
		fxGeneratorOnOff.setEnabled(status);
		if (plotArea.getDisplayData().getChannelBData().getFxGenStatus()) {
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
				connection.sendVariablePack("BCD", Integer.valueOf(Double.valueOf(((double) voltagePerDivision.getValue()*1000)).intValue()));
			}
		});
		
		//noise gen function invoked on current data to simulate data streaming receiving adc data at every 300ms
		noiseGen.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (simulating) {
					simulating = false;
					plotArea.setChBTriggerAnnotation(false);
				} else {
					simulating = true;
					Thread simulation = new Thread() {
						public void run () {
							while (simulating) {
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										plotArea.getDisplayData().getChannelBData().applyNoise();
										plotArea.getDisplayData().getChannelBData().fireSeriesChanged();
										plotArea.getDisplayData().applyFilter();
										plotArea.getDisplayData().applyMathExpression();
										plotArea.setChBTriggerAnnotation(false);
										plotArea.setChBTriggerAnnotation(true);
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
				if (plotArea.getDisplayData().getChannelBData().getChannelVisibility()) {
					visibility.setText("On");
					plotArea.getDisplayData().getChannelBData().setChannelVisibility(false);
					plotArea.getDisplayData().applyFilter();
					plotArea.getDisplayData().applyMathExpression();
					plotArea.getDisplayData().refreshChannel();
					measurements.zeroAllMeasurements();
					plotArea.getChart().fireChartChanged();
					toggleAllComponents(false);
					visibility.repaint();
					if (plotArea.getDisplayData().getChannelAData().getChannelVisibility()) {
						plotAreaControl.setTrigSource("AC");
					}
				} else {
					visibility.setText("Off");
					plotArea.getDisplayData().getChannelBData().setChannelVisibility(true);
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
					connection.sendMessage("BCCD");
				} else {
					coupling.setText("AC Coupling");
					couplingAC = true;
					coupling.repaint();
					connection.sendMessage("BCCA");
				}
			}
		});
		
		//add channel offset control - software controlled.... for now
		setChannelOffset.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				plotArea.getDisplayData().getChannelBData().setVerticalOffset((double) chOffset.getValue());
				plotArea.getDisplayData().applyFilter();
				plotArea.getDisplayData().applyMathExpression();
				plotArea.getDisplayData().refreshChannel();
				measurements.refreshAllValues();
				plotArea.getChart().fireChartChanged();
				connection.sendVariablePack("BCO", Integer.valueOf(Double.valueOf((plotArea.getDisplayData().getChannelBData().getVerticalOffset()*200)).intValue()));
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
				if (plotArea.getDisplayData().getChannelBData().getFxGenStatus()) {
					fxGeneratorOnOff.setText("Function Generator On");
					fxGeneratorOnOff.repaint();
					plotArea.getDisplayData().getChannelBData().clear();
					plotArea.getDisplayData().getChannelBData().turnOffFxGen();
					plotArea.getDisplayData().refreshChannel();
					measurements.refreshAllValues();
					plotArea.getChart().fireChartChanged();
					toggleFxGenComponents(false);
					if (!inHouseFxGen) {
						connection.sendMessage("BCFG0");
					}
				} else {
					fxGeneratorOnOff.setText("Function Generator Off");
					fxGeneratorOnOff.repaint();
					plotArea.getDisplayData().getChannelBData().setFxWave(fxWaveType.getSelectedItem().toString());
					if (inHouseFxGen) {
						plotArea.getDisplayData().getChannelBData().generateData();
						plotArea.getDisplayData().refreshChannel();
						measurements.refreshAllValues();
						plotArea.getChart().fireChartChanged();
					} else {
						connection.sendMessage("BCFG1");
						plotAreaControl.setTrigSource("BC");
					}
					toggleFxGenComponents(true);
				}
			}
		});
		
		//add wave type selection control
		fxWaveType.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (plotArea.getDisplayData().getChannelBData().getFxGenStatus()) {
					plotArea.getDisplayData().getChannelBData().setFxWave(fxWaveType.getSelectedItem().toString());
					if (inHouseFxGen) {
						plotArea.getDisplayData().getChannelBData().generateData();
						plotArea.getDisplayData().refreshChannel();
						measurements.refreshAllValues();
						plotArea.getChart().fireChartChanged();
					} else {
						if (fxWaveType.getSelectedItem().toString() == "Sine") {
							connection.sendMessage("BCFWS");
						} else if (fxWaveType.getSelectedItem().toString() == "Triangle") {
							connection.sendMessage("BCFWT");
						} else if (fxWaveType.getSelectedItem().toString() == "Square") {
							connection.sendMessage("BCFWQ");
						} else if (fxWaveType.getSelectedItem().toString() == "Ramp") {
							connection.sendMessage("BCFWR");
						} else {
							connection.sendMessage("BCFWN");
						}
						plotAreaControl.setTrigSource("BC");
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
					plotArea.getDisplayData().getChannelBData().generateData();
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
					connection.sendMultiVariablePack("BCFV", values);
					plotAreaControl.setTrigSource("BC");
				}
			}
		});
	}

}
