package mainframecontrol;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class Channel1Control extends JPanel {
	
	private JButton visibility = new JButton("on/off");
	private JButton couplingAC = new JButton("AC Coupling");
	private JButton couplingDC = new JButton("DC Coupling");
	private JButton setChannelOffset = new JButton("Set");
	private JButton fxGeneratorOnOff = new JButton("Funtion Generator On/Off");
	
	public Channel1Control () {
		this.setLayout(new FlowLayout());
		this.add(visibility);
		this.add(couplingAC);
		this.add(couplingDC);
		this.add(setChannelOffset);
		this.add(fxGeneratorOnOff);
	}
	
	private void addActionListener() {
		visibility.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}

}
