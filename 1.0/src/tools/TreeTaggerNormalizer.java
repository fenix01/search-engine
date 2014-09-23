package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class TreeTaggerNormalizer implements Normalizer {

	private String treeTaggerBin;
	private static ArrayList<String> stopCats;
	
	static {
		stopCats = new ArrayList<String>();
		stopCats.add("PUN");
		stopCats.add("PRO");
		stopCats.add("PRP");
		stopCats.add("DET");
		stopCats.add("SENT");
		stopCats.add("KON");
	}
	
	public TreeTaggerNormalizer(String treeTaggerBin) {
		this.treeTaggerBin = treeTaggerBin;
	}
	
	@Override
	public ArrayList<String> normalize(String fileName) throws IOException {
		String command = treeTaggerBin + " " + fileName;
		final Process proc = Runtime.getRuntime().exec(command);
		final ArrayList<String> lemmas = new ArrayList<String>();		
		
		// Consommation de la sortie standard de l'application externe dans
		// un Thread separe
		Thread t = new Thread() {
			public void run() {
				try {
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(proc.getInputStream()));
					String line = "";
					String[] fields;
					String lemma;
					boolean keepIt;
					try {
						while ((line = reader.readLine()) != null) {
							// Traitement du flux de sortie de l'application
							// si besoin est
							fields = line.split("\t");
							if (fields.length == 3) {
								lemma = fields[2];
								// si le lemme est "unknown", on remplace
								// par la forme de surface
								if (lemma.equals("<unknown>") || lemma.equals("@card@")) {
									lemmas.add(fields[0]);
								} else {
									lemmas.add(lemma);
								}
							}
						}
					} finally {
						reader.close();
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		};
		
		t.start();

		
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return lemmas;
	}
	

	public static void main(String[] args) {
		String word = "/net/public/iri/text-arrosage.txt";
		Normalizer stemmer = new TreeTaggerNormalizer("/net/public/iri/tree-tagger/cmd/tree-tagger-french");
		try {
			System.out.println(stemmer.normalize(word));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public ArrayList<String> normalize(File fileName) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}


}
