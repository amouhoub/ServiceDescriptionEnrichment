package fr.dauphine.sde.relations;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.graph.Node;

import fr.dauphine.sde.model.Service;
import fr.dauphine.sde.nlp.similarity.Word2VecBridge;

/**
 * A toolset for matching I/O relations
 * 
 * @author Mohamed LAmine Mouhoub
 *
 */
public abstract class RelationMatching {

	/**
	 * Matches ontology and text I/O relations and evaluates/ranks the ontology
	 * relations. The results are directly stored in the Service Object
	 * 
	 * @param service
	 *            the service for which a relation evaluation is needed
	 */
	public static void evaluateRelations(Service service) {
		service.setRelationsScores(new HashMap<>());
		service.setRelations(new HashMap<>());

		// Make sure there are at least one extracted input and on extracted
		// output
		if (!service.getExtractedInputs().isEmpty() && !service.getExtractedOutputs().isEmpty()) {
			for (int i = 0; i < service.getOntoRelationsMatrix().size(); i++) {
				for (int j = 0; j < service.getOntoRelationsMatrix().get(i).size(); j++) {
					// For all onto relations
					for (List<Node> ontoRels : service.getOntoRelationsMatrix().get(i).get(j)) {
						String ontoRel = listToString(ontoRels);
						HashMap<List<String>, double[]> textMatchMap = new HashMap<>();
						HashMap<String, double[]> textSMatchMap = new HashMap<>();
						for (List<String> textRels : service.getTextRelationsMatrix().get(i).get(j)) {
							String textRel = textRels.toString();
							textMatchMap.put(textRels, relationsSimilarity(ontoRel, textRel));
							textSMatchMap.put(textRel, textMatchMap.get(textRels));
						}
						if (!textMatchMap.isEmpty()) {
							service.getRelations().put(ontoRel, textSMatchMap);
							service.getRelationsScores().put(ontoRels, textMatchMap);
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the similarity value between two I/O relations (a textual and an
	 * ontological). The similarity is measured between [0,1]
	 * 
	 * @param ontoRel
	 * @param textRel
	 * @return
	 */
	private static double[] relationsSimilarity(String ontoRel, String textRel) {
		return Word2VecBridge.stringSimilarity(ontoRel, textRel);
	}

	/**
	 * Converts a on ontology IO relation into a string format for matching and
	 * for printing
	 * 
	 * @param list
	 * @return
	 */
	private static String listToString(List<Node> list) {
		return list.stream().filter(n -> n != null && n.isURI()).map(n -> n.getLocalName()).collect(Collectors.toList())
				.toString();
	}
}
