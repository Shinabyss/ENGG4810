package plotdisplay;

import java.awt.BasicStroke;
import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

public class Channels {
	
	private JFreeChart xylineChart;
	private ChartPanel chartPanel;
	
	public Channels(String channelName) {
	    xylineChart = ChartFactory.createXYLineChart(
	         channelName ,
	         "Time" ,
	         "Voltage" ,
	         createDataset(),
	         PlotOrientation.VERTICAL ,
	         true , true , false);
		         
		chartPanel = new ChartPanel( xylineChart );
	    chartPanel.setPreferredSize( new java.awt.Dimension( 600 , 250 ) );
	    final XYPlot plot = xylineChart.getXYPlot( );
	    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer( );
	    renderer.setSeriesPaint( 0 , Color.RED );
	    renderer.setSeriesPaint( 1 , Color.GREEN );
	    renderer.setSeriesPaint( 2 , Color.YELLOW );
	    renderer.setSeriesStroke( 0 , new BasicStroke( 4.0f ) );
	    renderer.setSeriesStroke( 1 , new BasicStroke( 3.0f ) );
	    renderer.setSeriesStroke( 2 , new BasicStroke( 2.0f ) );
	    plot.setRenderer( renderer );
		
	}
	
	public ChartPanel getChartPanel() {
		
		return chartPanel;
		
	}
	   
	private XYDataset createDataset( )
	{
	    final XYSeries triangle = new XYSeries("");          
	    /*triangle.add( 1.0 , 0 );          
	    triangle.add( 2.0 , 5 );          
	    triangle.add( 4.0 , -5 );
	    triangle.add( 6.0 , 5 );          
	    triangle.add( 8.0 , -5 );
	    triangle.add( 10.0 , 5 );          
	    triangle.add( 12.0 , -5 );
	    triangle.add( 14.0 , 5 );          
	    triangle.add( 16.0 , -5 );
	    triangle.add( 18.0 , 5 );          
	    triangle.add( 20.0 , -5 );
	    triangle.add( 22.0 , 5 );          
	    triangle.add( 24.0 , -5 );
	    triangle.add( 26.0 , 5 );          
	    triangle.add( 28.0 , -5 );
	    triangle.add( 30.0 , 5 );          
	    triangle.add( 32.0 , -5 );
	    triangle.add( 34.0 , 5 );          
	    triangle.add( 36.0 , -5 );
	    triangle.add( 38.0 , 5 );          
	    triangle.add( 40.0 , -5 );*/
	    for (int i=0; i<900; i++) {
	    	triangle.add(i, Math.sin(Math.toRadians(i)));
	    }
	    
	    final XYSeriesCollection dataset = new XYSeriesCollection( );          
	    dataset.addSeries( triangle );          
	    return dataset;
	   }

}
