package main;

import indexation.IndexGenerator;
import indexation.Similarity;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import tools.FrenchStemmer;
import tools.FrenchTokenizer;

import common.Common;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;


public class Main extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtSearch;
	private JWebBrowser webBrowser = null;
	private JCheckBox chkCorpus;
	private JCheckBox chkNorm;

	public void search() {
		long begin = System.currentTimeMillis();
		HashMap<Integer, Double> req = null;
		Similarity sim = new Similarity();
		try {
			if (chkNorm.isSelected()) {
				req = sim.computeRequest(Common.DIRWEIGTH_STEMMER,
						txtSearch.getText(), IndexGenerator.idx_stemmer,
						new FrenchStemmer());
			} else {
				req = sim.computeRequest(Common.DIRWEIGTH_TOKENIZER,
						txtSearch.getText(), IndexGenerator.idx_tokenizer,
						new FrenchTokenizer());
			}
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SortedSet<Entry<Integer, Double>> req2 = Common.sortMap(req);

		long end = System.currentTimeMillis();
		float timeElapsed = end - begin;
		// lblTime.setText(timeElapsed + " ms");

		StringBuilder stb = new StringBuilder();
		stb.append("<html><h1>Résultats pour : " + txtSearch.getText()
				+ "</h1>");
		stb.append("<h3>" + sim.getCountResult() + " résultats (" + timeElapsed
				+ " ms)</h3><ul>");
		for (Map.Entry<Integer, Double> entry : req2) {
			String fileName = IndexGenerator.dirCorpus.get(entry.getKey())[1];
			String filePath = IndexGenerator.dirCorpus.get(entry.getKey())[0];
			stb.append("<li><a href=\"file:///" + filePath + "\" >" + fileName
					+ "</a>");
			stb.append("</li><hr><br />");
		}
		stb.append("</ul></html>");
		try {
			Common.writeRequest(stb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		webBrowser.navigate(new File(Common.DIRRSC+"request.htm").getAbsolutePath());
	}

	public Main() {
		super(new BorderLayout());
		JPanel webBrowserPanel = new JPanel(new BorderLayout());
		webBrowserPanel.setBorder(BorderFactory
				.createTitledBorder("Troll Search"));
		webBrowser = new JWebBrowser();
		webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
		add(webBrowserPanel, BorderLayout.CENTER);
		// Create an additional bar allowing to show/hide the menu bar of the
		// web browser.
		JPanel buttonPanel = new JPanel();
		txtSearch = new JTextField(50);
		txtSearch.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
				if (arg0.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER)
					search();
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		JButton btnSearch = new JButton("Search !");
		chkCorpus = new JCheckBox("text/html");
		chkCorpus.setSelected(false);
		chkCorpus.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (chkCorpus.isSelected())
					Common.Choice = true;
				else
					Common.Choice = false;
				IndexGenerator idx = new IndexGenerator();
				idx.generate();
			}
		});
		chkNorm = new JCheckBox("stemmer/tokenizer");
		chkNorm.setSelected(true);

		btnSearch.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				search();

			}
		});
		getClass().getResource("/resources/filename.txt"); 
		buttonPanel.add(txtSearch);
		buttonPanel.add(btnSearch);
		buttonPanel.add(chkCorpus);
		buttonPanel.add(chkNorm);
		add(buttonPanel, BorderLayout.NORTH);
	}

	/* Standard main method to try that test as a standalone application. */
	public static void main(String[] args) {
		IndexGenerator idx = new IndexGenerator();
		idx.generate();
		UIUtils.setPreferredLookAndFeel();
		NativeInterface.open();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame("Troll Search");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.getContentPane().add(new Main(), BorderLayout.CENTER);
				frame.setSize(1000, 800);
				frame.setLocationByPlatform(true);
				frame.setVisible(true);
			}
		});
		NativeInterface.runEventPump();
	}

}
