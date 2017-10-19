package plotdisplay;


import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;

import javax.swing.*;
import org.jfree.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import Data.DataSet;
import mainframecontrol.PlotAreaControl;
import plotdisplay.*;

/**
 * plot area class plotting data points using JFreechart
 * @author Michael Connor
 *
 */
public class PlotArea {
	
	//Essential Objects
	private JFreeChart xylineChart;
	private ChartPanel chartPanel;
	private DataSet dataSet;
	private final XYPlot plot;
	private NumberAxis voltageAxis = new NumberAxis();
	private NumberAxis timeAxis = new NumberAxis();
	private XYTextAnnotation chATrig = new XYTextAnnotation("ChA Trig", 0, 0);
	private XYTextAnnotation chBTrig = new XYTextAnnotation("ChB Trig", 0, 0);
	private boolean eightBits = false;
	
	/**
	 * Constructor of class constructing a plot area and initialize the dimensions of the GUI
	 * stroke of the plots, grids and labels
	 */
	@SuppressWarnings("deprecation")
	public PlotArea() {
		dataSet = new DataSet();
		xylineChart = ChartFactory.createXYLineChart(
		         "DigiScope" ,
		         "Time Elapsed Since Trigger (ms)" ,
		         "Voltage (V)" ,
		         dataSet,
		         PlotOrientation.VERTICAL ,
		         true , true , false);
			         
		chartPanel = new ChartPanel( xylineChart );
		chartPanel.setPreferredSize( new java.awt.Dimension( 1000 , 600 ) );
		chartPanel.setPopupMenu(null);
		chartPanel.setMouseZoomable(false);
		plot = xylineChart.getXYPlot( );
		plot.getRangeAxis().setAutoRange(false);
		plot.getDomainAxis().setAutoRange(false);
	    plot.getRangeAxis().setFixedAutoRange(2);
	    plot.getDomainAxis().setFixedAutoRange(100);
	    plot.setBackgroundPaint(Color.BLACK);
	    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
	    renderer.setShapesVisible(false);
	    renderer.setSeriesPaint( 0 , Color.YELLOW );
	    renderer.setSeriesPaint( 1 , Color.BLUE );
	    renderer.setSeriesPaint( 2 , Color.GREEN );
	    renderer.setSeriesPaint( 3 , Color.RED );
	    renderer.setSeriesStroke( 0 , new BasicStroke( 1.0f ) );
	    renderer.setSeriesStroke( 1 , new BasicStroke( 1.0f ) );
	    renderer.setSeriesStroke( 2 , new BasicStroke( 1.0f ) );
	    renderer.setSeriesStroke( 3 , new BasicStroke( 1.0f ) );
	    plot.setRenderer( renderer );
	    timeAxis.setRange(-10, 10);
	    timeAxis.setTickUnit(new NumberTickUnit(2));
	    timeAxis.setLabel("Time Elapsed Since Trigger (ms)");
	    voltageAxis.setRange(-2.75, 2.75);
	    voltageAxis.setTickUnit(new NumberTickUnit(0.5));
	    voltageAxis.setLabel("Voltage (V)");
	    plot.setDomainAxis(timeAxis);
	    plot.setRangeAxis(voltageAxis);
	    chATrig.setPaint(Color.CYAN);
	    chBTrig.setPaint(Color.ORANGE);
	}
	
	/**
	 * return the chart panel of the plot area
	 * @return
	 */
	public ChartPanel getChartPanel() {	
		return chartPanel;
	}
	
	/**
	 * return if the plot display is in 8 bits resolution
	 * @return
	 */
	public boolean isEightBits () {
		return eightBits;
	}
	
	/**
	 * set eight bits resolution to status
	 * @param status
	 */
	public void enableEightBits (boolean status) {
		eightBits = status;
	}
	
	/**
	 * return the voltage axis of the plot area
	 * @return
	 */
	public NumberAxis getVoltageAxis() {
		return voltageAxis;
	}
	
	/**
	 * set the voltage per division and the shift to the parameter
	 * @param voltsPerTick
	 * @param origin
	 */
	public void setVoltageDivision (double voltsPerTick, double origin) {
		voltageAxis.setTickUnit(new NumberTickUnit(voltsPerTick));
		voltageAxis.setRange(origin-voltsPerTick*5.5, origin+voltsPerTick*5.5);
	}
	
