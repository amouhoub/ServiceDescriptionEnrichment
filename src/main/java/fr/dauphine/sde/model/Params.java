package fr.dauphine.sde.model;

import fr.dauphine.sde.nlp.similarity.Word2Vec;
import fr.dauphine.sde.nlp.similarity.WordSimilarity;

/**
 * Params contains static parameters pre-set to their default values. Some
 * values such as the word2vec api and ontology endpoints should be directly set
 * in the config.properties file
 * 
 * <ul>
 * <li><b>WORD2VEC_API_URL</b> = refer to config.properties
 * <li><b>ONTOLOGY_ENDPOINT</b> = refer to config.properties
 * <li><b>OWLSTC_DIRECTORY</b> = refer to config.properties
 * <li><b>OWLSTC_ANNOTATIONS_PATH</b> = refer to config.properties
 * <li><b>SIMILARITY_THRESHOLD</b> = 0.5
 * <li><b>maxDistance</b> = 0
 * <li><b>maxHierarchy</b> = 0
 * <li><b>similarityMethod</b> = new Word2Vec()
 * <li><b>FilterDCG</b> = true
 * <li><b>FilterSameOntology</b> = false
 * <li><b>FilterSubClass</b> = false
 * <li><b>OWLS_TC_Pattern</b> = true
 * </ul>
 * 
 * TODO: set default values from a config.properties file at compilation
 * 
 */
public class Params {

	/**
	 * TODO: load them from a properties file.
	 */
	public static String WORD2VEC_API_URL;

	/**
	 * Link to a default SPARQL endpoint hosting an RDF/OWL repository
	 * containing ontologies
	 */
	public static String ONTOLOGY_ENDPOINT;

	/**
	 * Hardcoded OWLS-TC directory, included in the project resources because
	 * the official OWLS-TC link often goes off indefinitely
	 */
	public static String OWLSTC_DIRECTORY;

	/**
	 * Pre-processed OWLS-TCv4 NLP annotations for textual descriptions to avoid
	 * a cold-start
	 */
	public static String OWLSTC_ANNOTATIONS_PATH;

	public static double SIMILARITY_THRESHOLD = 0.5;

	public static int maxDistance = 0;

	public static int maxHierarchy = 0;

	public static WordSimilarity similarityMethod = new Word2Vec();

	/**
	 * Avoid loops (DCG, Directed Cyclic Graphs)
	 */
	public static boolean FilterDCG = true;

	/**
	 * Filter intermediate nodes from external ontologies including
	 */
	public static boolean FilterSameOntology = false;

	/**
	 * Filter out rdfs:subClassOf properties
	 */
	public static boolean FilterSubClass = false;

	/**
	 * true: OWLS-TC Property extraction pattern false: normal rdfs:domain |
	 * rdfs:range pattern
	 */
	public static boolean OWLS_TC_Pattern = true;

}
