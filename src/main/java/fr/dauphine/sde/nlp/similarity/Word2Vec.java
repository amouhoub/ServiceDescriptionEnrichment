/**
 * 
 */
package fr.dauphine.sde.nlp.similarity;

import fr.dauphine.sde.model.Params;

/**
 * This class implements a word2vec based "semantic" string similarity.
 * Currently, the word2vec similarity is calculated on a remote server using our
 * dedicated API.
 * 
 * @see fr.dauphine.sde.nlp.similarity.Word2VecBridge
 * 
 * @author Mohamed LAmine Mouhoub
 *
 */
public class Word2Vec extends WordSimilarity {

	// TODO normalize fuzzy score and set this default threshold value;
	// private static final double DEFAULT_WORD2VEC_NORMALIZED_THRESHOLD = 0.5;

	public Word2Vec(double threshold) {
		super(threshold);
	}

	public Word2Vec() {
		super(Params.SIMILARITY_THRESHOLD);
	}

	/**
	 * Since this similarity is calculated on a dedicated server, this class
	 * calls our deployed word2vec API through a bridge class.
	 * 
	 * @see fr.dauphine.sde.nlp.similarity.Word2VecBridge
	 * @see fr.dauphine.sde.nlp.similarity.WordSimilarity#similarity(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public double similarity(String word1, String word2) {
		double d = Word2VecBridge.wordSimilarity(word1.toLowerCase(), word2.toLowerCase());
		return d;
	}

	@Override
	public String toString() {
		return "word2vec similarity";
	}

}
