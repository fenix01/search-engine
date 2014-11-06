package search;

import java.util.HashMap;

/**
 * classe java permettant de stocker un couple d'entier,double
 * @author Yohann
 *
 */
public class Couple {
	
	private int docID;
	private HashMap<String,Float> weight;
	
	public int getDocID() {
		return docID;
	}

	public HashMap<String,Float> getWeight() {
		return weight;
	}

	Couple(int docID){
		this.docID = docID;
		this.weight = new HashMap<>();
	}
	
	public void addWord(String word, float weight_){
		this.weight.put(word, weight_);
	}
	
	public void fusion(Couple c2){
		weight.putAll(c2.getWeight());
	}

}
