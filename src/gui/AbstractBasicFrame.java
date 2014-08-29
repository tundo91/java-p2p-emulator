package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.Timestamp;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public abstract class AbstractBasicFrame extends JFrame {
	protected JPanel mainPanel;
	protected JPanel topPanel;
	protected JPanel bottomPanel;
	protected JTextArea log;
	public static final long serialVersionUID = 42L;
	public AbstractBasicFrame(final String paramFrameNameString) {
		super(paramFrameNameString);
		setSize(new Dimension(400, 500));
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// setting main panel
		mainPanel = new JPanel();
		mainPanel.setOpaque(true);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		mainPanel.setLayout(new BorderLayout(2, 2));

		// setting bottom
		bottomPanel = new JPanel();
		bottomPanel.setOpaque(true);
		bottomPanel.setBorder(BorderFactory.createTitledBorder("Log"));
		bottomPanel.setLayout(new BorderLayout());
		log = new JTextArea();
		log.setLineWrap(true);
		log.setRows(10);
		log.setEditable(false);
		final JScrollPane areaScrollPane = new JScrollPane(log);
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(250, 250));
		areaScrollPane.setVisible(true);
		bottomPanel.add(areaScrollPane, BorderLayout.CENTER);
		mainPanel.add(bottomPanel,BorderLayout.PAGE_END);
	}

	/**
	 * Insert a new entry in the log area with a carriage return at the end.
	 * @param logEntry is the String to be inserted into the log.
	 */
	public void appendLogEntry(String logEntry) {
		final java.util.Date date = new java.util.Date();
		logEntry += "\n";
		log.insert(new Timestamp(date.getTime()).toString().substring(11) + ": " + logEntry, log.getSelectionEnd());
	}
}
