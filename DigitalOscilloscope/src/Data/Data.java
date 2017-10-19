package Data;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.DefaultListModel;

import org.jfree.data.general.SeriesException;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYIntervalDataItem;
import org.jfree.data.xy.XYSeries;

import mainframecontrol.MeasurementDisplay;

import biz.source_code.*;
import biz.source_code.dsp.filter.FilterCharacteristicsType;
import biz.source_code.dsp.filter.FilterPassType;
import biz.source_code.dsp.filter.IirFilter;
import biz.source_code.dsp.filter.IirFilterCoefficients;
import biz.source_code.dsp.filter.IirFilterDesignExstrom;
import biz.source_code.dsp.filter.IirFilterDesignFisher;
import org.jtransforms.fft.*;
/**
 * The data class, storing data in x and y coordinates
 * allow multitude method of manipulation to the data points
 * 
 * Credit to eichelbe for providing opensource library 
 * JTransforms to perform fft operations
 * 
 * @author Michael Connor
 *
 */
public class Data extends XYSeries {
	
	//data variables
	private int frequency;
	private double peakToPeak;
	private double verticalOffset;
	private double horizontalOffset;
	private double fxGenOffset;
	private int sampleSize;
	private int sampleRate;
	private boolean channelOnOff = true;
	private boolean fxGen = false;
	private String fxGenWave;
	
	//measurement variables
	private String mMaxVoltage = "0.0";
	private String mMinVoltage = "0.0";
	private String mP2PVoltage = "0.0";
	private String mAveVoltage = "0.0";
	private String mStdVoltage = "0.0";
	private String mFrequency = "0.0";
	private DecimalFormat df = new DecimalFormat("#.####");
	private DecimalFormat ff = new DecimalFormat("#.#");
	
	//measurement objects
	private MeasurementDisplay measurementPanel;
	private XYSeries visibleData = new XYSeries("Visible Data");
	private double visibleMaxTime;
	
	//band pass variables and objects
	private int interpolationRate = 30;
	private IirFilterCoefficients lowPassCoef;
	private IirFilterCoefficients bandPassCoef;
	private IirFilter lowPassFilter;
	private IirFilter bandPassFilter;
	private boolean lowPass = true;
	
	//memory array for storing XY-data
	ArrayList<XYDataItem> mem = new ArrayList<XYDataItem>();
	
	/**
	 * construct a data class to store data points for plotting
	 * @param channelName
	 */
	public Data(String channelName) {
		super(channelName, true, false);
		frequency = 1000;
		peakToPeak = 2;
		verticalOffset = 0;
		fxGenOffset = 0;
		horizontalOffset = 0;
		sampleSize = 25000;
		sampleRate = 1000000;
		visibleMaxTime = 250;
		df.setRoundingMode(RoundingMode.CEILING);
		ff.setRoundingMode(RoundingMode.CEILING);
		measurementPanel = new MeasurementDisplay(this);
		zeroAllMeasurement();
	}
	
	/**
	 * turn on/off low pass mode
	 * set data low pass mode to status
	 * @param status
	 */
	public void setLowPass (boolean status) {
		lowPass = status;
	}
	
	/**
	 * return if this data is currently running low pass mode
	 * @return
	 */
	public boolean isLowPass () {
		return lowPass;
	}
	
	/**
	 * perform band pass operation on the current data
	 */
	public void bandPassSignal () {
		if (lowPass || this.isEmpty()) {
			return;
		}
		int newSampleFrequency = interpolationRate*sampleRate;
		double minX = this.getMinX();
		double maxX = this.getMaxX();
		double maxY = this.getMaxY();
		double stepSize = (maxX-minX)/(sampleSize*interpolationRate);
		double calcFrequency = calculateFrequency(this);
		double lowerPassBand = (calcFrequency + sampleRate)*0.9/newSampleFrequency;
		double upperPassBand = (calcFrequency + sampleRate)*1.1/newSampleFrequency;
		if (!this.isEmpty()) {
			for (double i=this.getMinX(); i<this.getMaxX(); i+=stepSize) {
				try {
					this.add(i, 0);
				} catch (SeriesException err) {}
			}
		}
		double fcf1 = ((double)newSampleFrequency/2)/interpolationRate/newSampleFrequency;
		lowPassCoef = IirFilterDesignExstrom.design(FilterPassType.lowpass, 10, fcf1, 0);
		lowPassFilter = new IirFilter(lowPassCoef);
		bandPassCoef = IirFilterDesignFisher.design(FilterPassType.bandpass, FilterCharacteristicsType.chebyshev, 4, -40, lowerPassBand, upperPassBand);
		bandPassFilter = new IirFilter(bandPassCoef);
		for (int j=0; j<this.getItemCount(); j++){
			double filteredValue = lowPassFilter.step(this.getY(j).doubleValue());
			filteredValue *= Math.sin(sampleRate*this.getX(j).doubleValue());
			double yValue = bandPassFilter.step(filteredValue);
			this.addOrUpdate(this.getX(j).doubleValue(), yValue);
		}
		ArrayList<XYDataItem> plotData = new ArrayList<XYDataItem>();
		for (int k=0; k<this.getItemCount(); k++) {
			if (k >= (this.getItemCount()-sampleSize)/2 && k < (this.getItemCount()+sampleSize)/2) {
				plotData.add(this.getDataItem(k));
			}
		}
		this.clear();
		for (int l=0; l<plotData.size(); l++) {
			this.add(plotData.get(l));
		}
		double newMaxY = this.getMaxY();
		for (int m=0; m<this.getItemCount(); m++) {
			this.updateByIndex(m, this.getY(m).doubleValue()/newMaxY*maxY*2);
		}
		sampleRate = newSampleFrequency;
		this.fireSeriesChanged();
	}
	
