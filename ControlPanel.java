package projectview;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class ControlPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5668066109741424643L;
	private GUIMediator gui;
	JButton stepButton = new JButton("Step");
	JButton clearButton = new JButton("Clear");
	JButton runButton = new JButton("Run/Pause");
	JButton reloadButton = new JButton("Reload");
	
	/**
	 * @param gui
	 */
	public ControlPanel(GUIMediator gui) {
		super(new GridLayout(1,0));
		this.gui = gui;
		
		add(stepButton);
		add(clearButton);
		add(runButton);
		add(reloadButton);
		
		stepButton.setBackground(Color.WHITE);
		clearButton.setBackground(Color.WHITE);
		runButton.setBackground(Color.WHITE);
		reloadButton.setBackground(Color.WHITE);
		
		stepButton.addActionListener(e -> gui.step());
		clearButton.addActionListener(e -> gui.clearJob());
		runButton.addActionListener(e->gui.toggleAutoStep());
		reloadButton.addActionListener(e->gui.reload());
		
		JSlider slider = new JSlider(5,1000);
		slider.addChangeListener(e->gui.setPeriod(slider.getValue()));
		add(slider);
	}		
	
	public void update(String arg1) {
		runButton.setEnabled(gui.getCurrentState().getRunPauseActive());
		stepButton.setEnabled(gui.getCurrentState().getStepActive());
		clearButton.setEnabled(gui.getCurrentState().getClearActive());
		reloadButton.setEnabled(gui.getCurrentState().getReloadActive());
	}
}
