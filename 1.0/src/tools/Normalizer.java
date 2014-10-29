package tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Interface de normalisation des mots
 * @author xtannier
 *
 */
public interface Normalizer {
	public ArrayList<String> normalize(String request) throws IOException;
	public ArrayList<String> normalize(File fileName) throws IOException;
}
