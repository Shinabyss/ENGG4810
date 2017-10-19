package GUI;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import communication.TCPClient;
import filterchannel.FilterChannel;
import mainframecontrol.ChannelAControl;
import mainframecontrol.ChannelBControl;
import mainframecontrol.PlotAreaControl;
import mathchannel.MathChannel;
import plotdisplay.PlotArea;

/**
 * 
 * @author Michael Connor
 * 
 * DigiScope GUI
 * Construct the GUI with control and display and
 * display on PC
 *
 */
public class DigitalOscilloscopeGUI extends JFrame {
	
	//Essential Objects
	private Container contentpane;
	private PlotArea plotArea = new PlotArea();
	private ChannelAControl control1;
	private ChannelBControl control2;
	private PlotAreaControl plotControl;
	private FilterChannel filterChannel;
	private MathChannel mathChannel;
	private TCPClient connection;
	private String ipAddress;
	
	/**
	 * Constructor setting up objects
	 */
	public DigitalOscilloscopeGUI() {

		contentpane = getContentPane();
		contentpane.setLayout(new BoxLayout(contentpane, BoxLayout.PAGE_AXIS));
		connection = new TCPClient(plotArea);
		addConnectionControl(contentpane);
		addPlotArea(contentpane);
		addControlArea(contentpane);
		connection.addEssentials(plotControl, control1, control2);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	/**
	 * Adding the connection control to the GUI
	 * @param container
	 */
	private void addConnectionControl (Container container) {
		JPanel connectionControl = new JPanel();
		connectionControl.setLayout(new FlowLayout());
		JTextField IPAddress = new JTextField();
		IPAddress.setColumns(15);
		IPAddress.setText("192.168.137.8");
		JButton connect = new JButton ("Connect");
		JButton startProcess = new JButton ("Start");
		JButton endProcess = new JButton ("Stop");
		JButton disconnect = new JButton ("Disconnect");
		JLabel initMessage = new JLabel(connection.getInitMessage());
		
		//add action listeners to all buttons
		connect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ipAddress = IPAddress.getText();
				try {
					connection.setup(ipAddress);
				} catch (Exception err) {
					JOptionPane.showMessageDialog(null, "Connection Failed " + ipAddress, "Connection Error", JOptionPane.ERROR_MESSAGE);
				}
				initMessage.setText(connection.getInitMessage());
			}
		});
		disconnect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				connection.disconnect();
				initMessage.setText(connection.getInitMessage());
			}
		});
		startProcess.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				connection.run();
				
			}
		});
		endProcess.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				connection.setCommunication(false);
			}
		});
		connectionControl.add(IPAddress);
		connectionControl.add(connect);
		connectionControl.add(startProcess);
		connectionControl.add(endProcess);
		connectionControl.add(disconnect);
		connectionControl.add(initMessage);
		container.add(connectionControl);		
	}
	
	/**
	 * Add the plot area to display data on 10x10 grid
	 * @param container
	 */
	private void addPlotArea (Container container) {
		
		JPanel plot = new JPanel();
		plot.setLayout(new BoxLayout(plot, BoxLayout.PAGE_AXIS));
		plot.setSize(1000, 600);
		plot.add(plotArea.getChartPanel());
		plotControl = new PlotAreaControl(plotArea, connection);
		plot.add(plotControl);
		container.add(plot);	
	}
	
	/**
	 * add control area as tabbed panels containing ChA, ChB, Filter and Math channels
	 * @param container
	 */
	private void addControlArea (Container container) {
		
		JTabbedPane controlArea = new JTabbedPane();
		control1 = new ChannelAControl(plotArea, connection, plotControl);
		control2 = new ChannelBControl(plotArea, connection, plotControl);
		filterChannel = new FilterChannel(plotArea);
		mathChannel = new MathChannel(plotArea);
		controlArea.addTab("Channel A", control1);
		controlArea.addTab("Channel B", control2);
		controlArea.addTab("Filter Channel", filterChannel);
		controlArea.addTab("Math Channel", mathChannel);
		container.add(controlArea, BorderLayout.CENTER);
	}

}