	/**
	 * set interpolation rate to value
	 * @param value
	 */
	public void setInterpolationRate (int value) {
		interpolationRate = value;
	}
	
	/**
	 * get the measurement display for the current data
	 * @return
	 */
	public MeasurementDisplay getMeasurementPanel () {
		return measurementPanel;
	}
	
	/**
	 * get the measured max voltage of the visible data
	 * @return
	 */
	public String getmMaxVoltage () {
		return mMaxVoltage;
	}
	
	/**
	 * get the measure min voltage of the visible data
	 * @return
	 */
	public String getmMinVoltage () {
		return mMinVoltage;
	}
	
	/**
	 * get the measured peak to peak voltage of the visible data
	 * @return
	 */
	public String getmP2PVoltage () {
		return mP2PVoltage;
	}
	
	/**
	 * get the measure average voltage of the visible data
	 * @return
	 */
	public String getmAveVoltage () {
		return mAveVoltage;
	}
	
	/**
	 * get the measured standard deviation voltage of the visible data
	 * @return
	 */
	public String getmStdVoltage () {
		return mStdVoltage;
	}
	
	/**
	 * get the measured frequency of the visible data
	 * @return
	 */
	public String getmFrquency () {
		return mFrequency;
	}
	
	/**
	 * zero all the measurement values of the data
	 */
	public void zeroAllMeasurement () {
		mMaxVoltage = "0.00";
		mMinVoltage = "0.00";
		mP2PVoltage = "0.00";
		mAveVoltage = "0.00";
		mStdVoltage = "0.00";
		mFrequency = "0.00";
	}
	
	/**
	 * set the current maximum visible time to the parsed argument
	 * @param maxVisibleTime
	 */
	public void setMaxVisibleTime (double maxVisibleTime) {
		visibleMaxTime = maxVisibleTime;
	}
	
	/**
	 * take in a data value and return the calculated frequency in Hz
	 * make use of the JTransforms library to perform fast fourier transfrom
	 * @param data
	 * @return
	 */
	public double calculateFrequency (XYSeries data) {
		if (data.isEmpty()) {
			return 0;
		}
		double[] input = new double[data.getItemCount()];
		ArrayList<Double> magnitude = new ArrayList<Double>();
		double maxMagnitude = -127;
		int maxIndex = -1;
		double calcFrequency = 0;
		for (int k=0; k<data.getItemCount(); k++) {
			input[k] = data.getY(k).doubleValue();
		}
		DoubleFFT_1D fftDo = new DoubleFFT_1D(input.length);
        double[] fft = new double[input.length * 2];
        System.arraycopy(input, 0, fft, 0, input.length);
        fftDo.realForwardFull(fft);
        for (int l=0; l<(data.getItemCount()/2); l++) {
    		  double re = fft[2*l];
    		  double im = fft[2*l+1];
    		  magnitude.add(Math.sqrt(re*re+im*im));
        }
        for (int m=0; m<magnitude.size(); m++) {
        	if (magnitude.get(m)>maxMagnitude) {
        		maxMagnitude = magnitude.get(m);
        		maxIndex = m;
        	}
        }
        calcFrequency = (double) maxIndex*sampleRate/data.getItemCount();
        return calcFrequency;
	}
	
