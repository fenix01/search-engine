package common;

import indexation.TaskIndexing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import tools.FrenchStemmer;
import tools.FrenchTokenizer;
import tools.Normalizer;

//Classe qui contient des méthodes communes pour les différentes package

public class Common {

	public static Boolean Choice = false;
	//false = version html
	//true = version texte
	public static String DIRRSC = "./rsc/";
	public static String DIRCORPUS = DIRRSC + "lemonde/";

	public static String DIRWEIGTH_STEMMER = DIRRSC + "weight/" + "stemmer/";
	public static String DIRWEIGTH_TOKENIZER = DIRRSC + "weight/" + "tokenizer/";
	public static ArrayList<String> emptyWords;
	
	private static void unloadEmptyWords(){
		if (!(emptyWords == null))
		if (!emptyWords.isEmpty())
			emptyWords.clear();
	}

	// Fonction qui permet de charger la liste des mots vides
	public static void loadEmptyWords() {
		unloadEmptyWords();
		File f;
		FileReader rd = null;
		BufferedReader br = null;
		String line;
		ArrayList<String> stop = null;
		try {
			stop = new ArrayList<>();
			f = new File(DIRRSC+"stop.txt");
			rd = new FileReader(f);
			br = new BufferedReader(rd);
			while ((line = br.readLine()) != null) {
				stop.add(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				br.close();
				rd.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		emptyWords = stop;
	}

	// Fonction qui permet de renvoyer un Map trié par les valeurs
	public static SortedSet<Entry<String, Double>> sortMap(Map mp) {
		TreeSet sortedMap = new TreeSet<Map.Entry<String, Double>>(
				new Comparator<Map.Entry<String, Double>>() {
					@Override
					public int compare(Map.Entry<String, Double> e1,
							Map.Entry<String, Double> e2) {
						int comp = Double.compare(e1.getValue(), e2.getValue());
						if (comp == 0)
							return 0;
						else if (comp > 0)
							return -1;
						else
							return 1;

					}
				});
		sortedMap.addAll(mp.entrySet());
		return sortedMap;
	}

	// Fonction qui renvoit vrai si le mot en paramètre est un mot vide
	public static boolean isEmptyWord(String word)
	{
		String regexp = "[a-zA-Zéèçà0-9]*";
	    return !word.toLowerCase().matches(regexp) || Common.emptyWords.contains(word.toLowerCase());
	}

	// Fonction qui renvoit la liste des fichiers contenus dans un répertoire en
	// fonction de son extension
	public static void getDirectory(File f, HashMap<Integer,String[]> listFiles, final String ext) throws IOException {
		// Liste des fichiers du répertoire
		// ajouter un filtre (FileNameFilter) sur les noms
		// des fichiers si nécessaire
		File[] ltFiles=null;
		ltFiles = f.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if (!pathname.isDirectory() && !pathname.getName().endsWith(ext))
					return false;
				return true;
			}
		});

		for (File file_ : ltFiles) {
			if(file_.isDirectory())
					getDirectory(file_, listFiles,ext);
			else{
				String[] fileStr = new String[2];
				fileStr[0] = file_.getCanonicalPath();
				fileStr[1] = file_.getName();
				listFiles.put(listFiles.size(),fileStr);
			}	
		}
	}
	
	/*
	 * écrit la liste des documents dans un fichier corpus.txt.
	 * permet de sauvegarder le dictionnaire inversée, permettant de retrouver
	 * un fichier à partir de son identifiant numérique.
	 */
	public static void writeDirectory(HashMap<Integer,String[]> listFiles){
		try {
			FileWriter fw = new FileWriter(new File(DIRRSC+"corpus.txt"));
			BufferedWriter bw = new BufferedWriter(fw);
			for (Map.Entry<Integer, String[]> doc : listFiles.entrySet()){
				String line = doc.getKey() + "\t" + doc.getValue()[0] + "\t" + doc.getValue()[1];
				bw.write(line);
				bw.newLine();
			}
			bw.close();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * permet de récupérer à partir de corpus.txt la liste des fichiers du corpus
	 */
	public static HashMap<Integer,String[]> readDirectory(){
		HashMap<Integer,String[]> listFiles = new HashMap<>();
		try {
			FileReader fr = new FileReader(new File(DIRRSC+"corpus.txt"));
			BufferedReader br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null){
				String[] doc = line.split("\t");
				String[] value = new String[2];
				value[0] = doc[1];
				value[1] = doc[2];
				listFiles.put(Integer.parseInt(doc[0]), value);
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listFiles;
	}
	
	

	
	public static void writeRequest(String content) throws IOException{
		File f = new File(DIRRSC+"request.htm");
		if (f.exists())
			f.delete();
		FileWriter fw = new FileWriter(f);
		fw.write(content);
		fw.close();
	}
	
	public static void main(String[] args) throws IOException {
		//File f = new File(Common.DIRCORPUS);
		File f = new File("/partage/public/iri/projetIRI/corpus/0000/");
		HashMap<Integer,String[]> h;

		Normalizer stemmer = new FrenchStemmer(common.Common.DIRRSC+"stop.txt");
		Normalizer stemmer2 = new FrenchStemmer(common.Common.DIRRSC+"stop.txt");
		Normalizer stemmer3 = new FrenchStemmer(common.Common.DIRRSC+"stop.txt");
		Normalizer stemmer4 = new FrenchStemmer(common.Common.DIRRSC+"stop.txt");
		
		File fCorpus = new File(DIRRSC+"corpus.txt");
		if (fCorpus.exists()){
			h = readDirectory();
			
		}
		else {
			h = new HashMap<>();
			Common.getDirectory(f,h,".txt");
			writeDirectory(h);
			
		}
//		for (Map.Entry<Integer, String[]> hit : h.entrySet()) {
//			System.out.println(hit.getKey() + "\t" + hit.getValue()[0]);
//		}		 
		loadEmptyWords();
		TaskIndexing ti1  = new TaskIndexing(h,0,2500,stemmer,true,"index1",2500);
		TaskIndexing ti2  = new TaskIndexing(h,2500,5000,stemmer2,true,"index2",2500);
		TaskIndexing ti3  = new TaskIndexing(h,5000,7500,stemmer3,true, "index3",2500);
		TaskIndexing ti4  = new TaskIndexing(h,7500,h.size(),stemmer4,true, "index4",2500);
		ti1.start();
		ti2.start();
		ti3.start();
		ti4.start();
//		
//		Thread thread1 = new Thread(ti1);
//		Thread thread2 = new Thread(ti2);
//		thread1.start();
//		thread2.start();
		
	}

}
