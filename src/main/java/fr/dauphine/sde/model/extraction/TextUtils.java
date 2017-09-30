/**
 * 
 */
package fr.dauphine.sde.model.extraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import fr.dauphine.sde.nlp.similarity.WordSimilarity;

/**
 * This class provides the necessary methods to locate and extract the I/O words
 * from the textual description.
 * 
 * @author aminos
 *
 */
public class TextUtils {

	private WordSimilarity matchingMethod;

	/**
	 * A HashSet of all nasty chars found in OWLS-TC in text descriptions.
	 */
	private static final Set<String> NASTY_CHARS = Stream.of(" ", "", "+", "&", "/", ";", ":", "@", "?", "=", "(", ")",
			"!", "[", "]", "{", "}", "-", "*", "%", "#", "<", ">", "$", "^", "\\", ",", ".")
			.collect(Collectors.toCollection(HashSet::new));;
	/**
	 * Use the nasty chars along with the invisible nasty empty char(it's not
	 * the null char, thanks to UTF-8 !!!) as separators for sentences, etc.
	 */
	private static final String NASTY_SEPARATORS = StringUtils.join(NASTY_CHARS, "");

	public TextUtils(WordSimilarity matchingMethod) {
		this.matchingMethod = matchingMethod;
	}

	/**
	 * Finds the first similar word to the given word in a given list of words
	 * and returns its index in the list
	 * 
	 * @param wordsList
	 *            list of words to search in
	 * @param word
	 *            word to find similar for
	 * @return the index of the first similar word if it exists, -1 otherwise
	 */
	public int findSimilar(String word, List<String> wordsList) {
		for (String s : wordsList) {
			if (matchingMethod.areSimilar(word, s)) {
				return wordsList.indexOf(s);
			}
		}
		return -1;
	}

	/**
	 * Finds the first similar word to the given word in a given list of words
	 * using a given method and returns its index in the list
	 * 
	 * @param wordsList
	 *            list of words to search in
	 * @param word
	 *            word to find similar for
	 * @return the index of the first similar word if it exists, -1 otherwise
	 */
	public static int findSimilar(String word, List<String> wordsList, WordSimilarity method) {
		for (int i = 0; i < wordsList.size(); i++) {
			if (method.areSimilar(word, wordsList.get(i))) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Finds all the occurrences of similar words to the word in given list of
	 * words
	 * 
	 * @param wordsList
	 *            list of words to search in
	 * @param word
	 *            word to find similar for
	 * @return the similar word to the word s in the list l if it exists, ""
	 *         otherwise
	 */
	public List<Integer> findAllSimilars(String word, List<String> wordsList) {
		List<Integer> result = new ArrayList<>();
		for (int i = 0; i < wordsList.size(); i++) {
			if (matchingMethod.areSimilar(word, wordsList.get(i))) {
				result.add(i);
			}
		}
		return result;
	}

	/**
	 * Finds all the occurrences of similar words to the word in given list of
	 * words. Returns a list of pairs containing both the matching words and
	 * their positions.
	 * 
	 * @param wordsList
	 *            Matrix of words to search in. Each row represents a sentence,
	 *            columns represent tokens.
	 * @param word
	 *            word to find similar for
	 * @return a list of similar words to the word s in the list l if they
	 *         exists, null otherwise
	 */
	public List<Triple<String, Integer, Integer>> findAllSimilarsPairs(String word, List<List<String>> wordsList) {
		List<Triple<String, Integer, Integer>> result = new ArrayList<>();
		List<String> split = extremeCleanAndSplitWord(word);
		for (int i = 0; i < wordsList.size(); i++) {
			for (int j = 0; j < wordsList.get(i).size(); j++) {
				if (NASTY_CHARS.contains(wordsList.get(i).get(j))) {
					continue;
				}
				for (String Wi : split) {
					if (matchingMethod.areSimilar(Wi, wordsList.get(i).get(j))) {
						result.add(Triple.of(wordsList.get(i).get(j), i, j));
						break;
					}
				}
			}
		}
		return result.size() != 0 ? result : null;
	}

	public static List<String> extremeCleanAndSplitWord(String word) {
		List<String> result = new ArrayList<>();
		for (String s : StringUtils.split(word, NASTY_SEPARATORS)) {
			if (!NASTY_CHARS.contains(s)) {
				result.addAll(Arrays.asList(StringUtils.splitByCharacterTypeCamelCase(s)));
			}
		}
		return result;
	}

	public static List<String> extremeCleanAndSplit(List<String> words) {
		List<String> result = new ArrayList<>();
		for (String word : words) {
			result.addAll(extremeCleanAndSplitWord(word));
		}
		return result;
	}

	/**
	 * Finds all the occurrences of similar words to the word in given list of
	 * words using the given method
	 * 
	 * @param wordsList
	 *            list of words to search in
	 * @param word
	 *            word to find similar for
	 * @param method
	 *            specify a word similarity method
	 * @return the similar word to the word s in the list l if it exists, ""
	 *         otherwise
	 */
	public static List<Integer> findAllSimilars(String word, List<String> wordsList, WordSimilarity method) {
		List<Integer> result = new ArrayList<>();
		for (int i = 0; i < wordsList.size(); i++) {
			if (method.areSimilar(word, wordsList.get(i))) {
				result.add(i);
			}
		}
		return result;
	}

	/**
	 * Finds all the occurrences of similar words to the word in given list of
	 * words using the given method Returns a list of pairs (word, indice)
	 * containing both the matching word and its position in the sentence.
	 * 
	 * @param wordsList
	 *            list of words to search in
	 * @param word
	 *            word to find similar for
	 * @param method
	 *            specify a word similarity method
	 * @return the similar word to the word s in the list l if it exists, ""
	 *         otherwise
	 */
	public static List<Pair<String, Integer>> findAllSimilarsPair(String word, List<String> wordsList,
			WordSimilarity method) {
		List<Pair<String, Integer>> result = new ArrayList<>();
		for (int i = 0; i < wordsList.size(); i++) {
			if (method.areSimilar(word, wordsList.get(i))) {
				result.add(Pair.of(wordsList.get(i), i));
			}
		}
		return result;
	}

	/**
	 * Returns a HashMap containing all the matches (word positions) of words
	 * from the given patterns list in the given words list
	 * 
	 * @param patterns
	 *            list of pattern words
	 * @param words
	 *            list of target words
	 * @return
	 */
	public Map<String, List<Integer>> allMatchedIndexes(List<String> patterns, List<String> words) {
		Map<String, List<Integer>> result = new HashMap<>();
		for (String pattern : patterns) {
			result.put(pattern, findAllSimilars(pattern, words));
		}
		return result;
	}

	/**
	 * Returns a HashMap containing all the matches (words and their positions)
	 * of words from the given patterns list in the given words list
	 * 
	 * @param patterns
	 *            list of pattern words
	 * @param words
	 *            list of target words
	 * @return
	 */
	public Map<String, List<Triple<String, Integer, Integer>>> allMatchedPairs(List<String> patterns,
			List<List<String>> words) {
		Map<String, List<Triple<String, Integer, Integer>>> result = new HashMap<>();
		for (String pattern : patterns) {
			List<Triple<String, Integer, Integer>> similars = findAllSimilarsPairs(pattern, words);
			if (similars != null) {
				result.put(pattern, similars);
			}
		}
		return result;
	}
}