	/**
	 * update the current visible data by using the max visible time variable variable
	 * then calculate all measurements using the updated visible data
	 * zero all measurements if the current visible data is empty
	 */
	public void refreshAllMeasurements () {
		visibleData.clear();
		if (this.isEmpty()) {
			zeroAllMeasurement();
			return;
		}
		try {
			double globalTotalVoltage = 0.0;
			for (int i=0; i<this.getItemCount(); i++) {
				if (this.getX(i).doubleValue()>=(-visibleMaxTime) && this.getX(i).doubleValue()<=visibleMaxTime) {
					visibleData.add(this.getDataItem(i));
				}
				globalTotalVoltage += this.getY(i).doubleValue();
			}
			double globalAveVolt = globalTotalVoltage/this.getItemCount();
			mMaxVoltage = df.format(visibleData.getMaxY());
			mMinVoltage = df.format(visibleData.getMinY());
			double calcP2P = (visibleData.getMaxY()-visibleData.getMinY());
			mP2PVoltage = df.format(calcP2P);
			double totalVisibleVoltage = 0.0;
			for (int i=0; i<visibleData.getItemCount(); i++) {
				totalVisibleVoltage += visibleData.getY(i).doubleValue();
			}
			double visibleAveVolt = totalVisibleVoltage/visibleData.getItemCount();
			mAveVoltage = df.format(visibleAveVolt);
			double deviation = 0.0;
			for (int j=0; j<visibleData.getItemCount(); j++) {
				deviation += (Math.pow((visibleData.getY(j).doubleValue()-visibleAveVolt), 2));
			}
			double stdev = Math.sqrt(deviation/visibleData.getItemCount());
			mStdVoltage = df.format(stdev);
			double calcFrequency = calculateFrequency(visibleData);
			mFrequency = ff.format(calcFrequency);
		} catch (Exception nperr) {};
	}
	
	/**
	 * set the frequency for the data - mainly for function gen purpose
	 * @param newFrequency
	 */
	public void setFrequency (int newFrequency) {
		frequency = newFrequency;
	}
	
	/**
	 * set the peak to peak for the data - mainly for function gen purpose
	 * @param newPeak2Peak
	 */
	public void setPeakToPeak (double newPeak2Peak) {
		peakToPeak = newPeak2Peak;
	}
	
	/**
	 * set the vertical offset for the data
	 * @param newVOffset
	 */
	public void setVerticalOffset (double newVOffset) {
		double updateValue = newVOffset - verticalOffset;
		verticalOffset = newVOffset;
		for (int i=0; i<this.getItemCount(); i++) {
			this.updateByIndex(i, (this.getY(i).doubleValue()+updateValue));
		}
	}
	
	/**
	 * set the function gen vertical offset for the data
	 * @param newFxVOffset
	 */
	public void setFxGenVOffset (double newFxVOffset) {
		fxGenOffset = newFxVOffset;
	}
	
	/**
	 * set the horizontal offset for the data
	 * @param newHOffset
	 */
	public void setHorizontalOffset (double newHOffset) {
		horizontalOffset = newHOffset;
	}
	
	/**
	 * set the sample size of this data
	 * @param newSampleSize
	 */
	public void setSampleSize (int newSampleSize) {
		sampleSize = newSampleSize;
	}
	
	/**
	 * set the sample rate of this data
	 * @param newSampleRate
	 */
	public void setSampleRate (int newSampleRate) {
		sampleRate = newSampleRate;
	}
	
	/**
	 * return the current set frequency of the data
	 * @return
	 */
	public int getFrequency () {
		return frequency;
	}
	
	/**
	 * return the current set peak to peak of the data
	 * @return
	 */
	public double getPeakToPeak () {
		return peakToPeak;
	}
	
	/**
	 * return the current vertical offset of this data
	 * @return
	 */
	public double getVerticalOffset () {
		return verticalOffset;
	}
	
	/**
	 * return the current horizontal offset of the data
	 * @return
	 */
	public double getHorizontalOffset () {
		return horizontalOffset;
	}
	
	/**
	 * return the current sample size of data
	 * @return
	 */
	public int getSampleSize () {
		return sampleSize;
	}
	
	/**
	 * return the current sample rate of the data
	 * @return
	 */
	public int getSampleRate () {
		return sampleRate;
	}
	
	/**
	 * inbuilt software sine function generator
	 * using the the current parameter of the data to generate a sine wave to display on current data plot
	 */
	public void generateSine () {
		this.clear();
		for (double i=(-(1000*sampleSize/(double)sampleRate)/2); i<((1000*sampleSize/(double)sampleRate)/2); i+=(1000.00/sampleRate)) {
			double yValue = verticalOffset + fxGenOffset + peakToPeak/2*Math.sin(frequency/(1000.00/360.00)*Math.toRadians(i)+horizontalOffset);
			if (yValue<0.000000001 && yValue>(-0.000000001))
				yValue = 0.0;
	    	this.add(i, yValue);
		}
	}
	
