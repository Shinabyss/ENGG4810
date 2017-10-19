package Data;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.fathzer.soft.javaluator.DoubleEvaluator;

import mainframecontrol.MeasurementDisplay;

import biz.source_code.*;
import biz.source_code.dsp.filter.FilterPassType;
import biz.source_code.dsp.filter.IirFilterCoefficients;
import biz.source_code.dsp.filter.IirFilterDesignExstrom;

/**
 * contain data information for all channels to be plotted
 * also stores the on/off status of math and filter channel
 * @author Dustin
 *
 */
public class DataSet extends XYSeriesCollection {
	
	//Essential objects and variables
	private Data channelA;
	private Data channelB;
	private Data math;
	private Data filter;
	private Data triggerAnnoA;
	private Data triggerAnnoB;
	private DefaultListModel<Double[]> filterVarIIR = new DefaultListModel<Double[]>();
	private DefaultListModel<Double> filterVarFIR = new DefaultListModel<Double>();;
	private String mathExpression = null;
	private DoubleEvaluator evaluator = new DoubleEvaluator();
	private String sourceChannel = "Channel A";
	private boolean filterOn = false;
	private boolean mathOn = false;
	private MeasurementDisplay filterMeasure;
	private MeasurementDisplay mathMeasure;
	private DecimalFormat nf = new DecimalFormat("#.##################");
	
	/**
	 * construct a new data set with 4 channels of data
	 * channelA, channelB, math and filter
	 */
	public DataSet() {
		channelA = new Data("Channel A");
		channelB = new Data("Channel B");
		filter = new Data("Filter Channel");
		math = new Data("Math Channel");
		triggerAnnoA = new Data("Ch A Trigger Point");
		triggerAnnoB = new Data("Ch B Trigger Point");
		nf.setMaximumFractionDigits(18);
		nf.setRoundingMode(RoundingMode.CEILING);
		this.addSeries(channelA);
		this.addSeries(channelB);
		this.addSeries(filter);
		this.addSeries(math);
		filterMeasure = new MeasurementDisplay(filter);
		mathMeasure = new MeasurementDisplay(math);
	}
	
	/**
	 * return channel A data
	 * @return
	 */
	public Data getChannelAData() {
		return channelA;
	}
	
	/**
	 * return channel B data
	 * @return
	 */
	public Data getChannelBData() {
		return channelB;
	}
	
	/**
	 * return filter channel data
	 * @return
	 */
	public Data getFilterData() {
		return filter;
	}
	
	/**
	 * return math channel data
	 * @return
	 */
	public Data getMathData() {
		return math;
	}
	
	/**
	 * set sample size to new value on all channels
	 * sample size kept universal for math calculation purposes
	 * @param newSampleSize
	 */
	public void setSampleSize (int newSampleSize) {
		channelA.setSampleSize(newSampleSize);
		channelB.setSampleSize(newSampleSize);
		filter.setSampleSize(newSampleSize);
		math.setSampleSize(newSampleSize);
	}
	
	/**
	 * set sample rate to new value on all channels
	 * sample rate kept universal for math calculation purposes
	 * @param newSampleRate
	 */
	public void setSampleRate (int newSampleRate) {
		channelA.setSampleRate(newSampleRate);
		channelB.setSampleRate(newSampleRate);
	}
	
	/**
	 * takes in a list of doubles and load the doubles as variables for fir filtering
	 * @param variables
	 */
	public void setFIRVariable (DefaultListModel<Double> variables) {
		filterVarFIR.clear();
		filterVarIIR.clear();
		for (int i=0; i<variables.size(); i++) {
			filterVarFIR.addElement(variables.get(i));
		}
	}
	
	/**
	 * takes in a list of double arrays and load the doubles as variables for iir filtering
	 * @param variables
	 */
	public void setIIRVariable (DefaultListModel<Double[]> variables) {
		filterVarFIR.clear();
		filterVarIIR.clear();
		for (int i=0; i<variables.size(); i++) {
			filterVarIIR.addElement(variables.get(i));
		}
	}
	
	/**
	 * return the currently loaded fir filter variable
	 * @return
	 */
	public DefaultListModel<Double> getFIRVariable () {
		return filterVarFIR;
	}
	
	/**
	 * return the currently loaded iir filter variable
	 * @return
	 */
	public DefaultListModel<Double[]> getIIRVariable () {
		return filterVarIIR;
	}	
	
