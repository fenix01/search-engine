package search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

public class Similarity {

//	public static double getSimilarity(String filename1, String filename2) throws IOException{
//		HashMap<String, Double> weight1 = Weighting.readWgtFile(new File(filename1));
//		HashMap<String, Double> weight2 = Weighting.readWgtFile(new File(filename2));
//		
//		double sum = 0;
//
//		double f1 = 0, f2 = 0, result = 0;
//		for (String key : weight1.keySet()) {
//			if (weight2.containsKey(key))
//				sum += weight1.get(key) * weight2.get(key);
//			f1 += weight1.get(key) * weight1.get(key);
//		}
//
//		for (String key : weight2.keySet()) {
//			f2 += weight2.get(key) * weight2.get(key);
//		}
//		result = sum / (Math.sqrt(f1) * Math.sqrt(f2));
//
//		return result;
//	}
//	
//	public static void getSimilarDocuments(String fileName, HashMap<Integer,String[]> h) throws IOException{
//		HashMap<String,Double> simFiles = new HashMap<>();
//		for (Map.Entry<Integer, String[]> key_: h.entrySet()){
//			Double similarity = getSimilarity(fileName, key_.getValue()[0]);
//			simFiles.put(key_.getValue()[1], similarity);
//		}
//		SortedSet<Entry<String, Double>> sortmap = common.Common.sortMap(simFiles);
//		for (Map.Entry val : sortmap){
//			System.out.println(val.getKey()+"\t"+val.getValue());
//		}
//	}
//	
//	public static void main(String[] args) throws IOException{
//		System.out.println(getSimilarity(common.Common.DIRWEIGTH_STEMMER+"texte.95-1.txt.poids", 
//				common.Common.DIRWEIGTH_STEMMER+"texte.95-2.txt.poids"));
//		File f = new File(common.Common.DIRWEIGTH_STEMMER);
//		HashMap<Integer,String[]> h=new HashMap<>();
//		 common.Common.getDirectory(f,h,".poids",-1);
//		getSimilarDocuments(common.Common.DIRWEIGTH_STEMMER+"texte.95-1.txt.poids", h);
//	}
}