	/**
	 * inbuilt software sine function generator
	 * using the the current parameter of the data to generate a triangle wave to display on current data plot
	 */
	public void generateTriangle() {
		this.clear();
		double voltage = verticalOffset + fxGenOffset;
		boolean rising = true;
		for (double i=((1000*sampleSize/(double)sampleRate)/2); i<((1000*sampleSize/(double)sampleRate)/2); i+=(1000.00/sampleRate)) {
			if (voltage<0.000000001 && voltage>(-0.000000001))
				voltage = 0.0;
			this.add(i, voltage);
			if (rising) {
				voltage += (peakToPeak/2/(frequency*sampleSize/(double)sampleRate/4));
				if (voltage >= (peakToPeak/2+verticalOffset)) {
					rising = false;
				}
			} else {
				voltage -= (peakToPeak/2/(frequency*sampleSize/(double)sampleRate/4));
				if (voltage <= (-peakToPeak/2+verticalOffset)) {
					rising = true;
				}
			}
		}
	}
	
	/**
	 * inbuilt software sine function generator
	 * using the the current parameter of the data to generate a line to display on current data plot
	 */
	public void generateLine() {
		this.clear();
		for (double i=(-(1000*sampleSize/(double)sampleRate)/2); i<((1000*sampleSize/(double)sampleRate)/2); i+=(1000.00/sampleRate)) {
			this.add(i, verticalOffset);
		}
		fxGen = false;
	}
	
	/**
	 * return the current visibility status of the data
	 * @return
	 */
	public boolean getChannelVisibility () {
		return channelOnOff;
	}
	
	/**
	 * set the data visibility to status
	 * if visibility is off, store the data into the memory array
	 * if visibility is on and there is data in the memory array, load them
	 * @param status
	 */
	public void setChannelVisibility (boolean status) {
		channelOnOff = status;
		if (!channelOnOff) {
			mem.clear();
			for (int i=0; i<this.getItemCount(); i++) {
				mem.add(this.getDataItem(i));
			}
			this.clear();
		} else {
			if (this.isEmpty()) {
				for (int j=0; j<mem.size(); j++) {
					this.add(mem.get(j));
				}
				mem.clear();
			}
		}
	}
	
	/**
	 * generate data based on the current function gen wave form
	 */
	public void generateData () {
		if (!channelOnOff) {
			return;
		}
		this.clear();
		if (fxGenWave == "Sine") {
			generateSine();
		} else if (fxGenWave == "Triangle") {
			generateTriangle();
		}
		fxGen = true;
	}
	
	/**
	 * return the current on/off status of the function gen
	 * @return
	 */
	public boolean getFxGenStatus () {
		return fxGen;
	}
	
	/**
	 * switch off function generator
	 */
	public void turnOffFxGen () {
		fxGen = false;
	}
	
	/**
	 * switch on function generator
	 */
	public void turnOnFxGen () {
		fxGen = true;
	}
	
	/**
	 * set data waveform to new waveform
	 * @param waveForm
	 */
	public void setFxWave (String waveForm) {
		fxGenWave = waveForm;
	}
	
	/**
	 * display live data parsed in as one string
	 * data needs to be in the format of CSV
	 * activates band pass signal on data
	 * if data is not in low pass mode
	 * @param data
	 */
	public void displayLiveData (String data) {
		if (!channelOnOff) {
			return;
		}
		this.clear();
		String[] dataArray = data.split(",");
		int acquirableSample = sampleSize;
		if (sampleSize > dataArray.length) {
			acquirableSample = dataArray.length;
		}
		for (int i=0; i<acquirableSample; i++) {
			double xValue = (double) (i-acquirableSample/2)*1000/sampleRate;
			try {
				this.add(xValue, (Double.parseDouble(dataArray[i]) + verticalOffset));
			} catch (NumberFormatException nerr) {
				System.out.println("can't pass " + dataArray + " into double!");
			}
		}
		bandPassSignal();
		this.fireSeriesChanged();
	}
	
	/**
	 * apply fir filter on the selected data using the parsed in variables
	 * the filtered data will be into the current data
	 * @param channel
	 * @param variables
	 */
	public void applyFIRFilter (Data channel, DefaultListModel<Double> variables) {
		this.clear();
		System.out.println(channel.getItemCount());
		for(int i=0; i<(channel.getItemCount()); i++) {
			double yValue = 0.00;
			for (int j=0; j<variables.size(); j++) {
				if ((i-j)>=0) {
					yValue += ((double)channel.getY(i-j)*variables.get(j));
				}
			}
			this.add(channel.getX(i), yValue);
		}
	}
	
