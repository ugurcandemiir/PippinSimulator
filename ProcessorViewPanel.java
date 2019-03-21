package projectview;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import project.Model;

public class ProcessorViewPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3518918860540674834L;
	Model model;
	JTextField acc = new JTextField();
	JTextField ip = new JTextField();
	JTextField base = new JTextField();
	/**
	 * @param model
	 */
	public ProcessorViewPanel(Model model) {
		super(new GridLayout(1,0));
		this.model = model;
	
		add(new JLabel("Accumulator: ", JLabel.RIGHT));
		add(acc);
		add(new JLabel("Instruction Pointer: ", JLabel.RIGHT));
		add(ip);
		add(new JLabel("Memory Base: ", JLabel.RIGHT));
		add(base);
	}
	
	public void update(String arg1) {
		if (model!= null) {
			acc.setText("" + model.getAccum());
			ip.setText("" + model.getInstrPtr());
			base.setText("" + model.getMemBase());
		}
	}
	
	public static void main(String[] args) { // Test to make sure class works
		Model model = new Model();
		ProcessorViewPanel panel = new ProcessorViewPanel(model);
		JFrame frame = new JFrame("TEST");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(700, 60);
		frame.setLocationRelativeTo(null);
		frame.add(panel);
		frame.setVisible(true);
	}
	
}
