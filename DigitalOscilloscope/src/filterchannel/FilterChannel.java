package filterchannel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import Data.Data;
import mainframecontrol.MeasurementDisplay;
import plotdisplay.PlotArea;

/**
 * Filter channel control
 * @author Michael Connor
 *
 */
public class FilterChannel extends JPanel {
	
	//Essential variable and objects
	private PlotArea plotArea;
	private JFileChooser fileDirectory = new JFileChooser();
	private JButton loadFilter = new JButton("Load Filter Variable File");
	private JButton toggle = new JButton("On");
	private JButton refresh = new JButton("Refresh");
	private String filePath;
	private DefaultListModel<Double> firFilterVariables = new DefaultListModel<Double>();
	private DefaultListModel<Double[]> iirFilterVariables = new DefaultListModel<Double[]>();
	private JLabel filterStatus = new JLabel("No Filter Loaded");
	private JLabel sourceChannel = new JLabel("Source Channel");
	private JComboBox channelInput = new JComboBox();
	private Data filterData;
	private MeasurementDisplay measurements;
	private boolean readingIIR = false;
	private DecimalFormat df = new DecimalFormat("#.###");
	
	/**
	 * constructor requiring plot area
	 * setting up objects and variables
	 * @param plotArea
	 */
	public FilterChannel (PlotArea plotArea) {
		this.plotArea = plotArea;
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		fileDirectory.addChoosableFileFilter(new CSVFileFilter());
		fileDirectory.setAcceptAllFileFilterUsed(false);
		filterData = plotArea.getDisplayData().getFilterData();
		channelInput.addItem("Channel A");
		channelInput.addItem("Channel B");
		channelInput.addItem("Math");
		df.setRoundingMode(RoundingMode.CEILING);
		measurements = plotArea.getDisplayData().getFilterMeasure();
		JPanel control = new JPanel();
		JPanel display = new JPanel();
		control.add(loadFilter);
		control.add(sourceChannel);
		control.add(channelInput);
		control.add(toggle);
		control.add(refresh);
		display.add(filterStatus);
		this.add(control);
		this.add(display);
		this.add(measurements);
		addActionListener();
	}
	
