package common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

//Classe qui contient des méthodes communes pour les différents package

public class Common {
	
	//extension d'un fichier temporaire
	public static String extTMP = ".tmp";
	//extension de l'index
	public static String extIDX = ".idx";
	//extension du fichier poids
	public static String extWEIGHT = ".wgt";	

	public static String DIRRSC = "./rsc/";
	public static String DIRINDEX = DIRRSC + "index/";
	//public static String DIRCORPUS = DIRRSC + "corpus/";
	public static String DIRCORPUS = "/public/iri/projetIRI/corpus/";

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
	
	/**
	 * retourne les premières lettres d'un mot de la ligne d'index vérifie si
	 * certains mots ont une taille inférieur à x
	 * 
	 * @param line
	 * @param x
	 * @return
	 */
	public static String firstOcc(String line, int x) {
		if (line == null)
			return null;
		String word = line.split("\t")[0];
		if (word.length() < x)
			return word;
		else
			return word.substring(0, x);
	}

	// Fonction qui permet de renvoyer un Map trié par les valeurs
	public static SortedSet<Entry<String, Float>> sortMap(Map mp) {
		TreeSet sortedMap = new TreeSet<Map.Entry<String, Float>>(
				new Comparator<Map.Entry<String, Float>>() {
					@Override
					public int compare(Map.Entry<String, Float> e1,
							Map.Entry<String, Float> e2) {
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

	/**
	 *  Fonction qui renvoit la liste des fichiers contenus dans un répertoire en fonction de son extension
	 */
	public static void getDirectory(File f, TreeMap<Integer,String> listFiles, final String ext, int max_entries) 
			throws IOException {
		
		if (max_entries != -1 && listFiles.size() >= max_entries)
			return;
		// Liste des fichiers du répertoire
		// ajouter un filtre (FileNameFilter) sur les noms
		// des fichiers si nécessaire
		File[] ltFiles=null;
		ltFiles = f.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				//filtre les fichiers en ignorant les fichiers ne commençant pas par ext
				if (!pathname.isDirectory() && !pathname.getName().endsWith(ext))
					return false;
				return true;
			}
		});

		for (File file_ : ltFiles) {
			if(file_.isDirectory())
					getDirectory(file_, listFiles,ext,max_entries);
			else{
				listFiles.put(listFiles.size(),file_.getCanonicalPath());
			}	
		}
	}
	
	/*
	 * écrit la liste des documents dans un fichier corpus.txt.
	 * permet de sauvegarder le dictionnaire inversée, permettant de retrouver
	 * un fichier à partir de son identifiant numérique.
	 */
	public static void writeDirectory(TreeMap<Integer, String> h, int nb_th,int nb_doc){
		int modulo=nb_doc/nb_th;
		
		try {
			FileWriter fwall = new FileWriter(new File(DIRRSC+"all.corpus"));
			BufferedWriter bwall = new BufferedWriter(fwall);
			
				for(int j=0;j<nb_th;j++){
					
					FileWriter fw = new FileWriter(new File(DIRRSC+j+".corpus"));
					BufferedWriter bw = new BufferedWriter(fw);
					
					while(h.size()>0 && h.firstKey()/modulo==j){
						
							bw.write(h.firstKey()+"\t"+h.get(h.firstKey()));
							bw.newLine();
							
							File f = new File(h.get(h.firstKey()));
							bwall.write(f.getName());
							bwall.newLine();
							h.remove(h.firstKey());
					}
					bw.close();
					fw.close();
				}
				bwall.close();
			
			}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * permet de récupérer à partir de corpus.txt la liste des fichiers du corpus
	 */
	public static TreeMap<Integer, String[]> readDirectory(){
		TreeMap<Integer,String[]> listFiles = new TreeMap<>();
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
	
	/**
	 * cherche un mot dans un index de façon séquentielle
	 * @param f fichier d'index
	 * @param word mot à rechercher
	 * @return la ligne de l'index avec les docs, les poids, les tfs, et le df
	 * @throws IOException
	 */
	public static String sequentialSearch(File f, String word) throws IOException{
		FileReader fr;
		fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		String line = "";
		boolean found = false;
		while (!found && line != null){
			line = br.readLine();
			if (line != null){
				String word_ = line.split("\t")[0];
				if (word_.equals(word)){
					found = true;
				}
			}
		}
		br.close();
		if (found)
			return line;
		else return "";
	}
	
	public static String binarySearch(RandomAccessFile rdFile, String word){
		try {
			long start = 0;
			long end = rdFile.length();
			String curWord,line,line2 = null;
			while (start <= end){
				long mid = start + (end - start) / 2;
				rdFile.seek(mid);
				line2 = rdFile.readLine();
				line = rdFile.readLine();
				curWord = line.split("\t")[0];
				if (curWord.compareTo(word) >= 0){
					end = mid - 1;
				}
				else {
					start = mid + 1;
				}
			}
			rdFile.seek(start);
			rdFile.readLine();
			line = rdFile.readLine();
			if (word.equals(line.split("\t")[0]))
				return line;
			else if (word.equals(line2.split("\t")[0]))
				return line2;
			else return "";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * méthode qui renvoie pour un mot donné la ligne d'informations de l'index
	 */
	public static String getWordIndex(String word) throws FileNotFoundException{
		char c = word.charAt(0);
		File f = new File(DIRINDEX+c+"_index");
		RandomAccessFile rdFile = new RandomAccessFile(f, "r");
		return binarySearch(rdFile, word);
		
	}
	
	
	public static void writeRequest(String content) throws IOException{
		File f = new File(DIRRSC+"request.htm");
		if (f.exists())
			f.delete();
		FileWriter fw = new FileWriter(f);
		fw.write(content);
		fw.close();
	}

}
