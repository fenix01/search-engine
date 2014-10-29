package main;

import indexation.TaskIndexing;
import indexation.Weighting;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.TreeMap;

import tools.FrenchStemmer;
import tools.Normalizer;
import common.Common;

public class Main {
	
	public static int documents = 50000;

	public static void main(String[] args) throws IOException {
		
//		RandomAccessFile rdFile = new RandomAccessFile(new File(Common.DIRINDEX+"c_index"), "r");
//		System.out.println(Common.binarySearch(rdFile, "chewew"));
		
		File f = new File(Common.DIRCORPUS);

		HashMap<Integer,String[]> h;

		Normalizer stemmer = new FrenchStemmer(common.Common.DIRRSC+"stop.txt");
		Normalizer stemmer2 = new FrenchStemmer(common.Common.DIRRSC+"stop.txt");
		//Normalizer stemmer3 = new FrenchStemmer(common.Common.DIRRSC+"stop.txt");
		//Normalizer stemmer4 = new FrenchStemmer(common.Common.DIRRSC+"stop.txt");
		
		File fCorpus = new File(Common.DIRRSC+"corpus.txt");
		if (fCorpus.exists()){
			h = Common.readDirectory();			
		}
		else {
			h = new HashMap<>();
			Common.getDirectory(f,h,".txt",documents);
			Common.writeDirectory(h);
			
		}
		
		File index_dir = new File(Common.DIRINDEX);
		index_dir.mkdir();
		for(File file : index_dir.listFiles()){
			file.delete();
		}
		
		Common.loadEmptyWords();
		TaskIndexing ti1  = new TaskIndexing(h,0,documents/2,stemmer,true,"index0",5000);
		TaskIndexing ti2  = new TaskIndexing(h,documents/2,h.size(),stemmer2,true,"index1",5000);
//		TaskIndexing ti3  = new TaskIndexing(h,5000,7500,stemmer3,true, "index2",2500);
//		TaskIndexing ti4  = new TaskIndexing(h,7500,h.size(),stemmer4,true, "index3",2500);
		ti1.start();
		ti2.start();
//		ti3.start();
//		ti4.start();
		
		ti1.join();
		ti2.join();
		
		TaskIndexing.fusionThreadsIndexes(2);
		TaskIndexing.splitIndex();
		
		
		
		File fIndexes = new File(Common.DIRINDEX);
		HashMap<Integer,String[]> indexes = new HashMap<>();
		TreeMap<Integer,Double> sum_weights= new TreeMap<>();
		Common.getDirectory(fIndexes, indexes, "",-1);
		Weighting weight_1 = new Weighting(sum_weights, indexes, 0, indexes.size()/2, documents);
		Weighting weight_2 = new Weighting(sum_weights, indexes,indexes.size()/2 , indexes.size(), documents);
		
		weight_1.start();
		weight_2.start();
		weight_1.join();
		weight_2.join();
		Weighting.saveWeights(Common.DIRINDEX+"poids_total", sum_weights);
		
		
	}

}
