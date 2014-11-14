package main;

import indexation.TaskIndexing;
import indexation.Weighting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.TreeMap;

import search.Fenetre;
import tools.FrenchStemmer;
import tools.FrenchTokenizer;
import tools.Normalizer;
import common.Common;

public class Main {
	
	public static int nb_doc = 0;
	public static int nb_thread = 4;

	public static void main(String[] args) throws IOException {
		
		
		Common.load_data();
		
		
		// liste des fichiers du corpus
		TreeMap<Integer, String> h = null;
		//permet de verifier si la liste des fichiers existe 
		File fini = new File(Common.FICINI);
		File dir = new File(Common.DIRRSC);
		File fCorpus = new File(Common.DIRCORPUS);
		File index_dir = new File(Common.DIRINDEX);
		File token_dir = new File(Common.DIRTOKEN);
		File stem_dir = new File(Common.DIRSTEM);
		
		
		
		System.out.println(Common.DIRINDEX);
		
		boolean clearSuccess;
		
		//int nb_thread=4;
		//permet de charger les mots vides
		Common.loadEmptyWords();
		
		boolean stop = false;
		int command;
		Scanner sc = new Scanner(System.in);
		while (!stop) {
			System.out.println("--Bienvenue dans notre moteur de recherche--");
			System.out.println("Choisissez une commande à effectuer");
			System.out.println("choisir le nombre de threads utilisés (4 par défaut) : tapez 0");
			System.out.println("lister les fichiers du corpus : tapez 1");
			System.out.println("Indexer le corpus : tapez 2 (!WARNING! Très long)");
			System.out.println("Parcours les index pour inclure le poids,"
					+"et générer le fichier somme des poids : tapez 3 (!WARNING! Long)");
			System.out.println("Ouvrir la fenêtre de recherche : tapez 4");
			System.out.println("Supprimer les index, le fichier poids, et les fichiers .corpus : tapez 5");
			System.out.println("quitter : tapez 6 (ALPHA)");
			System.out.println("Les fichiers créés pendant ces opérations restent présents sur le disque à la fermeture du programme");
			command = sc.nextInt();
			switch (command) {
			case 0:
				System.out.println("Nombre de threads à utiliser");
				nb_thread = sc.nextInt();
			//lister les fichiers du corpus
			case 1:
				
				dir.mkdir();
				Common.clearDiskSpace(true, true);

				long startTimeCorpus = System.currentTimeMillis();
				h = new TreeMap<>();
				Common.getDirectory(fCorpus, h, ".txt", nb_doc);
				Common.writeDirectory(h,nb_thread,nb_doc);

				
				System.gc();
				
				long estimatedTimeCorpus = System.currentTimeMillis() - startTimeCorpus;
				System.out.println(estimatedTimeCorpus / 1000);
				break;
			//Indexer le corpus
			case 2:
				if(!new File(Common.DIRRSC+"html"+Common.extCORPUSLIST).exists()){
					System.out.println("corpus non crée");
				}
				else{
				Common.clearDiskSpace(true,false);
				long startTime = System.currentTimeMillis();
				index_dir.mkdir();
				Normalizer stemmer[]=new Normalizer[nb_thread];
				Normalizer tokenizer[]=new Normalizer[nb_thread];
				
				for(int i=0;i<nb_thread;i++){
					stemmer[i]=new FrenchStemmer(Common.FICEMPTYWORD);
					tokenizer[i]=new FrenchTokenizer(Common.FICEMPTYWORD);
				}
				
				token_dir.mkdir();
				stem_dir.mkdir();
				
				TaskIndexing index[]=new TaskIndexing[nb_thread];
				TaskIndexing.index_dir = Common.DIRTOKEN;
				
				for(int i=0;i<nb_thread;i++){
					index[i]=new TaskIndexing(i+Common.extCORPUSLIST, tokenizer[i], true,
							"index"+i, 10000/nb_thread);
				}
				
				for(int i=0;i<nb_thread;i++){
					index[i].start();
				}
				for(int i=0;i<nb_thread;i++){
					index[i].join();
				}
			
				//fusionne les index créé par les n threads
				TaskIndexing.fusionThreadsIndexes(nb_thread);
				//découper l'index en plusieurs index
				TaskIndexing.splitBinaryIndex(2);

				long estimatedTime = System.currentTimeMillis() - startTime;
				System.out.println(estimatedTime / 1000);

				startTime = System.currentTimeMillis();

				TaskIndexing.index_dir = Common.DIRSTEM;
				for(int i=0;i<nb_thread;i++){
					index[i]=new TaskIndexing(i+Common.extCORPUSLIST, stemmer[i], true,
							"index"+i, 8000/nb_thread);
				}
				
				for(int i=0;i<nb_thread;i++){
					index[i].start();
				}
				for(int i=0;i<nb_thread;i++){
					index[i].join();
				}
			
				//fusionne les index créé par les n threads
				TaskIndexing.fusionThreadsIndexes(nb_thread);
				//découper l'index en plusieurs index
				TaskIndexing.splitBinaryIndex(2);

				System.gc();
				
				estimatedTime = System.currentTimeMillis() - startTime;
				System.out.println(estimatedTime / 1000);
				}
				break;
			case 3:

				long startTime1 = System.currentTimeMillis();
				TreeMap<Integer, String> s_indexes = new TreeMap<>();
				TreeMap<Integer, String> t_indexes = new TreeMap<>();
				TreeMap<Integer, Float> sum_weights_s = new TreeMap<>();
				TreeMap<Integer, Float> sum_weights_t = new TreeMap<>();
				Common.getDirectory(token_dir, t_indexes, ".idx", -1);
				Common.getDirectory(stem_dir, s_indexes, ".idx", -1);
				Weighting weight_1 = new Weighting(sum_weights_t, t_indexes, 0,
						t_indexes.size(), nb_doc);
				Weighting weight_2 = new Weighting(sum_weights_s, s_indexes,0 , s_indexes.size(), nb_doc);
				
				weight_1.start();
				weight_2.start();
				
				weight_1.join();
				weight_2.join();
				Weighting.saveWeights(Common.DIRTOKEN + "docWeight"+Common.extWEIGHT, sum_weights_t);
				Weighting.saveWeights(Common.DIRSTEM + "docWeight"+Common.extWEIGHT, sum_weights_s);
				Weighting.saveHtmlFilePath(Common.DIRTOKEN);
				Weighting.saveHtmlFilePath(Common.DIRSTEM);
				System.gc();
				long estimatedTime1 = System.currentTimeMillis() - startTime1;
				System.out.println(estimatedTime1 / 1000);
				
				break;
			case 4:				
				Fenetre f = new Fenetre();
				f.display();
				sc.close();
				stop = true;
				break;
			case 5:
				clearSuccess = Common.clearDiskSpace(true,true);
				if (!clearSuccess) System.out.println("Un erreur est survenue");
				else System.out.println("CONGRATULATIONS!");
				break;
			case 6:
				stop = true;
				sc.close();
				System.out.println("Au revoir");
				break;
				
			}
		}

	



		

	}

}
