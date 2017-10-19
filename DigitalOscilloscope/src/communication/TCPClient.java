package communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.sun.corba.se.impl.protocol.ServantCacheLocalCRDBase;

import mainframecontrol.ChannelAControl;
import mainframecontrol.ChannelBControl;
import mainframecontrol.PlotAreaControl;
import plotdisplay.PlotArea;

/**
 * 
 * @author Michael Connor
 *Communication class handle in and out packets
 */
public class TCPClient {
	
	//Objects to be used for class
	private boolean connectionEstablished;
	private SocketChannel sock;
	private InetSocketAddress sockAddress;
	private PlotArea plotArea;
	private boolean communicate = true;
	private String initialMessage = "No Connection";
	private PlotAreaControl plotControl;
	private ChannelAControl channelAControl;
	private ChannelBControl channelBControl;
	private ByteBuffer byteReader1;
	private ByteBuffer byteReader2;
	private ByteBuffer byteReader4;
	private ByteBuffer stringReader;
	private ByteBuffer dataReader;
	
	/**
	 * create a new connection class to control Plot Area
	 * 
	 * @param plotArea
	 */
	public TCPClient (PlotArea plotArea) {
		this.plotArea = plotArea;
	}
	
	/**
	 * 
	 * Adding required components to TCP
	 * @param plotControl
	 * @param channelAControl
	 * @param channelBControl
	 */
	public void addEssentials (PlotAreaControl plotControl, ChannelAControl channelAControl, ChannelBControl channelBControl) {
		this.plotControl = plotControl;
		this.channelAControl = channelAControl;
		this.channelBControl = channelBControl;
	}
	
	/**
	 * disconnect current connection
	 */
	public void disconnect () {
		if (sock.isOpen()) {
			try {
				communicate = false;
				sock.close();
				initialMessage = "No Connection";
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("nothing to disconnect");
			}
		}
	}
	
	/**
	 * start current connection on the IP address given
	 * takes the first packet of data as string and set it as initMessage
	 * throw exception if connection cannot be established 
	 * @param IPAddress
	 * @throws Exception
	 */
	public void setup(String IPAddress) throws Exception {
		sockAddress = new InetSocketAddress(IPAddress, 1000);
		sock = SocketChannel.open(sockAddress);
		byteReader1 = ByteBuffer.allocate(1);
		byteReader1.order(ByteOrder.LITTLE_ENDIAN);
		byteReader2 = ByteBuffer.allocate(2);
		byteReader2.order(ByteOrder.LITTLE_ENDIAN);
		byteReader4 = ByteBuffer.allocate(4);
		byteReader4.order(ByteOrder.LITTLE_ENDIAN);
		stringReader = ByteBuffer.allocate(1024);
		stringReader.order(ByteOrder.LITTLE_ENDIAN);
		dataReader = ByteBuffer.allocate(80000);
		dataReader.order(ByteOrder.LITTLE_ENDIAN);
		
		
		

		String message = "";
		int count = sock.read(stringReader);
		System.out.println(count);
		stringReader.clear();
		for (int i=0; i<count; i++) {
			message += (char)stringReader.get();
		}
		if (message.length() == 0) {
			message = "No Connection";
		}

		initialMessage = message;
		System.out.println(message);
		sock.configureBlocking(false);
		
	}
	
	/**
	 * set reading communication to status
	 * @param status
	 */
	public void setCommunication (boolean status) {
		communicate = status;
	}
	
	/**
	 * return if the program is currently able to read data from hardware
	 * @return
	 */
	public boolean getComStatus () {
		return communicate;
	}
	
