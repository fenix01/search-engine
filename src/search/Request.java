package search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

import main.Main;
import common.Common;
import tools.Normalizer;

public class Request {
	
	private ArrayList<String> request;
	private float sum_weight;
	private HashMap<String,Float> weigths;
	private String chemin_idx=null;
	
	public Request(String req, Normalizer norm,String path){
		//permet de normaliser la requête
		this.request = norm.normalize(req);
		this.weigths = new HashMap<>();
		this.chemin_idx=path;
	}
	
	/**
	 * permet de rechercher les documents pour la requête donnée
	 * @return la liste des documents sous forme d'une chaîne de caractères
	 * @throws IOException 
	 */
	public String search() throws IOException{
		

		long startTime = System.currentTimeMillis();
		
		//contient pour chaque mot sa liste de documents
		LinkedList<ArrayList<Couple>> ltRequest = new LinkedList<>(); 
		for (String word : request){
			if(!Common.isEmptyWord(word)){
				ArrayList<Couple> ltDocs;
				//extrait pour chaque mot sa liste de docs avec les poids
				ltDocs = extractWordFromIndex(word);
				//calcul le poids des mots de la requête
				
				if (ltDocs != null){
					System.out.println(word);
					float weight =  (float) Math.log10(Main.nb_doc/ltDocs.size());
					weigths.put(word, weight);
					sum_weight += (weight * weight);
					ltRequest.add(ltDocs);
					}
			}
		}
		
		//calcul la racine de la somme des poids de la requête
		sum_weight = (float) Math.sqrt(sum_weight);
		
		
		//il faut à présent calculer la similarité entre la requête et la liste des docs extraits
		ArrayList<Couple> ltFusion = fusion(ltRequest);
		if(ltFusion==null){
			
			return "Aucun résultat trouvé";
		}
		
		//on récupère les similarités
		HashMap<String,Float> similarities = similarity(ltFusion);
		
		SortedSet<Entry<String, Float>> sortedSim = Common.sortMap(similarities);
		

		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println(estimatedTime / 1000);
		
		String res= ltFusion.size()+" document(s) trouvé(s) en "+Long.toString(estimatedTime)+"  ms"+"\n";
		
		int limit=0;
		
		for(Entry<String, Float> entry : sortedSim){
			res+=entry.getKey()+"\n";
			limit++;
			if(limit==31)
				break;
		}
		return res;
	}
	
	private HashMap<String,Float> similarity(ArrayList<Couple> ltFusion) throws IOException{
		float sim = 0;
		HashMap<Integer,Float> similarities = new HashMap<>();
		HashMap<String,Float> similarities2 = new HashMap<>();
		
		for (Couple cpDoc : ltFusion){
			sim=0;
			for (Map.Entry<String, Float> wordDoc : cpDoc.getWeight().entrySet()){
				float req_weight = weigths.get(wordDoc.getKey());
				sim+= (req_weight * wordDoc.getValue());
				
			}
			similarities.put(cpDoc.getDocID(), sim);
		}
		FileReader fr = new FileReader(chemin_idx+"weight"+Common.extWEIGHT);
		BufferedReader br = new BufferedReader(fr);
		
		String line = null;
		while ((line = br.readLine()) != null && similarities.size() > 0){
			String[] lineSumWeigth = line.split("\t");
			
			Float simReq = similarities.get(Integer.parseInt(lineSumWeigth[0]));
			if (simReq != null){
				float sumWeight = Float.parseFloat(lineSumWeigth[1]);
				//System.out.println(lineSumWeigth[0]+"\t"+lineSumWeigth[1]);
				simReq = (float) (simReq / (this.sum_weight * sumWeight));
				similarities.remove(lineSumWeigth[0]);
				similarities2.put(lineSumWeigth[2], simReq);
			}
		}
		System.out.println(similarities2.size());
		br.close();
		return similarities2;
	}
	
	/**
	 * extrait un mot de l'index est retourne la liste des documents et le poids
	 * @param word
	 * @return HashMap dont l'ordre d'insertion est conserver (docID,poids)
	 */
	private ArrayList<Couple> extractWordFromIndex(String word) {
		ArrayList<Couple> ltDocs = null;
		String firstOcc = Common.firstOcc(word, 2);
		File fIndex = new File(chemin_idx + firstOcc + Common.extIDX);
		String line = null;
		try {
			line = Common.sequentialBinarySearch(fIndex, word);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!line.equals("")){
			String[] lineIndex = line.split("\t");
			String[] docs = lineIndex[1].split(","); 
			
			ltDocs = new ArrayList<Couple>(docs.length);
			for (String doc : docs){
				String[] docEl = doc.split(":");
				Couple cp = new Couple(Integer.parseInt(docEl[0]));
				cp.addWord(word , Float.parseFloat(docEl[1]));
				ltDocs.add(cp);
				
			}
			
			
		}
		return ltDocs;
	}

	/**
	 * permet d'appliquer l'algo de fusion sur X listes de documents.
	 * Chaque liste de documents est associé à un mot clé de la requête
	 * @param ltRequest prend une liste de liste de documents
	 * @return une liste de documents contenant l'intersection de toutes les listes
	 */
	private ArrayList<Couple> fusion(LinkedList<ArrayList<Couple>> ltRequest){
		//liste finale de documents
		//ArrayList<Couple> ltDocs = new ArrayList<>();
		if(ltRequest.size()==1){
			return ltRequest.getFirst();
		}
		// tant qu'il reste au moins 2 listes de documents à parcourir
		while (ltRequest.size() > 1) {
			int l1 = 0,l2 = 0;
			//on dépile deux listes de documents
			ArrayList<Couple> ltDoc1 = ltRequest.pollFirst();
			ArrayList<Couple> ltDoc2 = ltRequest.pollFirst();
			ArrayList<Couple> ltDoc3 = new ArrayList<>();
			//on applique l'algo de fusion
			while (l1 != ltDoc1.size() && l2 != ltDoc2.size()){
				//on dépile deux documents
				Couple c1 = ltDoc1.get(l1);
				Couple c2 = ltDoc2.get(l2);
				//on compare si leurs ids est pareil
				if (c1.getDocID() == c2.getDocID()){
					ltDoc3.add(c1);
					c1.fusion(c2);
					l1++;
					l2++;
				}
				else if (c1.getDocID() < c2.getDocID())
					l1++;
				else l2++;
			}
			ltRequest.add(ltDoc3);
		}
		return ltRequest.pollFirst();
		
	}

}
