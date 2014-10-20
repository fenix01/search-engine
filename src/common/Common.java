package common;

import indexation.TaskIndexing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
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

	public static String aListToString(ArrayList<String> aList) {
		StringBuilder stb = new StringBuilder();
		for (String word : aList) {
			stb.append(word);
			stb.append(" ");
		}
		return stb.toString();
	}
	
	private static void unload(){
		if (!(emptyWords == null))
		if (!emptyWords.isEmpty())
			emptyWords.clear();
	}

	// Fonction qui permet de charger la liste des mots vides
	public static void loadEmptyWords() {
		unload();
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
		HashMap<Integer,String[]> h=new HashMap<>();

		long startTime = System.currentTimeMillis(); 
		Common.getDirectory(f,h,".txt");
		System.out.println( System.currentTimeMillis()-startTime );
		Normalizer stemmer = new FrenchStemmer(common.Common.DIRRSC+"stop.txt");
		
		for (Map.Entry<Integer, String[]> hit : h.entrySet()) {
			System.out.println(hit.getKey() + "\t" + hit.getValue()[0]);
		}
		
		TaskIndexing ti1  = new TaskIndexing(h,0,5000,stemmer,true,"index1");
		TaskIndexing ti2  = new TaskIndexing(h,5001,h.size(),stemmer,true, "index2");
		
		Thread thread1 = new Thread(ti1);
		Thread thread2 = new Thread(ti2);
		thread1.start();
		thread2.start();
		
	}

}
