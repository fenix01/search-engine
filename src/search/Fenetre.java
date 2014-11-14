package search;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.*;

import common.Common;

import tools.FrenchStemmer;
import tools.FrenchTokenizer;

/**
 * Affiche uen fenetre pour effectuer les recherches
 */
public class Fenetre extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JPanel content = new JPanel();
	final Font fontEntered = new Font(Font.DIALOG, Font.PLAIN, 20);
	final Font geglo = new Font(Font.DIALOG, Font.PLAIN, 30);
	int indice = 0;

	Desktop desktop = Desktop.getDesktop();
	
	final DefaultListModel<String> model=new DefaultListModel<String>();
	final JList<String> list = new JList<String>(model);
	
	JRadioButton jr1 = new JRadioButton("Tokenizer");
	JRadioButton jr2 = new JRadioButton("Stemmer");

	public Fenetre(){}
	
	public void display(){

		this.setTitle("Fenetre");
		this.setSize(800, 800);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);     
		this.setLayout(null);
		this.setBackground(Color.ORANGE);



		JPanel Pane = new JPanel();
		Pane.setLayout(null);

		JLabel Lab1 = new JLabel("D'LUL");
		Lab1.setFont(geglo);
		JButton bouton = new JButton("Recherche");


		
		final JTextField text = new JTextField(20);
		text.setFont(fontEntered);

		Pane.add(text);
		Pane.add(bouton);
		Pane.add(Lab1);
		Pane.add(jr1);
		Pane.add(jr2);

		Pane.getComponent(0).setBounds(150, 80, 300, 30);
		Pane.getComponent(1).setBounds(550, 80, 150, 30);
		Pane.getComponent(2).setBounds(325, 10, 200, 40);
		Pane.getComponent(3).setBounds(150, 125, 100, 20);
		Pane.getComponent(4).setBounds(350, 125, 100, 20);

		
		
		list.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent evt) {
		        JList<?> l = (JList<?>)evt.getSource();
		        if (evt.getClickCount() == 2) {
		            int index = l.locationToIndex(evt.getPoint());
		            try {
		            	if(index > 0) desktop.open(new File(model.get(index)));
					} catch (IOException e) {
						e.printStackTrace();
					}
		        }
		    }
		});
		
		
		final JTextArea p= new JTextArea();
		JScrollPane scroll = new JScrollPane (list);


		p.setEditable(false);
		p.setFont(fontEntered);

		jr1.setSelected(true);

		bouton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){

				
				
				model.removeAllElements();
				
				try {
					if(jr1.isSelected()){  
						Request request = new Request(text.getText(), new FrenchTokenizer(),Common.DIRTOKEN);
						String s=request.search();
						String[] items=s.split("\n");
						for(int i=0;i<items.length;i++){
							model.addElement(items[i]);
						}
					}
					else if(jr2.isSelected()){  
						Request request = new Request(text.getText(), new FrenchStemmer(),Common.DIRSTEM);
						String s=request.search();
						String[] items=s.split("\n");
						for(int i=0;i<items.length;i++){
							model.addElement(items[i]);
						}
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
		});

		jr2.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				jr1.setSelected(false);
				jr2.setSelected(true);
			}
		});

		jr1.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				jr1.setSelected(true);
				jr2.setSelected(false);
			}
		});


		this.getContentPane().add(Pane);
		this.getContentPane().add(scroll);
		this.getContentPane().getComponent(0).setBounds(0, 0, 800, 150);
		this.getContentPane().getComponent(1).setBounds(50, 175, 675, 550);

		this.setResizable(false);
		this.setVisible(true);
	}




}