package main;

import indexation.TaskIndexing;
import indexation.Weighting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.TreeMap;

import search.Fenetre;
import search.Request;
import tools.FrenchStemmer;
import tools.FrenchTokenizer;
import tools.Normalizer;
import common.Common;

public class Main {
	
	public static int nb_doc = 0;

	public static void main(String[] args) throws IOException {
		// liste des fichiers du corpus
		TreeMap<Integer, String> h = null;
		File fRevCorpus = new File(Common.DIRRSC + "0.corpus");
		File fCorpus = new File(Common.DIRCORPUS);
		int nb_thread=4;
		//permet de charger les mots vides
		Common.loadEmptyWords();
		
		boolean stop = false;
		int command;
		Scanner sc = new Scanner(System.in);
		while (!stop) {
			System.out.println("--Bienvenue dans notre moteur de recherche--");
			System.out.println("Choisissez une commande à effectuer");
			System.out.println("lister les fichiers du corpus : tapez 1");
			System.out.println("lire la liste des fichiers du corpus : tapez 2");
			System.out.println("Indexer le corpus : tapez 3");
			System.out.println("Parcours les indexes pour inclure le poids,"+
			"et générer le fichier somme des poids : tapez 4");
			System.out.println("Effectuer une recherche : tapez 6");
			System.out.println("Supprimer les indexes, le fichier poids, et le fichier corpus : tapez 8");
			System.out.println("quitter : tapez 9");
			command = sc.nextInt();
			switch (command) {
			//lister les fichiers du corpus
			case 1:
				System.out.println("Nombre de fichiers à indexer");
				nb_doc = sc.nextInt();
				
				
				long startTimeCorpus = System.currentTimeMillis();
				h = new TreeMap<>();
				Common.getDirectory(fCorpus, h, ".txt", nb_doc);
				Common.writeDirectory(h,nb_thread,nb_doc);

				long estimatedTimeCorpus = System.currentTimeMillis() - startTimeCorpus;
				System.out.println(estimatedTimeCorpus / 1000);
				break;
			//lire la liste des fichiers du corpus
			case 2:
				if (!fRevCorpus.exists())
					System.out.println("Attention le fichier n'existe pas, appelez la commande 1!");
				else {
					FileReader fr=new FileReader(fRevCorpus);
					 BufferedReader br = new BufferedReader(fr);
					 String line=br.readLine();
					 int cpt=0;
					while(line !=null){
						cpt++;
						line=br.readLine();
					}
					nb_doc=cpt*nb_thread;
					System.out.println(nb_doc);
				}
				break;
			//Indexer le corpus
			case 3:

				long startTime = System.currentTimeMillis();
				Normalizer stemmer[]=new Normalizer[nb_thread];
				Normalizer tokenizer[]=new Normalizer[nb_thread];
				
				for(int i=0;i<nb_thread;i++){
					stemmer[i]=new FrenchStemmer(common.Common.DIRRSC
							+ "stop.txt");
					tokenizer[i]=new FrenchTokenizer(common.Common.DIRRSC
							+ "stop.txt");
				}
				
				TaskIndexing index[]=new TaskIndexing[nb_thread];
				
				for(int i=0;i<nb_thread;i++){
					index[i]=new TaskIndexing(i+".corpus", stemmer[i], true,
							"index"+i, 10000/nb_thread);
				}
				
				for(int i=0;i<nb_thread;i++){
					index[i].start();
				}
				for(int i=0;i<nb_thread;i++){
					index[i].join();
				}
			
				//fusionne les indexes créé par les 2 threads
				TaskIndexing.fusionThreadsIndexes(nb_thread);
				//découper l'index en plusieurs index
				TaskIndexing.splitBinaryIndex(2);

				long estimatedTime = System.currentTimeMillis() - startTime;
				System.out.println(estimatedTime / 1000);
				
				break;
			case 4:

				long startTime1 = System.currentTimeMillis();
				File fIndexes = new File(Common.DIRINDEX);
				TreeMap<Integer, String> indexes = new TreeMap<>();
				TreeMap<Integer, Float> sum_weights = new TreeMap<>();
				Common.getDirectory(fIndexes, indexes, ".idx", -1);
				Weighting weight_1 = new Weighting(sum_weights, indexes, 0,
						indexes.size() / 2, nb_doc);
				Weighting weight_2 = new Weighting(sum_weights, indexes,
						indexes.size() / 2, indexes.size(), nb_doc);
				
				weight_1.start();
				weight_2.start();
				
				weight_1.join();
				weight_2.join();
				Weighting.saveWeights(Common.DIRINDEX + "docWeight"+Common.extWEIGHT, sum_weights);
				Weighting.saveWeights2();

				long estimatedTime1 = System.currentTimeMillis() - startTime1;
				System.out.println(estimatedTime1 / 1000);
				
				break;
			case 5:
				System.out.println("commande 4: indexation avec stemming, commande 5 indexation tokenizer");
			case 6:
				/*BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String req = br.readLine();
				Request request = new Request(req, new FrenchStemmer());
				request.search();*/

				/*Desktop desktop = Desktop.getDesktop();
				desktop.open(new File("/partage/public/iri/projetIRI/corpus/0060/006001/00600117.html")) ;*/
				
				Fenetre f= new Fenetre();
				
				break;
			case 8:
				//permet de supprimer les anciens indexes, et de créer le répertoire
				File index_dir = new File(Common.DIRINDEX);
				index_dir.mkdir();
				for (File file : index_dir.listFiles()) {
					file.delete();
				}
				fRevCorpus.delete();
				break;
			case 9:
				stop = true;
				break;
			}
		}

	



		

	}

}