	/**
	 * set the math expression to the parameter string
	 * @param expression
	 */
	public void setMathExpression (String expression) {
		mathExpression = expression;
	}
	
	/**
	 * return the math expression string
	 * @return
	 */
	public String getMathExpression () {
		return mathExpression;
	}
	
	/**
	 * apply math expression and store the calculated results into the math channel
	 * will throw exception and display error message on invalid expression and circular calculations
	 * refresh measurement values of math channel
	 */
	public void applyMathExpression () {
		if (mathOn) {
			if (mathExpression.isEmpty()) {
				JOptionPane.showMessageDialog(null, "Math Expression cannot be evaluated - "
						+ "Math expression cannot be detected", 
						"Math Expression Format Error", JOptionPane.ERROR_MESSAGE);
			} else {
				math.clear();
				for (int i=0; i<math.getSampleSize(); i++) {
					try {
						if (mathExpression.contains("F") && sourceChannel == "Math") {
							JOptionPane.showMessageDialog(null, "Math Expression cannot be evaluated - "
									+ "Check if filter Channel is using math channel as source", 
									"Math and Filter Circulatory Calculation", JOptionPane.ERROR_MESSAGE);
							math.clear();
							break;
						} else if (mathExpression.contains("A") && mathExpression.contains("B") && mathExpression.contains("F")) {
							String subAExpression = mathExpression.replace("A", nf.format(channelA.getY(i)));
							String subABExpression = subAExpression.replace("B", nf.format(channelB.getY(i)));
							String subABFExpression = subABExpression.replace("F", nf.format(filter.getY(i)));
							math.add(channelA.getX(i), evaluator.evaluate(subABFExpression));
						} else if (mathExpression.contains("A") && mathExpression.contains("B")) {
							String subAExpression = mathExpression.replace("A", nf.format(channelA.getY(i)));
							String subABExpression = subAExpression.replace("B", nf.format(channelB.getY(i)));
							math.add(channelA.getX(i), evaluator.evaluate(subABExpression));
						} else if (mathExpression.contains("A") && mathExpression.contains("F")) {
							String subAExpression = mathExpression.replace("A", nf.format(channelA.getY(i)));
							String subAFExpression = subAExpression.replace("F", nf.format(filter.getY(i)));
							math.add(channelA.getX(i), evaluator.evaluate(subAFExpression));
						} else if (mathExpression.contains("B") && mathExpression.contains("F")) {
							String subBExpression = mathExpression.replace("B", nf.format(channelB.getY(i)));
							String subBFExpression = subBExpression.replace("F", nf.format(filter.getY(i)));
							math.add(channelB.getX(i), evaluator.evaluate(subBFExpression));
						} else if (mathExpression.contains("A")) {
							String subAExpression = mathExpression.replace("A", nf.format(channelA.getY(i)));
							math.add(channelA.getX(i), evaluator.evaluate(subAExpression));
						} else if (mathExpression.contains("B")) {
							String subBExpression = mathExpression.replace("B", nf.format(channelB.getY(i)));
							math.add(channelB.getX(i), evaluator.evaluate(subBExpression));
						} else if (mathExpression.contains("F")) {
							String subFExpression = mathExpression.replace("F", nf.format(filter.getY(i)));
							math.add(filter.getX(i), evaluator.evaluate(subFExpression));
						} else {
							mathExpression = null;
							JOptionPane.showMessageDialog(null, "Math Expression cannot be evaluated - "
									+ "Please make sure math function contain at least one channel to calculate from", 
									"Math Expression Format Error", JOptionPane.ERROR_MESSAGE);
						}
						
		
					} catch (Exception mathformat) {
						JOptionPane.showMessageDialog(null, "Math Expression cannot be evaluated - "
								+ "Check expression and turn on all channels used in expression", 
								"Math Expression Format Error", JOptionPane.ERROR_MESSAGE);
						math.clear();
						break;
					}
				}
				if (!math.isEmpty()) {
					mathMeasure.refreshAllValues();
				}
			}
		}
	}
	
