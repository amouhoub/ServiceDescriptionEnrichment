/**
 * 
 */
package fr.dauphine.sde.nlp.similarity;

/**
 * This interface defines the required methods to calculate the word-to-word
 * similarity
 * 
 * @author Mohamed LAmine Mouhoub
 *
 */
public abstract class WordSimilarity {

	public WordSimilarity(double threshold) {
		this.threshold = threshold;
	}

	public WordSimilarity() {
	}

	/**
	 * Default threshold in [0,1]
	 */
	protected double threshold;

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	/**
	 * Returns true if the similarity value between two words is above a given
	 * threshold
	 * 
	 * @see fr.dauphine.sde.nlp.similarity.WordSimilarity#areSimilar(java.lang.String,
	 *      java.lang.String)
	 */
	public boolean areSimilar(String word1, String word2, double threshold) {
		return similarity(word1, word2) >= threshold ? true : false;
	}

	/**
	 * Returns true if the similarity value between two words is above a
	 * predefined threshold
	 * 
	 * @see fr.dauphine.sde.nlp.similarity.WordSimilarity#areSimilar(java.lang.String,
	 *      java.lang.String)
	 */
	public boolean areSimilar(String word1, String word2) {
		return similarity(word1, word2) >= threshold ? true : false;
	}

	public abstract double similarity(String word1, String word2);

}