	/**
	 * start reading packet function
	 * this function will also downscale in data into processable values
	 * then construct a string using those values
	 * this then send the string value to the dataProcess function
	 */
	public void run () {
		communicate = true;
		Thread thread = new Thread() {
			public void run () {
				Integer counter = 0;
				while (communicate) {
					ArrayList<Short> dataValue = new ArrayList<Short>();
					StringBuilder sb = new StringBuilder();
					int status;
					Short inValue;
					String inPacket = "";
					dataReader.clear();
					byteReader1.clear();
					byteReader2.clear();
					byteReader4.clear();
					try {
						if (sock.read(byteReader2) == 2) {
							byteReader2.flip();
							char header1 = (char)byteReader2.get();
							char header2 = (char)byteReader2.get();
							System.out.println("Received Header: " + header1 + header2);
							sb.append(header1);
							sb.append(header2);
							byteReader2.clear();
							if (sb.charAt(0) == 'O' && sb.charAt(1) == 'V' ) {
								inPacket = sb.toString();
								System.out.println(inPacket);
							} else if (sb.charAt(0) == 'A') {
								if (sb.charAt(1) == 'D') {
									System.out.println("Received Data");
									if (plotArea.isEightBits()) {
										dataReader.limit(plotArea.getDisplayData().getChannelAData().getSampleSize());
									} else {
										dataReader.limit(plotArea.getDisplayData().getChannelAData().getSampleSize()*2);
									}
									while (dataReader.remaining()!=0) {
										sock.read(dataReader);
									}
									dataReader.flip();
									while (dataReader.hasRemaining()) {
										sb.append(",");
										if (plotArea.isEightBits()) {
											sb.append(Double.toString(((int)dataReader.get())/25.5-5));
										} else {
											sb.append(Double.toString(dataReader.getShort()/409.5-5));
										}
									}
									inPacket = sb.toString();
									System.out.println(inPacket);
								} else if (sb.charAt(1) == 'C') {
									if (sock.read(byteReader2) == 2) {
										byteReader2.flip();
										char Control1 = (char)byteReader2.get();
										char Control2 = (char)byteReader2.get();
										System.out.print("Received Control: " + Control1 + Control2);
										sb.append(Control1);
										sb.append(Control2);
										byteReader2.clear();
										if (sb.charAt(2) == 'F') {
											if (sock.read(dataReader) >= 10) {
												dataReader.flip();
												sb.append(",");
												sb.append(Short.toString(dataReader.getShort()));
												sb.append(",");
												sb.append(Short.toString(dataReader.getShort()));
												sb.append(",");
												sb.append(Double.toString(dataReader.getShort()*0.001));
												sb.append(",");
												sb.append(Double.toString(dataReader.getShort()*0.005));
												sb.append(",");
												sb.append(Short.toString(dataReader.getShort()));
												inPacket = sb.toString();
												if (dataReader.hasRemaining()) {
													System.out.println("Unnecessary Data =" + dataReader.getShort());
												}
												System.out.println("Fx Gen Control Received: " + inPacket);
											}
											dataReader.clear();
										} else if (sb.charAt(2) == 'C') {
											int Byteread = sock.read(dataReader);
											System.out.println("Channel Control Just Read " + Byteread + " Bytes");
											dataReader.flip();
											if (Byteread >= 6) {
												sb.append(",");
												sb.append(Double.toString(dataReader.getShort()*0.001));
												sb.append(",");
												sb.append(Double.toString(dataReader.getInt()*0.001));
												inPacket = sb.toString();
												if (dataReader.hasRemaining()) {
													System.out.println("Unnecessary Data =" + dataReader.getShort());
												}
												System.out.println("Ch Control Received: " + inPacket);
											}
											dataReader.clear();
										} else if (sb.charAt(2) == 'T') {
											if (sock.read(dataReader) >= 12) {
												dataReader.flip();
												sb.append(",");
												sb.append(dataReader.getChar());
												sb.append(",");
												sb.append(dataReader.getChar());
												sb.append(",");
												sb.append(Double.toString(dataReader.getShort()*-0.005));
												dataReader.position((dataReader.position()+4));
												sb.append(",");
												sb.append((char)dataReader.getShort());
												inPacket = sb.toString();
												if (dataReader.hasRemaining()) {
													System.out.println("Unnecessary Data =" + dataReader.getShort());
												}
												System.out.println("Trigger Control Received: " + inPacket);
											}
											dataReader.clear();
										}
									}
								}
								
							} else if (sb.charAt(0) == 'B') {
								if (sb.charAt(1) == 'D') {
									System.out.println("Received Data");
									if (plotArea.isEightBits()) {
										dataReader.limit(plotArea.getDisplayData().getChannelAData().getSampleSize());
									} else {
										dataReader.limit(plotArea.getDisplayData().getChannelAData().getSampleSize()*2);
									}
									while (dataReader.remaining()!=0) {
										sock.read(dataReader);
									}
									dataReader.flip();
									while (dataReader.hasRemaining()) {
										sb.append(",");
										if (plotArea.isEightBits()) {
											sb.append(Double.toString(((int)dataReader.get())/25.5-5));
										} else {
											sb.append(Double.toString(dataReader.getShort()/409.5-5));
										}
									}
									inPacket = sb.toString();
									System.out.println(inPacket);
								} else if (sb.charAt(1) == 'C') {
									if (sock.read(byteReader2) == 2) {
										byteReader2.flip();
										char Control1 = (char)byteReader2.get();
										char Control2 = (char)byteReader2.get();
										System.out.print("Received Control: " + Control1 + Control2);
										sb.append(Control1);
										sb.append(Control2);
										byteReader2.clear();
										if (sb.charAt(2) == 'F') {
											if (sock.read(dataReader) >= 10) {
												dataReader.flip();
												sb.append(",");
												sb.append(Short.toString(dataReader.getShort()));
												sb.append(",");
												sb.append(Short.toString(dataReader.getShort()));
												sb.append(",");
												sb.append(Double.toString(dataReader.getShort()*0.001));
												sb.append(",");
												sb.append(Double.toString(dataReader.getShort()*0.005));
												sb.append(",");
												sb.append(Short.toString(dataReader.getShort()));
												inPacket = sb.toString();
												if (dataReader.hasRemaining()) {
													System.out.println("Unnecessary Data =" + dataReader.getShort());
												}
												System.out.println("Fx Gen Control Received: " + inPacket);
											}
											dataReader.clear();
										} else if (sb.charAt(2) == 'C') {
											int Byteread = sock.read(dataReader);
											System.out.println("Channel Control Just Read " + Byteread + " Bytes");
											dataReader.flip();
											if (Byteread >= 6) {
												sb.append(",");
												sb.append(Double.toString(dataReader.getShort()*0.001));
												sb.append(",");
												sb.append(Double.toString(dataReader.getInt()*0.001));
												inPacket = sb.toString();
												if (dataReader.hasRemaining()) {
													System.out.println("Unnecessary Data =" + dataReader.getShort());
												}
												System.out.println("Ch Control Received: " + inPacket);
											}
											dataReader.clear();
										} else if (sb.charAt(2) == 'T') {
											if (sock.read(dataReader) >= 12) {
												dataReader.flip();
												sb.append(",");
												sb.append(dataReader.getChar());
												sb.append(",");
												sb.append(dataReader.getChar());
												sb.append(",");
												sb.append(Double.toString(dataReader.getShort()*-0.005));
												dataReader.position((dataReader.position()+4));
												sb.append(",");
												sb.append((char)dataReader.getShort());
												inPacket = sb.toString();
												if (dataReader.hasRemaining()) {
													System.out.println("Unnecessary Data =" + dataReader.getShort());
												}
												System.out.println("Trigger Control Received: " + inPacket);
											}
											dataReader.clear();
										}
									}
								}
							} 
							dataReader.clear();
						}
						
						byteReader2.clear();
						
						if (inPacket.length()<50 && inPacket.length()>0 ) {
							System.out.println("Config Received: " + inPacket + "with byte values: " + Integer.toBinaryString(inPacket.charAt(0)) + Integer.toBinaryString(inPacket.charAt(1)) );
						}
						if (inPacket.length() > 0) {
							dataProcess(inPacket);
						} 
						Thread.sleep(20);
					} catch (Exception e1) {
						e1.printStackTrace();
						communicate = false;
					}
				}
			
			}
		
		};
		thread.start();
	}
	