	/**
	 * add commands to buttons and combo boxes
	 */
	private void addActionListener () {
		
		//select channel input command
		channelInput.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				plotArea.getDisplayData().setSourceChannel(channelInput.getSelectedItem().toString());
			}
		});
		
		//toggle on and off the filter channel
		toggle.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (toggle.getText() == "On") {
					toggle.setText("Off");
					plotArea.getDisplayData().setFilterStatus(true);
					plotArea.getDisplayData().applyFilter();
					plotArea.getDisplayData().applyMathExpression();
				} else {
					toggle.setText("On");
					plotArea.getDisplayData().setFilterStatus(false);
					filterData.clear();
					if (plotArea.getDisplayData().getMathExpression().contains("F")) {
						plotArea.getDisplayData().getMathData().clear();
					}
					measurements.zeroAllMeasurements();
				}
			}
		});
		
		//refresh filter channel and replot if source channel or filter variable change
		refresh.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (plotArea.getDisplayData().filterStatus()) {
					plotArea.getDisplayData().applyFilter();
					plotArea.getDisplayData().applyMathExpression();
				}
			}
		});
		
		//load filter file, open up file selector with a csv filter
		loadFilter.addActionListener(new ActionListener() {
			
			@Override
			/**
			 * load a csv file from file selector window with a csv filter
			 * will throw error with messages if the filter file is invalid
			 * will determine what kind of filter is loaded and notify the user
			 */
			public void actionPerformed(ActionEvent e) {
				firFilterVariables.clear();
				iirFilterVariables.clear();
				fileDirectory.setCurrentDirectory(new File("C:"));
	            int returnVal = fileDirectory.showOpenDialog(FilterChannel.this);

	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	                filePath = fileDirectory.getSelectedFile().getAbsolutePath();
	                String line = "";
	            	String cvsSplitBy = ",";
	            	BufferedReader br = null;
	                try {
	            		br = new BufferedReader(new FileReader(filePath));
	            		while ((line = br.readLine()) != null) {
	            			if (line.contains(cvsSplitBy) && firFilterVariables.isEmpty()) {
	            				readingIIR = true;
	            				String[] variables = line.split(cvsSplitBy);
	            				if (variables.length != 2) {
	            					filterStatus.setText("No Filter Loaded");
	            					iirFilterVariables.clear();
	            					plotArea.getDisplayData().setFilterStatus(false);
	            					measurements.zeroAllMeasurements();
	            					toggle.setText("On");
	            					plotArea.getChart().fireChartChanged();
	            					readingIIR = false;
	            					JOptionPane.showMessageDialog(null, "Loading Filter Variables Failed "
	            							+ "- File does not conform to IIR filter format", 
	            							"File Format Error", JOptionPane.ERROR_MESSAGE);
	            					break;
	            				} else {
	            					Double[] var = {0.000, 0.000};
	            					if (variables[0].length()==1) {
	            						if (variables[0].charAt(0) == 'e') {
	            							variables[0] = "2.718";
	            						}
	            					}
	            					if (variables[0].length()==2) {
	            						if (variables[0].charAt(0) == '-' && variables[0].charAt(1) == 'e') {
	            							variables[0] = "-2.718";
	            						}
	            						if (variables[0].charAt(0) == 'p' && variables[0].charAt(1) == 'i') {
	            							variables[0] = "3.142";
	            						}
	            					}
	            					if (variables[0].length()==3) {
	            						if (variables[0].charAt(0) == '-' && variables[0].charAt(1) == 'p' && variables[0].charAt(2) == 'i') {
	            							variables[0] = "-3.142";
	            						}
	            					}
	            					if (variables[1].length()==1) {
	            						if (variables[1].charAt(0) == 'e') {
	            							variables[1] = "2.718";
	            						}
	            					}
	            					if (variables[1].length()==2) {
	            						if (variables[1].charAt(0) == '-' && variables[1].charAt(1) == 'e') {
	            							variables[1] = "-2.718";
	            						}
	            						if (variables[1].charAt(0) == 'p' && variables[1].charAt(1) == 'i') {
	            							variables[1] = "3.142";
	            						}
	            					}
	            					if (variables[1].length()==3) {
	            						if (variables[1].charAt(0) == '-' && variables[1].charAt(1) == 'p' && variables[1].charAt(2) == 'i') {
	            							variables[1] = "-3.142";
	            						}
	            					}
	            					try {
	            						var[0] = Double.parseDouble(variables[0]);
	            						var[1] = Double.parseDouble(variables[1]);
	            						iirFilterVariables.addElement(var);
	            						filterStatus.setText("IIR Filter Loaded");
	            					} catch (Exception varerr) {
	            						JOptionPane.showMessageDialog(null, "Loading Filter Variable Failed - "
		            							+ "Check if file contains only numbers", 
		            							"File Format Error", JOptionPane.ERROR_MESSAGE);
		            					filterStatus.setText("No Filter Loaded");
		            					iirFilterVariables.clear();
		            					plotArea.getDisplayData().setFilterStatus(false);
		            					measurements.zeroAllMeasurements();
		            					toggle.setText("On");
		            					plotArea.getChart().fireChartChanged();
		            					break;
	            					}
	            				}
	            			} else {
	            				if (readingIIR) {
	            					filterStatus.setText("No Filter Loaded");
	            					iirFilterVariables.clear();
	            					plotArea.getDisplayData().setFilterStatus(false);
	            					measurements.zeroAllMeasurements();
	            					toggle.setText("On");
	            					plotArea.getChart().fireChartChanged();
	            					readingIIR = false;
	            					JOptionPane.showMessageDialog(null, "Loading Filter Variables Failed "
	            							+ "- File does not conform to IIR filter format", 
	            							"File Format Error", JOptionPane.ERROR_MESSAGE);
	            					break;
		            			} else {
		            				try {
		            					if (line.length()==1) {
		            						if (line.charAt(0) == 'e') {
		            							line = "2.718";
		            						}
		            					}
		            					if (line.length()==2) {
		            						if (line.charAt(0) == '-' && line.charAt(1) == 'e') {
		            							line = "-2.718";
		            						}
		            						if (line.charAt(0) == 'p' && line.charAt(1) == 'i') {
		            							line = "3.142";
		            						}
		            					}
		            					if (line.length()==3) {
		            						if (line.charAt(0) == '-' && line.charAt(1) == 'p' && line.charAt(2) == 'i') {
		            							line = "-3.142";
		            						}
		            					}
		            					if (line.contains("e")) {
		            						String numVal[] = line.split("e");
		            						if (numVal.length == 2) {
		            							double value = Double.parseDouble(numVal[0])*Math.pow(10.0, Double.parseDouble(numVal[1]));
		            							line = df.format(value);
		            						}
		            					}
		            					if (line.contains("E")) {
		            						String numVal[] = line.split("E");
		            						if (numVal.length == 2) {
		            							double value = Double.parseDouble(numVal[0])*Math.pow(10.0, Double.parseDouble(numVal[1]));
		            							line = df.format(value);
		            						}
		            					}
		            					firFilterVariables.addElement(Double.parseDouble(line));
		            					filterStatus.setText("FIR Filter Loaded");
		            				} catch (Exception efd) {
		            					JOptionPane.showMessageDialog(null, "Loading Filter Variable Failed - "
		            							+ "Check if file contains only numbers", 
		            							"File Format Error", JOptionPane.ERROR_MESSAGE);
		            					filterStatus.setText("No Filter Loaded");
		            					firFilterVariables.clear();
		            					plotArea.getDisplayData().setFilterStatus(false);
		            					measurements.zeroAllMeasurements();
		            					toggle.setText("On");
		            					plotArea.getChart().fireChartChanged();
		            					break;
		            				}
		            			}
	            			}	
	            		}
	            	} catch (Exception err) {};
	            	for (int i=0; i<firFilterVariables.size(); i++) {
	            		System.out.println(firFilterVariables.getElementAt(i));
	            	}
	            	for (int i=0; i<iirFilterVariables.size(); i++) {
	            		System.out.println("A: " + iirFilterVariables.getElementAt(i)[0] + " B: " + iirFilterVariables.getElementAt(i)[1]);
	            	}
	            	readingIIR = false;
	            	loadVariables();
	            }
			}
		});
	}
	
	/**
	 * load the variable into the the plot area for use
	 */
	private void loadVariables () {
		if (firFilterVariables.size()>0) {
			plotArea.getDisplayData().setFIRVariable(firFilterVariables);
		} else {
			plotArea.getDisplayData().setIIRVariable(iirFilterVariables);
		}
		plotArea.getDisplayData().applyFilter();
	}
}