	/**
	 * apply iir filter on the selected data using the parsed in variables
	 * the filtered data will be into the current data
	 * @param channel
	 * @param variables
	 */
	public void applyIIRFilter (Data channel, DefaultListModel<Double[]> variables) {
		this.clear();
		if (channel.isEmpty()) {
			return;
		}
		this.add(channel.getX(0), 1/variables.get(0)[0]*((double)channel.getY(0)*variables.get(0)[1]));
		for(int i=1; i<(channel.getItemCount()); i++) {
			double firstValue = 0.00;
			double secondValue = 0.00;
			for (int j=0; j<variables.size(); j++) {
				if ((i-j)>=0) {
					firstValue += ((double)channel.getY(i-j)*variables.get(j)[1]);
				}
				if ((i-j)>=0 && j>0) {
					secondValue += ((double)this.getY(i-j)*variables.get(j)[0]);
				}
			}
			double yValue = 1/variables.get(0)[0]*(firstValue - secondValue);
			this.add(channel.getX(i), yValue);
		}
	}
	
	/**
	 * iir testing function, printing out the filtered value to double check with spec
	 * not used in the software
	 * @param outChannel
	 * @param inChannel
	 * @param variables
	 */
	public void testIIRFilter (Data outChannel, Data inChannel, DefaultListModel<Double[]> variables) {
		outChannel.clear();
		outChannel.add(inChannel.getX(0), 1/variables.get(0)[0]*((double)inChannel.getY(0)*variables.get(0)[1]));
		for(int i=1; i<(inChannel.getItemCount()); i++) {
			double firstValue = 0.00;
			double secondValue = 0.00;
			for (int j=0; j<variables.size(); j++) {
				if ((i-j)>=0) {
					firstValue += ((double)inChannel.getY(i-j)*variables.get(j)[1]);
				}
				if ((i-j)>=0 && j>0) {
					secondValue += ((double)outChannel.getY(i-j)*variables.get(j)[0]);
				}
			}
			double yValue = 1/variables.get(0)[0]*(firstValue - secondValue);
			outChannel.add(inChannel.getX(i), yValue);
		}
		for (int k=0; k<outChannel.getItemCount(); k++) {
			System.out.println(outChannel.getY(k));
		}
	}
	
	/**
	 * apply noise to the currently stored data
	 * the noise is randomly generated value within 10% of the actual value
	 * for simulating data influx purposes
	 */
	public void applyNoise () {
		if (this.isEmpty()) {
			return;
		} else {
			this.clear();
			if (fxGen) {
				if (fxGenWave == "Sine") {
					for (double i=(-(1000*sampleSize/(double)sampleRate)/2); i<((1000*sampleSize/(double)sampleRate)/2); i+=(1000.00/sampleRate)) {
						double yValue = verticalOffset + fxGenOffset + peakToPeak/2*Math.sin(frequency/(1000.00/360.00)*Math.toRadians(i)+horizontalOffset);
						yValue *=(1+(Math.random()-0.5)/5.0);
						if (yValue<0.000000001 && yValue>(-0.000000001))
							yValue = 0.0;
				    	this.add(i, yValue);
					}
				} else if (fxGenWave == "Triangle") {
					double voltage = verticalOffset + fxGenOffset;
					boolean rising = true;
					for (double i=(-(1000*sampleSize/(double)sampleRate)/2); i<((1000*sampleSize/(double)sampleRate)/2); i+=(1000.00/sampleRate)) {
						if (voltage<0.000000001 && voltage>(-0.000000001))
							voltage = 0.0;
						this.add(i, voltage*(1+(Math.random()-0.5)/5.0));
						if (rising) {
							voltage += (peakToPeak/2/(frequency*sampleSize/(double)sampleRate/4));
							if (voltage >= (peakToPeak/2+verticalOffset + fxGenOffset)) {
								rising = false;
							}
						} else {
							voltage -= (peakToPeak/2/(frequency*sampleSize/(double)sampleRate/4));
							if (voltage <= (-peakToPeak/2+verticalOffset + fxGenOffset)) {
								rising = true;
							}
						}
					}
				}
			} else {
				for (double i=(-(1000*sampleSize/(double)sampleRate)/2); i<((1000*sampleSize/(double)sampleRate)/2); i+=(1000.00/sampleRate)) {
					this.add(i, verticalOffset*(1+(Math.random()-0.5)/5.0));
				}
			}
		}
	}
}