	/**
	 * set the time per division to the new value and update max visible time
	 * @param timePerTick
	 */
	public void setTimeDivision (double timePerTick) {
		timeAxis.setTickUnit(new NumberTickUnit(timePerTick));
		timeAxis.setRange(timePerTick*-5, timePerTick*5);
		dataSet.setMaxVisibleTime(timePerTick*5);
	}
	
	/**
	 * set sample size and sample rate of the plots
	 * this sets the parameter of these to both channels
	 * and clears/reset the current graph as changing sample rate
	 * on existing data will skew the calculations on frequency etc
	 * @param sampleSize
	 * @param sampleRate
	 */
	public void setSampleSize (int sampleSize, int sampleRate) {
		if (dataSet.getChannelAData().getSampleSize()==sampleSize && dataSet.getChannelAData().getSampleRate()==sampleRate ) {
			return;
		}
		dataSet.setSampleSize(sampleSize);
		dataSet.setSampleRate(sampleRate);
		if (dataSet.getChannelAData().getFxGenStatus()) {
			dataSet.getChannelAData().generateData();
		}
		if (dataSet.getChannelBData().getFxGenStatus()) {
			dataSet.getChannelBData().generateData();
		}
		dataSet.refreshChannel();
		xylineChart.fireChartChanged();
		
	}
	
	/**
	 * return the data set of the plot area
	 * this include channel A, channel B, filter channel and math channel
	 * @return
	 */
	public DataSet getDisplayData () {
		return dataSet;
	}
	
	/**
	 * return the line chart of the plot area
	 * @return
	 */
	public JFreeChart getChart () {
		return xylineChart;
	}
	
	/**
	 * load data to channel A for plotting
	 * data must in in the for of a single string CSV
	 * this function will also activates filter and math channel
	 * depending on their status
	 * @param data
	 */
	public void loadDataToChannelA (String data) {
		getDisplayData().getChannelAData().displayLiveData(data);
		getDisplayData().applyFilter();
		getDisplayData().applyMathExpression();
		getDisplayData().getChannelAData().getMeasurementPanel().refreshAllValues();
		getDisplayData().refreshChannel();
		setChATriggerAnnotation(false);
		setChATriggerAnnotation(true);
	}
	
	/**
	 * load data to channel B for plotting
	 * data must in in the for of a single string CSV
	 * this function will also activates filter and math channel
	 * depending on their status
	 * @param data
	 */
	public void loadDataToChannelB (String data) {
		getDisplayData().getChannelBData().displayLiveData(data);
		getDisplayData().applyFilter();
		getDisplayData().applyMathExpression();
		getDisplayData().getChannelBData().getMeasurementPanel().refreshAllValues();
		getDisplayData().refreshChannel();
		setChBTriggerAnnotation(false);
		setChBTriggerAnnotation(true);
		
	}
	
	/**
	 * turn trigger point for channel A on or off
	 * turning it off then on will reset and recalculate
	 * the trigger point
	 * @param status
	 */
	public void setChATriggerAnnotation (boolean status) {
		if (status) {
			if (dataSet.getChannelAData().isEmpty()) {
				plot.removeAnnotation(chATrig);
				return;
			}
			chATrig.setY(dataSet.getChannelAData().getDataItem(dataSet.getChannelAData().getItemCount()/2).getYValue());
			plot.addAnnotation(chATrig);
		} else {
			plot.removeAnnotation(chATrig);
		}
	}
	
	/**
	 * turn trigger point for channel B on or off
	 * turning it off then on will reset and recalculate
	 * the trigger point
	 * @param status
	 */
	public void setChBTriggerAnnotation (boolean status) {
		if (status) {
			if (dataSet.getChannelBData().isEmpty()) {
				plot.removeAnnotation(chBTrig);
				return;
			}
			chBTrig.setY(dataSet.getChannelBData().getDataItem(dataSet.getChannelBData().getItemCount()/2).getYValue());
			plot.addAnnotation(chBTrig);
		} else {
			plot.removeAnnotation(chBTrig);
		}
	}
}