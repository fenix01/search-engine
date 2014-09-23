package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import tools.FrenchTokenizerAutomaton.Signal;

public class FrenchTokenizer implements Normalizer{
	
	private FrenchTokenizerAutomaton transducer;
	
	public FrenchTokenizer() {
		this.transducer = new FrenchTokenizerAutomaton();
	}
	
	@Override
	public ArrayList<String> normalize(String request) throws IOException {
		return this.tokenize(request);
	}
	
	@Override
	public ArrayList<String> normalize(File fileName) throws IOException {
		String text = "";
		//lecture du fichier texte	
		InputStream ips=new FileInputStream(fileName); 
		InputStreamReader ipsr=new InputStreamReader(ips);
		BufferedReader br=new BufferedReader(ipsr);
		String line;
		while ((line=br.readLine())!=null){
			text += line + " ";
		}
		br.close(); 
		return this.tokenize(text);
	}
	
	/**
	 * This method drives the automaton execution over the stream of chars.
	 */
	public ArrayList<String> tokenize(String text) {
		char[] textContent = text.toCharArray();
		ArrayList<String> tokens = new ArrayList<String>();
		// Initialize the execution
		int begin = -1;
		transducer.reset();
		// Run over the chars
		for(int i=0 ; i<textContent.length ; i++) {
			Signal s = transducer.feedChar( textContent[i] );
			switch(s) {
			case start_word:
				begin = i;
				break;
			case end_word:
				tokens.add(text.substring(begin, i));
				begin = -1;
				break;
			case end_word_prev:
				tokens.add(text.substring(begin, i-1));
				break;
			case switch_word:
				tokens.add(text.substring(begin, i));
				begin = i;
				break;
			case switch_word_prev:
				tokens.add(text.substring(begin, i-1));
				begin = i;
				break;
			case cancel_word:
				begin = -1;
				break;
			}
		}
		// Add the last one
		if (begin != -1) {
			tokens.add(text.substring(begin, text.length()));
		}
		
		return tokens;
	}

	public static void main(String[] args) {
		String test = "Ceci est un test de tokenisation. Avec des abats-jours, des aujourd'hui et des jusqu'à, s'il veut bien l'autre sera là.";
		String[] token = test.split(" ");
		for(String el : token){
			System.out.print(el+",");
		}
		System.out.println("");
		System.out.println((new FrenchTokenizer()).tokenize(test));
	}

}
