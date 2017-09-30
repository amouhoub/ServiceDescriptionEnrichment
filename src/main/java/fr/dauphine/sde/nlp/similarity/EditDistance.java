/**
 * 
 */
package fr.dauphine.sde.nlp.similarity;

import org.apache.commons.text.beta.similarity.LevenshteinDistance;

/**
 * This class implements the basic Edit (Levenshtein) Distance for a normalized
 * similarity value between words in [0,1]
 * 
 * @author Mohamed LAmine Mouhoub
 *
 */
public class EditDistance extends WordSimilarity {

	public static final double GOOD_LEVDIST_THRESHOLD = 0.7;

	private LevenshteinDistance levDistance;

	public EditDistance(double threshold) {
		super(threshold);
		this.levDistance = new LevenshteinDistance();
	}

	public EditDistance() {
		super(GOOD_LEVDIST_THRESHOLD);
		this.levDistance = new LevenshteinDistance();
	}

	/**
	 * Returns a normalized edit distance value between [0,1] based on the
	 * following equation:
	 * 
	 * normalizedDistance = 1-distance(word1,word2)/max(word1.length,
	 * word2.length)
	 * 
	 * @return a normalized value between [0,1]
	 * @see fr.dauphine.sde.nlp.similarity.WordSimilarity#similarity(java.lang.String,
	 *      java.lang.String)
	 */

	@Override
	public double similarity(String word1, String word2) {
		return 1 - (double) levDistance.apply(word1, word2) / Math.max(word1.length(), word2.length());
	}
	
	@Override
	public String toString() {
		return "Levenshtein similarity";
	}

}