	/**
	 * apply filter variable depending on which one contains value and store the calculated results into the filter channel
	 * will throw exception and display error message on circular calculations
	 * refresh measurement values of filter channel
	 */
	public void applyFilter () {
		if (filterOn && !sourceChannel.isEmpty()) {
			if (sourceChannel == "Channel A") {
				if (filterVarFIR.size()>0) {
					filter.applyFIRFilter(channelA, filterVarFIR);
				} else if (filterVarIIR.size()>0) {
					filter.applyIIRFilter(channelA, filterVarIIR);
				}
			} else if (sourceChannel == "Channel B") {
				if (filterVarFIR.size()>0) {
					filter.applyFIRFilter(channelB, filterVarFIR);
				} else if (filterVarIIR.size()>0) {
					filter.applyIIRFilter(channelB, filterVarIIR);
				}
			} else {
				if (mathExpression.contains("F")) {
					JOptionPane.showMessageDialog(null, "Filter cannot be evaluated - "
							+ "Check if math Channel is using filter channel as source", 
							"Math and Filter Circulatory Calculation", JOptionPane.ERROR_MESSAGE);
					filter.clear();
				} else if (filterVarFIR.size()>0) {
					filter.applyFIRFilter(math, filterVarFIR);
				} else if (filterVarIIR.size()>0) {
					filter.applyIIRFilter(math, filterVarIIR);
				}
			}
			filterMeasure.refreshAllValues();
		}
	}
	
	/**
	 * return the filter measurement display
	 * @return
	 */
	public MeasurementDisplay getFilterMeasure () {
		return filterMeasure;
	}
	
	/**
	 * return the math measurement display
	 * @return
	 */
	public MeasurementDisplay getMathMeasure () {
		return mathMeasure;
	}
	
	/**
	 * set the current trigger source channel to the new source parameter
	 * @param source
	 */
	public void setSourceChannel (String source) {
		sourceChannel = source;
	}
	
	/**
	 * return the current source channel
	 * @return
	 */
	public String getSourceChannel () {
		return sourceChannel;
	}
	
	/**
	 * refresh all channels on plot area
	 */
	public void refreshChannel() {
		this.removeAllSeries();
		this.addSeries(channelA);
		this.addSeries(channelB);
		this.addSeries(filter);
		this.addSeries(math);
	}
	
	/**
	 * return if the filter channel is currently on
	 * @return
	 */
	public boolean filterStatus () {
		return filterOn;
	}
	
	/**
	 * turn filter channel on/off to the status parameter
	 * @param status
	 */
	public void setFilterStatus (boolean status) {
		filter.clear();
		this.fireDatasetChanged();
		filterOn = status;
	}
	
	/**
	 * return if the math channel is currently on
	 * @return
	 */
	public boolean mathStatus () {
		return mathOn;
	}
	
	/**
	 * turn math channel on/off to the status parameter
	 * @param status
	 */
	public void setMathStatus (boolean status) {
		mathOn = status;
	}
	
	/**
	 * remove channel A data
	 */
	public void removeChannelA () {
		this.removeSeries(channelA);
	}
	
	/**
	 * add channel A data
	 */
	public void addChannelA () {
		this.addSeries(channelA);
	}
	
	/**
	 * annotate the trigger point of channel A
	 * @param status
	 */
	public void setATriggerPointDisplay (boolean status) {
		if (status) {
			triggerAnnoA.add(channelA.getDataItem(0).getXValue(), channelA.getDataItem(0).getY());
		} else {
			triggerAnnoA.clear();
		}
		this.fireDatasetChanged();
	}
	
	/**
	 * annotate the trigger point of channel B
	 * @param status
	 */
	public void setBTriggerPointDisplay (boolean status) {
		if (status) {
			triggerAnnoB.add(channelB.getDataItem(0).getXValue(), channelB.getDataItem(0).getY());
		} else {
			triggerAnnoB.clear();
		}
		this.fireDatasetChanged();
	}
	
	/**
	 * set the maximum visible time of all the channel to parameter
	 * @param maxVisibleTime
	 */
	public void setMaxVisibleTime(double maxVisibleTime) {
		channelA.setMaxVisibleTime(maxVisibleTime);
		channelB.setMaxVisibleTime(maxVisibleTime);
		math.setMaxVisibleTime(maxVisibleTime);
		filter.setMaxVisibleTime(maxVisibleTime);
	}
	
	/**
	 * refresh all channel measurement values
	 */
	public void refreshAllChannelMeasurement () {
		channelA.getMeasurementPanel().refreshAllValues();
		channelB.getMeasurementPanel().refreshAllValues();
		math.getMeasurementPanel().refreshAllValues();
		filter.getMeasurementPanel().refreshAllValues();
	}

}
