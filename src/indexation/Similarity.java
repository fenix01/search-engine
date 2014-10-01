package indexation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

public class Similarity {


	public static double getSimilarity(String filename1, String filename2) throws IOException{
		HashMap<String, Double> weight1 = Weighting.readWgtFile(new File(filename1+ ".poids"));
		HashMap<String, Double> weight2 = Weighting.readWgtFile(new File(filename2+ ".poids"));
		
		double sum = 0;

		double f1 = 0, f2 = 0, result = 0;
		for (String key : weight1.keySet()) {
			if (weight2.containsKey(key))
				sum += weight1.get(key) * weight2.get(key);
			f1 += weight1.get(key) * weight1.get(key);
		}

		for (String key : weight2.keySet()) {
			f2 += weight2.get(key) * weight2.get(key);
		}
		result = sum / (Math.sqrt(f1) * Math.sqrt(f2));

		return result;
	}
	
	public static void getSimilarDocuments(String fileName, ArrayList<String[]> fileList) throws IOException{
		HashMap<String,Double> simFiles = new HashMap<>();
		for (String[] file: fileList){
			Double similarity = getSimilarity(fileName, file[0]);
			simFiles.put(file[1], similarity);
		}
		SortedSet<Entry<String, Double>> sortmap = common.Common.sortMap(simFiles);
		for (Map.Entry val : sortmap){
			System.out.println(val.getKey()+"\t"+val.getValue());
		}
	}
	
	public static void main(String[] args) throws IOException{
		System.out.println(getSimilarity(common.Common.DIRWEIGTH_STEMMER+"texte.95-1.txt", 
				common.Common.DIRWEIGTH_STEMMER+"texte.95-2.txt"));
		File f = new File(common.Common.DIRWEIGTH_STEMMER);
		ArrayList<String[]> files = common.Common.getDirectory(f, "poids");
		getSimilarDocuments(common.Common.DIRWEIGTH_STEMMER+"texte.95-1.txt", files);
	}
}
