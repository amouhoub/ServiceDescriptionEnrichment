package fr.dauphine.sde.nlp.similarity;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.io.IOUtils;

import fr.dauphine.sde.model.Params;

/**
 * This class serves as a bridge between the {@link WordSimilarity}
 * {@link Word2Vec} class and our dedicated API on a remote server to calculate
 * word2vec similarity.<br>
 * We currently calculate it on a remote server due to the huge amount of memory
 * required by a good word2vec model like the GoogleNews300 trained model.
 * 
 * @author Mohamed LAmine Mouhoub
 *
 */
public class Word2VecBridge {

	// Local default API endpoint
	// private static final String WORD2VEC_API_URL =
	// "http://192.168.0.11:4567/similarity/";

	protected static double wordSimilarity(String word1, String word2) {
		double d = parseWordSimilarity(requestSimilarity("word?w1=" + word1 + "&w2=" + word2 + "&force=true"));
		return d;
	}

	/**
	 * Returns an array of different similarity measures between two strings.
	 * <ul>
	 * <li>averageSum : average of the sum of all the word-word similarity
	 * matrix</li>
	 * <li>vectorSum : sum of all word2vec vectors of all words for each
	 * sentence</li>
	 * <li>vectorProd : product of all w2v vectors of all words for each
	 * sentence</li>
	 * </ul>
	 * 
	 * @param s1
	 *            String 1
	 * @param s2
	 *            String 2
	 * @return [averageMax, vectorSum, vectorProd]
	 */
	public static double[] stringSimilarity(String s1, String s2) {
		try {
			return parseStringSimilarity(requestSimilarity(
					"sentence?s1=" + URLEncoder.encode(s1, "UTF-8") + "&s2=" + URLEncoder.encode(s2, "UTF-8")));
		} catch (UnsupportedEncodingException e) {
			// The HTTP.UTF_8 is ALWAYS a supported encoding
		}
		return null;
	}

	private static double parseWordSimilarity(JsonObject jsonResult) {
		if (!jsonResult.get("result").toString().equals("\"NaN\"")) {
			return jsonResult.getJsonNumber("result").doubleValue();
		} else {
			return Double.NaN;
		}
	}

	private static double[] parseStringSimilarity(JsonObject jsonResult) {
		double[] result = new double[3];
		result[0] = jsonResult.getJsonObject("result").getJsonNumber("averageMax").doubleValue();
		result[1] = jsonResult.getJsonObject("result").getJsonNumber("vectorSum").doubleValue();
		result[2] = jsonResult.getJsonObject("result").getJsonNumber("vectorProd").doubleValue();
		return result;
	}

	private static JsonObject requestSimilarity(String args) {
		try {
			URL url = new URL(Params.WORD2VEC_API_URL + args);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			connection.connect();
			// TODO Correct JSON syntax error in the API code
			String body = IOUtils.toString(connection.getInputStream());
			body = body.replace("NaN", "\"NaN\"");
			// System.out.println(body);
			JsonReader jsonReader = Json.createReader(new StringReader(body));
			JsonObject result = jsonReader.readObject();
			jsonReader.close();
			return result;

		} catch (IOException e) {
			System.err.println("Could not get similarity from w2v API");
			System.err.println("REQUEST URL=" + args);
			e.printStackTrace();
			return null;
		}
	}

}
