package indexation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlParser {

	private HashMap<String, Double> bestTags;
	private HashMap<String,String> textTags;

	public HtmlParser() {
		this.bestTags = new HashMap<>();
		this.textTags = new HashMap<>();
		this.bestTags.put("title", 5.0);
		this.bestTags.put("meta[content]", 4.0);
		this.bestTags.put("h1", 3.5);
		this.bestTags.put("h2", 3.1);
		this.bestTags.put("h3", 2.7);
		this.bestTags.put("h4", 2.3);
		this.bestTags.put("h5", 1.9);
		this.bestTags.put("h6", 1.5);
		this.bestTags.put("p", 1.5);
		this.bestTags.put("span", 1.5);
		this.bestTags.put("td", 1.4);
		this.bestTags.put("li", 1.4);
		this.bestTags.put("b", 1.4);
		this.bestTags.put("a[title]", 1.0);
		this.bestTags.put("a", 1.0);
		this.bestTags.put("img[title]", 1.7);
	}
	
	public double getCoef(String tag){
		return bestTags.get(tag);
	}
	
	public String getFullTextTags(){
		StringBuilder stb = new StringBuilder();
		for (String textTag : textTags.values()){
			stb.append(textTag);
			stb.append(" ");
		}
		return stb.toString();
	}
	
	public void parseText(File fileName) throws IOException{
		this.textTags.clear();
		Document doc = Jsoup.parse(fileName, "UTF-8");
		for (Entry<String, Double> tag : this.bestTags.entrySet()) {
			StringBuilder stb = new StringBuilder();
			Elements el_list = doc.select(tag.getKey());
			for (Element p_el : el_list)
			{
				stb.append(p_el.text());
				stb.append(" ");
			}
			this.textTags.put(tag.getKey(), stb.toString());
		}
	}

	public HashMap<String, String> getTextTags() {
		return textTags;
	}

}
