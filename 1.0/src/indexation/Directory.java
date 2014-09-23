package indexation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Directory implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int df(){
		return refs.size(); 
	}
	
	private HashMap<Integer,Integer> refs;

	public Directory() {
		this.refs = new HashMap<>();
	}

	public HashMap<Integer, Integer> getRefs() {
		return refs;
	}
	
	public void printMap(){
		for(Map.Entry<Integer, Integer> val : refs.entrySet()){
			System.out.println("key="+val.getKey()+" val="+val.getValue());
		}
	}

}
