package mathchannel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.fathzer.soft.javaluator.DoubleEvaluator;

import mainframecontrol.MeasurementDisplay;
import plotdisplay.PlotArea;

/**
 * Math channel control class
 * pass a math expression to be calculated in data class
 * @author Michael Connor
 *
 */
public class MathChannel extends JPanel {
	
	//Essential objects and variables
	private PlotArea plotArea;
	private String expression;
	private JTextField inputArea = new JTextField();
	private JButton generate = new JButton("On");
	private JButton refresh = new JButton("Refresh");
	private DoubleEvaluator evaluator = new DoubleEvaluator();
	private ArrayList<Double> variableA = new ArrayList<Double>();
	private ArrayList<Double> variableB = new ArrayList<Double>();
	private MeasurementDisplay measurements;
	
	/**
	 * constructor setup variable and objects
	 * @param plotArea
	 */
	public MathChannel (PlotArea plotArea) {
		this.plotArea = plotArea;
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		JPanel control = new JPanel();
		inputArea.setColumns(50);
		measurements = plotArea.getDisplayData().getMathMeasure();
		addActionListener();
		variableA.add(1.0);
		variableA.add(2.0);
		variableA.add(3.0);
		variableB.add(9.0);
		variableB.add(7.0);
		variableB.add(5.0);
		control.add(inputArea);
		control.add(generate);
		control.add(refresh);
		this.add(control);
		this.add(measurements);
	}
	
	/**
	 * add commands to buttons
	 */
	private void addActionListener () {
		
		//refresh math channel, will change plot if math expression changes
		refresh.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (plotArea.getDisplayData().mathStatus()) {
					expression = inputArea.getText();
					plotArea.getDisplayData().setMathExpression(expression);
					plotArea.getDisplayData().applyMathExpression();
					plotArea.getDisplayData().applyFilter();
				}
			}
		});
		
		//generate math channel plot by calculating the given math expression toggle math status on/off
		generate.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				expression = inputArea.getText();
				plotArea.getDisplayData().setMathExpression(expression);
				if (plotArea.getDisplayData().mathStatus()) {
					plotArea.getDisplayData().setMathStatus(false);
					plotArea.getDisplayData().getMathData().clear();
					if (plotArea.getDisplayData().getSourceChannel() == "Math") {
						plotArea.getDisplayData().getFilterData().clear();
					}
					measurements.zeroAllMeasurements();
					generate.setText("On");
				} else {
					plotArea.getDisplayData().setMathStatus(true);
					plotArea.getDisplayData().applyMathExpression();
					plotArea.getDisplayData().applyFilter();
					plotArea.getDisplayData().refreshChannel();
					generate.setText("Off");
					if (plotArea.getDisplayData().getMathData().isEmpty()) {
						plotArea.getDisplayData().setMathStatus(false);
						measurements.zeroAllMeasurements();
						generate.setText("On");
					}				
				}
			}
		});
	}
}
