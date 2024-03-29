package projectview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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

import project.Code;
import project.Loader;
import project.Model;

public class CodeViewPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5281966007792010868L;
	Model model;
	Code code;
	JScrollPane scroller;
	JTextField[] codeHex = new JTextField[Code.CODE_MAX];
	JTextField[] codeText = new JTextField[Code.CODE_MAX];
	int previousColor = -1;
	
	public CodeViewPanel(Model mdl) {
		super();
		model=mdl;
	
		JPanel innerPanel = new JPanel();
		JPanel numPanel = new JPanel();
		JPanel textPanel = new JPanel();
		JPanel hexPanel = new JPanel();
		setPreferredSize(new Dimension(300,150));
		setLayout(new BorderLayout());
		Border border = BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLACK), 
				"Code Memory View",
				TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION);
		setBorder(border);
		innerPanel.setLayout(new BorderLayout());
		numPanel.setLayout(new GridLayout(0,1));
		textPanel.setLayout(new GridLayout(0,1));
		hexPanel.setLayout(new GridLayout(0,1));
		for(int i = 0; i < Code.CODE_MAX; i++) {
			numPanel.add(new JLabel(i+": ", JLabel.RIGHT));
			codeText[i] = new JTextField(10);
			codeHex[i] = new JTextField(10);
			textPanel.add(codeText[i]);
			hexPanel.add(codeHex[i]);
		}
		innerPanel.add(numPanel, BorderLayout.LINE_START);
		innerPanel.add(textPanel, BorderLayout.CENTER);
		innerPanel.add(hexPanel, BorderLayout.LINE_END);
		
		scroller = new JScrollPane(innerPanel);
		add(scroller);
	}
	
	public void update(String arg1) {
		if(arg1 != null && arg1.equals("Load Code")) {
			code = model.getCodeMemory();
			int offset = model.getCurrentJob().getStartcodeIndex();
			for(int i = offset; 
					i < offset + model.getCurrentJob().getCodeSize(); i++) {
				codeText[i].setText(code.getText(i));
				codeHex[i].setText(code.getHex(i));
			}	
			previousColor = model.getInstrPtr();			
			codeHex[previousColor].setBackground(Color.YELLOW);
			codeText[previousColor].setBackground(Color.YELLOW);
		} else if(arg1 != null && arg1 instanceof String && ((String)arg1).startsWith("Clear")) {
			int offset = model.getCurrentJob().getStartcodeIndex();
			int codeSize = Integer.parseInt(((String)arg1).substring(6).trim());
			for(int i = offset; 
				i < offset + codeSize; i++) {
				codeText[i].setText("");
				codeHex[i].setText("");
			}	
			if(previousColor >= 0 && previousColor < Code.CODE_MAX) {
				codeText[previousColor].setBackground(Color.WHITE);
				codeHex[previousColor].setBackground(Color.WHITE);
			}
			previousColor = -1;
		}		
		if(this.previousColor >= 0 && previousColor < Code.CODE_MAX) {
			codeText[previousColor].setBackground(Color.WHITE);
			codeHex[previousColor].setBackground(Color.WHITE);
		}
		previousColor = model.getInstrPtr();
		if(this.previousColor >= 0 && previousColor < Code.CODE_MAX) {
			codeText[previousColor].setBackground(Color.YELLOW);
			codeHex[previousColor].setBackground(Color.YELLOW);
		} 
		if(scroller != null && code != null && model!= null) {
			JScrollBar bar= scroller.getVerticalScrollBar();
			int pc = model.getInstrPtr();
			if(pc > 0 && pc < Code.CODE_MAX && codeHex[pc] != null) {
				Rectangle bounds = codeHex[pc].getBounds();
				bar.setValue(Math.max(0, bounds.y - 15*bounds.height));
			}
		}
	}
	
	public static void main(String[] args) {
		Model model = new Model();
		CodeViewPanel panel = new CodeViewPanel(model);
		JFrame frame = new JFrame("TEST");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 700);
		frame.setLocationRelativeTo(null);
		frame.add(panel);
		frame.setVisible(true);
		int size = Integer.parseInt(Loader.load(model, new File("src/project/pexe/merge.pexe"), 0, 0));
		model.getCurrentJob().setCodeSize(size);
		panel.update("Load Code");
	}
	
}
