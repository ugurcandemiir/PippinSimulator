package projectview;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class MenuBarBuilder {
	GUIMediator gui;
	JMenuItem assemble = new JMenuItem("Assemble Source...");
	JMenuItem load = new JMenuItem("Load Program...");
	JMenuItem exit = new JMenuItem("Exit");
	JMenuItem go = new JMenuItem("Go");
	JMenuItem job0 = new JMenuItem("Job 0");
	JMenuItem job1 = new JMenuItem("Job 1");
	JMenuItem job2 = new JMenuItem("Job 2");
	JMenuItem job3 = new JMenuItem("Job 3");
	/**
	 * @param gui
	 */
	public MenuBarBuilder(GUIMediator gui) {
		this.gui = gui;
	}
	
	public JMenu createFileMenu() {
		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F); // so you can use ALT-F to open the File menu

		assemble.setMnemonic(KeyEvent.VK_M); // so ALT-F, ALT-M to call the assembler
		assemble.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
			//so you can use CTRL-M to call the assembler
		assemble.addActionListener(e -> gui.assembleFile());
		menu.add(assemble);
				
		// Write the same 4 instructions for load. 
		// Use VK_L for the accelerator
		// change the lambda expression to e -> gui.loadFile()
		load.setMnemonic(KeyEvent.VK_L); // so ALT-F, ALT-L to call the loader
		load.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
			//so you can use CTRL-L to call the assembler
		load.addActionListener(e -> gui.loadFile());
		menu.add(load);

		menu.addSeparator();
				
		// Write the same 4 instructions for exit. 
		// Use VK_E for the accelerator
		// change the lambda expression to e -> gui.exit()
		exit.setMnemonic(KeyEvent.VK_E); // so ALT-F, ALT-E to call the assembler
		exit.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
			//so you can use CTRL-E to call the exit
		exit.addActionListener(e -> gui.exit());
		menu.add(exit);

		return menu;
	}
	
	public JMenu createExecuteMenu() {
		JMenu menu = new JMenu("Execute");
		menu.setMnemonic(KeyEvent.VK_X); 

		go.setMnemonic(KeyEvent.VK_G); // for go
		go.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
			//so you can use CTRL-G to call go
		go.addActionListener(e -> gui.execute());
		menu.add(go);
				
		return menu;
	}
	
	public JMenu createJobsMenu() {
		JMenu menu = new JMenu("Change Job");
		menu.setMnemonic(KeyEvent.VK_J);

		job0.setMnemonic(KeyEvent.VK_0);
		job0.setAccelerator(KeyStroke.getKeyStroke(
			KeyEvent.VK_0, ActionEvent.CTRL_MASK));
		job0.addActionListener(e -> gui.changeToJob(0));
		menu.add(job0);
		
		job1.setMnemonic(KeyEvent.VK_1);
		job1.setAccelerator(KeyStroke.getKeyStroke(
			KeyEvent.VK_1, ActionEvent.CTRL_MASK));
		job1.addActionListener(e -> gui.changeToJob(1));
		menu.add(job1);
		
		job2.setMnemonic(KeyEvent.VK_2);
		job2.setAccelerator(KeyStroke.getKeyStroke(
			KeyEvent.VK_2, ActionEvent.CTRL_MASK));
		job2.addActionListener(e -> gui.changeToJob(2));
		menu.add(job2);

		job3.setMnemonic(KeyEvent.VK_3);
		job3.setAccelerator(KeyStroke.getKeyStroke(
			KeyEvent.VK_3, ActionEvent.CTRL_MASK));
		job3.addActionListener(e -> gui.changeToJob(3));
		menu.add(job3);

		return menu;
	}
	
	public void update(String arg1) {
		assemble.setEnabled(gui.getCurrentState().getAssembleFileActive());
		load.setEnabled(gui.getCurrentState().getLoadFileActive());
		go.setEnabled(gui.getCurrentState().getStepActive());
		job0.setEnabled(gui.getCurrentState().getChangeJobActive());
		job1.setEnabled(gui.getCurrentState().getChangeJobActive());
		job2.setEnabled(gui.getCurrentState().getChangeJobActive());
		job3.setEnabled(gui.getCurrentState().getChangeJobActive());
	}
	
	
	
}
