package main;

import indexation.TaskIndexing;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import tools.FrenchStemmer;
import tools.Normalizer;

import common.Common;

public class Main {

	public static void main(String[] args) throws IOException {
		
		TaskIndexing.splitIndex();
		File f = new File(Common.DIRCORPUS);

		HashMap<Integer,String[]> h;

		Normalizer stemmer = new FrenchStemmer(common.Common.DIRRSC+"stop.txt");
		Normalizer stemmer2 = new FrenchStemmer(common.Common.DIRRSC+"stop.txt");
		Normalizer stemmer3 = new FrenchStemmer(common.Common.DIRRSC+"stop.txt");
		Normalizer stemmer4 = new FrenchStemmer(common.Common.DIRRSC+"stop.txt");
		
		File fCorpus = new File(Common.DIRRSC+"corpus.txt");
		if (fCorpus.exists()){
			h = Common.readDirectory();
			
		}
		else {
			h = new HashMap<>();
			Common.getDirectory(f,h,".txt",50000);
			Common.writeDirectory(h);
			
		}
	 
		Common.loadEmptyWords();
		TaskIndexing ti1  = new TaskIndexing(h,0,25000,stemmer,true,"index0",5000);
		TaskIndexing ti2  = new TaskIndexing(h,25000,h.size(),stemmer2,true,"index1",5000);
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
	}

}
