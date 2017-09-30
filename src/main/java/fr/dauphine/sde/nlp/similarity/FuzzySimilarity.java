/**
 * 
 */
package fr.dauphine.sde.nlp.similarity;

import java.util.Locale;

import org.apache.commons.text.beta.similarity.FuzzyScore;

import fr.dauphine.sde.model.Params;

/**
 * This class implements the basic FuzzyScore (Apache Commons) string similarity
 * for a normalized similarity value
 * 
 * @author aminos
 *
 */
public class FuzzySimilarity extends WordSimilarity {

	// TODO normalize fuzzy score and set this default threshold value;
	// private static final double DEFAULT_FUZZY_NORMALIZED_THRESHOLD = 0.7;

	// private static final double DEFAULT_FUZZY_THRESHOLD = 6;

	private FuzzyScore fuzzy;

	public FuzzySimilarity(double threshold) {
		super(threshold);
		fuzzy = new FuzzyScore(Locale.ENGLISH);
	}

	public FuzzySimilarity() {
		super(Params.SIMILARITY_THRESHOLD);
		fuzzy = new FuzzyScore(Locale.ENGLISH);
	}

	public FuzzySimilarity(double threshold, Locale locale) {
		super(threshold);
		fuzzy = new FuzzyScore(locale);
	}

	/**
	 * <b>DOES NOT YET Return a similarity score between [0,1]</b>
	 * 
	 * @see fr.dauphine.sde.nlp.similarity.WordSimilarity#similarity(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public double similarity(String word1, String word2) {
		return fuzzy.fuzzyScore(word1, word2);
	}

	@Override
	public String toString() {
		return "Commons Text FuzzyScore similarity";
	}

}
