package projectview;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import project.Loader;
import project.Model;

public class DataViewPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5336597310267908307L;
	private Model model;
	private JScrollPane scroller;
	private JTextField[] dataHex;
	private JTextField[] dataDecimal;
	private int lower = -1;
	private int upper = -1;
	private int previousColor = -1;

	public DataViewPanel(Model model, int lower, int upper) {
		super();
		this.model = model;
		this.lower = lower;
		this.upper = upper;
	
		JPanel innerPanel = new JPanel();
		JPanel numPanel = new JPanel();
		JPanel decimalPanel = new JPanel();
		JPanel hexPanel = new JPanel();
		setLayout(new BorderLayout());
		Border border = BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLACK), 
				"Data Memory View [" + lower + "-" + upper + "]",
				TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION);
		setBorder(border);
		innerPanel.setLayout(new BorderLayout());
		numPanel.setLayout(new GridLayout(0,1));
		decimalPanel.setLayout(new GridLayout(0,1));
		hexPanel.setLayout(new GridLayout(0,1));
		innerPanel.add(numPanel, BorderLayout.LINE_START);
		innerPanel.add(decimalPanel, BorderLayout.CENTER);
		innerPanel.add(hexPanel, BorderLayout.LINE_END);
		dataHex = new JTextField[upper-lower];
		dataDecimal = new JTextField[upper-lower];
		for(int i = lower; i < upper; i++) {
			numPanel.add(new JLabel(i+": ", JLabel.RIGHT));
			dataDecimal[i - lower] = new JTextField(10);
			dataHex[i-lower] = new JTextField(10);
			decimalPanel.add(dataDecimal[i-lower]);
			hexPanel.add(dataHex[i-lower]);
		}
		scroller = new JScrollPane(innerPanel);
		add(scroller);
	}

	public void update(String arg1) {
		for(int i = lower; i < upper; i++) {
			int val = model.getData(i);
			dataDecimal[i-lower].setText("" + val);
			String s = Integer.toHexString(val);
			if(val < 0)
				s = "-" + Integer.toHexString(-val);
			dataHex[i-lower].setText(s.toUpperCase());
		}
		if(arg1 != null && arg1.equals("Clear")) {
			if(lower <= previousColor && previousColor < upper) {
				dataDecimal[previousColor-lower].setBackground(Color.WHITE);
				dataHex[previousColor-lower].setBackground(Color.WHITE);
				previousColor = -1;
			}
		} else {
			if(previousColor  >= lower && previousColor < upper) {
				dataDecimal[previousColor-lower].setBackground(Color.WHITE);
				dataHex[previousColor-lower].setBackground(Color.WHITE);
			}
			previousColor = model.getChangeIndex();
			if(previousColor  >= lower && previousColor < upper) {
				dataDecimal[previousColor-lower].setBackground(Color.YELLOW);
				dataHex[previousColor-lower].setBackground(Color.YELLOW);
			} 
		}
		if(scroller != null && model != null) {
			JScrollBar bar= scroller.getVerticalScrollBar();
			if (model.getChangeIndex() >= lower &&
					model.getChangeIndex() < upper &&
					// the following just checks createMemoryDisplay has run
					dataDecimal != null) {
				Rectangle bounds = dataDecimal[model.getChangeIndex()-lower].getBounds();
				bar.setValue(Math.max(0, bounds.y - 15*bounds.height));
			}
		}
	}

	public static void main(String[] args) {
		Model model = new Model();
		DataViewPanel panel = new DataViewPanel(model, 0, 500);
		JFrame frame = new JFrame("TEST");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 700);
		frame.setLocationRelativeTo(null);
		frame.add(panel);
		frame.setVisible(true);
		System.out.println(Loader.load(model, new File("src/project/pexe/test.pexe"), 0, 0));
		panel.update((String)null);
	}
}
