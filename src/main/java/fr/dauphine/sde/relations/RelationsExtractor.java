package fr.dauphine.sde.relations;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Triple;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.simple.Sentence;
import fr.dauphine.sde.graph.GraphExtraction;
import fr.dauphine.sde.model.Params;
import fr.dauphine.sde.model.Service;
import fr.dauphine.sde.model.ServiceList;
import fr.dauphine.sde.model.extraction.OWLSExtractor;
import fr.dauphine.sde.model.extraction.TextUtils;
import fr.dauphine.sde.nlp.CoreNLPPipeline;

/**
 * A toolset for extracting I/O relations from both text and ontologies. This is
 * the main class that initiates the extraction process. It is called by JavaFX
 * Services to parallelize and split the extraction into tasks.
 * 
 * @author Mohamed LAmine Mouhoub
 *
 */
public class RelationsExtractor {

	private CoreNLPPipeline coreNLP;

	public RelationsExtractor() {
		coreNLP = new CoreNLPPipeline(true);
	}

	/**
	 * One call to extract them all, <br>
	 * And in the service bject save them. <br>
	 * --by a developper poet !
	 * 
	 * @param service
	 * @return
	 */
	private Service extractRelations(Service service) {

		extractOntologyRelations(service);

		recognizeIOinText(service);

		extractTextRelations(service);

		return service;
	}

	public void recognizeIOinText(Service service) {

		Annotation document = coreNLP.OWLSTC.get(service.getName());
		service.setAnnotation(document);
		extractTokens(service);

		TextUtils txtExt = new TextUtils(Params.similarityMethod);
		service.setInMatches(txtExt.allMatchedPairs(service.getInputs(), service.getTokens()));// sent.words()
		service.setOutMatches(txtExt.allMatchedPairs(service.getOutputs(), service.getTokens()));// sent.words()

		// Set the extracted Inputs and extracted Outputs
		service.setExtractedInputs(new ArrayList<>(service.getInMatches().keySet()));
		service.setExtractedOutputs(new ArrayList<>(service.getOutMatches().keySet()));
	}

	/**
	 * Extracts the textual relations between all I/O of a service using NLP
	 * techniques (Dependency Grammar, word2vec, etc)
	 * 
	 * @param service
	 */
	public void extractTextRelations(Service service) {

		List<List<List<List<String>>>> textMatrix = new ArrayList<>();

		int i = 0;
		for (Map.Entry<String, List<Triple<String, Integer, Integer>>> input : service.getInMatches().entrySet()) {
			textMatrix.add(new ArrayList<>());
			int j = 0;
			for (Map.Entry<String, List<Triple<String, Integer, Integer>>> output : service.getOutMatches()
					.entrySet()) {
				textMatrix.get(i).add(new ArrayList<>());
				int l = 0;
				for (Triple<String, Integer, Integer> in : input.getValue()) {
					textMatrix.get(i).get(j).add(new ArrayList<>());
					for (Triple<String, Integer, Integer> out : output.getValue()) {
						// Skip if not from the same sentence
						// bcz dependency trees are intra-sentence only
						if (in.getMiddle() != out.getMiddle()) {
							continue;
						}
						Sentence sent = new Sentence(
								service.getAnnotation().get(SentencesAnnotation.class).get(in.getMiddle()));
						List<String> path = sent.algorithms().dependencyPathBetween(in.getRight(), out.getRight());
						textMatrix.get(i).get(j).get(l).addAll(decryptPathToList(path));
					}
					l++;
				}
				j++;
			}
			i++;
		}

		service.setTextRelationsMatrix(textMatrix);
		List<String> textRelations = new ArrayList<>();
		service.getTextRelationsMatrix()
				.forEach(in -> in.forEach(row -> row.stream().filter(rel -> !rel.isEmpty()).collect(Collectors.toList())
						.forEach(rel -> textRelations
								.add(rel.stream().filter(s -> s != null).collect(Collectors.toList()).toString()))));
		service.setTextRelations(textRelations);
	}

	/**
	 * Extracts the ontology relations between all I/O of a service using SPARQL
	 * 
	 * @param service
	 */
	public void extractOntologyRelations(Service service) {
		service.setOntoRelationsMatrix(GraphExtraction.extractIORelationsMatrix(service));
		List<String> ontoRelations = new ArrayList<>();
		service.getOntoRelationsMatrix()
				.forEach(
						in -> in.forEach(
								row -> row.stream().filter(rel -> !rel.isEmpty()).collect(Collectors.toList())
										.forEach(rel -> ontoRelations.add(rel.stream()
												.filter(n -> n != null && n.isURI()).map(n -> n.getLocalName())
												.collect(Collectors.toList()).toString()))));
		service.setOntoRelations(ontoRelations);
	}

	private static List<String> decryptPathToList(List<String> path) {
		List<String> result = new ArrayList<>();
		// words are at uneven indices, even indices contain dependency labels
		for (int i = 0; i < path.size(); i = i + 2) {
			result.add(path.get(i));
		}
		return result;
	}

	/**
	 * Tokenize the textual description (split the description into sentences
	 * then sentences into tokens). The results are stored directly in the
	 * service object
	 * 
	 * @param service
	 */
	private void extractTokens(Service service) {
		List<List<String>> tokens = new ArrayList<>();
		service.getAnnotation().get(SentencesAnnotation.class)
				.forEach(sentAnn -> tokens.add(new Sentence(sentAnn).words()));
		tokens.forEach(list -> {
			list = TextUtils.extremeCleanAndSplit(list);
		});
		service.setTokens(tokens);
	}

	/**
	 * Extract the relations from all owls-tc services. There are about 1000
	 * services in total. i and j are start and stop indices.<br>
	 * Save them in a json file
	 * 
	 * @param begin
	 * @param end
	 * @throws Exception
	 */
	protected void extractFromOWLSTC(int begin, int end) {
		// coreNLP = new CoreNLPPipeline(true);
		ServiceList slist = new ServiceList();

		File directory = new File(Params.OWLSTC_DIRECTORY);
		Collection<File> files = FileUtils.listFiles(directory, OWLSExtractor.EXTENSIONS, false);
		int i = 0;
		for (File file : files) {
			if (i > end)
				break;
			if (i < begin) {
				i++;
				continue;
			}
			Service service = OWLSExtractor.getService(file);
			slist.add(extractRelations(service));

			i++;
		}
		slist.saveJSON();
	}

}