	/**
	 * return the initial connection message
	 * @return
	 */
	public String getInitMessage() {
		return initialMessage;
	}
	
	/**
	 * packet sender function
	 * for sending Byte values
	 * @param header
	 * @param value
	 */
	public void sendByte (byte data) {
		try {
			ByteBuffer byteBuff = ByteBuffer.allocate(1);
			byteBuff.order(ByteOrder.LITTLE_ENDIAN);
			byteBuff.put(data);
			byteBuff.flip();
			sock.write(byteBuff);
		} catch (Exception err) {
			JOptionPane.showMessageDialog(null, "Communication Failure "
					+ "- Cannot Find Hardware!", 
					"Communication Error", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	/**
	 * packet sender function
	 * for sending integer value with a String header
	 * @param header
	 * @param value
	 */
	public void sendVariablePack (String header, int value) {
		try {
			CharBuffer strBuff = CharBuffer.wrap(header.toCharArray());
			ByteBuffer intBuff = ByteBuffer.allocate(4);
			intBuff.order(ByteOrder.LITTLE_ENDIAN);
			intBuff.putInt(value);
			ByteBuffer[] packet = new ByteBuffer[]{
					Charset.defaultCharset().encode(strBuff),
					intBuff
			};
			sock.write(packet);
		} catch (Exception err) {
			JOptionPane.showMessageDialog(null, "Communication Failure "
					+ "- Cannot Find Hardware!", 
					"Communication Error", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	/**
	 * packet sender function
	 * for sending multiple Integer values with a string header
	 * @param header
	 * @param values
	 */
	public void sendMultiVariablePack (String header, int[] values) {
		try {
			CharBuffer strBuff = CharBuffer.wrap(header.toCharArray());
			ByteBuffer intBuff = ByteBuffer.allocate(20);
			intBuff.order(ByteOrder.LITTLE_ENDIAN);
			for (int i: values) {
				intBuff.putInt(i);
			}
			intBuff.flip();
			ByteBuffer[] packet = new ByteBuffer[]{
					Charset.defaultCharset().encode(strBuff),
					intBuff
			};
			sock.write(packet);
		} catch (Exception err) {
			JOptionPane.showMessageDialog(null, "Communication Failure "
					+ "- Cannot Find Hardware!", 
					"Communication Error", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	/**
	 * packet sender function
	 * for sending string values
	 * @param message
	 */
	public void sendMessage(String message) {
		try {
			CharBuffer strBuff = CharBuffer.wrap(message.toCharArray());
			sock.write(Charset.defaultCharset().encode(strBuff));
		} catch (Exception err) {
			JOptionPane.showMessageDialog(null, "Communication Failure "
					+ "- Cannot Find Hardware!", 
					"Communication Error", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	/**
	 * for processing packets after downscaling into processable value and turned into string
	 * @param message
	 */
	private void dataProcess (String message) {
		String header = message.substring(0, 2);
		System.out.println("Processing Data with header: " + header);
		switch (header) {
			case "OV":
				setCommunication(false);
				JOptionPane.showMessageDialog(null, "WARNING! Input voltage exceeds "
						+ "hardware tolerence! - Stopping Aquisition", 
						"Hardware Tolerence Warning", JOptionPane.WARNING_MESSAGE);
			case "AC":
				plotControl.setTrigSource("AC");
				String departmentHeaderA = message.substring(2, 4);
				switch (departmentHeaderA) {
				case "FG":
					String fgValues[] = message.substring(5).split(",");
					for (int i=0; i<fgValues.length; i++) {
						System.out.println(fgValues[i]);
					}
					channelAControl.setFxGenStatus(fgValues[0]);
					switch (fgValues[1]) {
						case "0":
							channelAControl.setFxGenWave("Sine");
							break;
						case "1":
							channelAControl.setFxGenWave("Square");
							break;
						case "2":
							channelAControl.setFxGenWave("Ramp");
							break;
						case "3":
							channelAControl.setFxGenWave("Triangle");
							break;
						case "4":
							channelAControl.setFxGenWave("Noise");
							break;
						default:
							break;
					}
					channelAControl.setFxGenValue(fgValues[2], fgValues[3], fgValues[4]);
					break;
				case "TR":
					String trValues[] = message.substring(5).split(",");
					switch (trValues[0]) {
						case "A":
							plotControl.setTrigMode("Auto");
							break;
						case "N":
							plotControl.setTrigMode("Normal");
							break;
						case "S":
							plotControl.setTrigMode("Single");
							break;
						default:
							break;
					}
					switch (trValues[1]) {
						case "R":
							plotControl.setTrigType("Rising");
							break;
						case "F":
							plotControl.setTrigType("Falling");
							break;
						case "L":
							plotControl.setTrigType("Level");
							break;
						default:
							break;
					}
					plotControl.setTrigThreshold(Double.parseDouble(trValues[2]));
					switch (trValues[3]) {
						case "3":
							plotControl.setTrigStatus("Stopped");
							break;
						case "1":
							plotControl.setTrigStatus("Triggered");
							break;
						default:
							plotControl.setTrigStatus("Armed");
						}
					break;
				case "CD":
					channelAControl.setCoupling('D');
					String pldValues[] = message.substring(5).split(",");
					plotControl.updateVoltageValue(Double.parseDouble(pldValues[0]));
					plotControl.updateTimeValue(Double.parseDouble(pldValues[1]));
					break;
				case "CA":
					channelAControl.setCoupling('A');
					String plaValues[] = message.substring(5).split(",");
					plotControl.updateVoltageValue(Double.parseDouble(plaValues[0]));
					plotControl.updateTimeValue(Double.parseDouble(plaValues[1]));
					break;
				default:

				}
				break;
			case "BC":
				plotControl.setTrigSource("BC");
				String departmentHeaderB = message.substring(2, 4);
				switch (departmentHeaderB) {
				case "FG":
					String fgValues[] = message.substring(5).split(",");
					channelBControl.setFxGenStatus(fgValues[1]);
					switch (fgValues[1]) {
						case "0":
							channelBControl.setFxGenWave("Sine");
							break;
						case "1":
							channelBControl.setFxGenWave("Square");
							break;
						case "2":
							channelBControl.setFxGenWave("Ramp");
							break;
						case "3":
							channelBControl.setFxGenWave("Triangle");
							break;
						case "4":
							channelBControl.setFxGenWave("Noise");
							break;
						default:
							break;
					}
					channelBControl.setFxGenValue(fgValues[2], fgValues[3], fgValues[4]);
					break;
				case "TR":
					String trValues[] = message.substring(5).split(",");
					switch (trValues[0]) {
						case "A":
							plotControl.setTrigMode("Auto");
							break;
						case "N":
							plotControl.setTrigMode("Normal");
							break;
						case "S":
							plotControl.setTrigMode("Single");
							break;
						default:
							break;
					}
					switch (trValues[1]) {
						case "R":
							plotControl.setTrigType("Rising");
							break;
						case "F":
							plotControl.setTrigType("Falling");
							break;
						case "L":
							plotControl.setTrigType("Level");
							break;
						default:
							break;
					}
					plotControl.setTrigThreshold(Double.parseDouble(trValues[2]));
					switch (trValues[3]) {
						case "3":
							plotControl.setTrigStatus("Stopped");
							break;
						case "1":
							plotControl.setTrigStatus("Triggered");
							break;
						default:
							plotControl.setTrigStatus("Armed");
							break;
						}
					break;
				case "CD":
					channelBControl.setCoupling('D');
					String pldValues[] = message.substring(5).split(",");
					plotControl.updateVoltageValue(Double.parseDouble(pldValues[0]));
					plotControl.updateTimeValue(Double.parseDouble(pldValues[1]));
					break;
				case "CA":
					channelBControl.setCoupling('A');
					String plaValues[] = message.substring(5).split(",");
					plotControl.updateVoltageValue(Double.parseDouble(plaValues[0]));
					plotControl.updateTimeValue(Double.parseDouble(plaValues[1]));
					break;
				default:

				}
				break;
			case "AD":
				plotControl.setTrigSource("AC");
				plotControl.setTrigStatus("Triggered");
				System.out.println("Plotting In Ch A");
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						System.out.println("Generate Plot on chA");
						plotArea.loadDataToChannelA(message.substring(2));
						System.out.println("Plot Complete on chA");
						sendMessage("E");
						plotControl.setTrigStatus("Armed");
					}
				});
				break;
			case "BD":
				plotControl.setTrigSource("BC");
				plotControl.setTrigStatus("Triggered");
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						System.out.println("Generate Plot on chB");
						plotArea.loadDataToChannelB(message.substring(2));
						System.out.println("Plot Complete on chB");
						sendMessage("E");
						plotControl.setTrigStatus("Armed");
					}
				});
				break;
			default:
				break;
		}
	}
}
