package fr.dauphine.sde.graph;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;

import fr.dauphine.sde.model.Params;
import fr.dauphine.sde.model.Service;
import fr.dauphine.sde.model.ServiceList;
import fr.dauphine.sde.model.extraction.OWLSExtractor;

/**
 * This class contains tools for evaluating the framework using the OWLS-TC
 * benchmark
 * 
 * @author Mohamed LAmine Mouhoub
 *
 */
public class EvaluationUtils {

	private static final String DBPEDIA_URL = "http://dbpedia.org/";

	private static boolean isTotallyDBpedia(List<String> io) {
		for (String string : io) {
			if (!string.startsWith(DBPEDIA_URL))
				return false;
		}
		return true;
	}

	/**
	 * Returns a string (meant to be used for logging/dialog boxes) containing
	 * the results of I/O extraction from OWLS-TCv4 with our framework using the
	 * parameters from the GUI
	 * 
	 * @param serviceList
	 * @param filteredList
	 * @return
	 */
	public static String printExtractedIOStats(ServiceList serviceList, List<String> filteredList) {
		int nbInputs = 0, nbOutputs = 0, nbExtrInputs = 0, nbExtrOutputs = 0, nbExtrOnto = 0, nbExtrText = 0;
		int nbrServices = filteredList.size();
		int nbAllIOServices = 0, nbAllORelServices = 0, nbAllTRelServices = 0, nbAllRelServices = 0;

		StringBuilder sb = new StringBuilder();

		System.out.println("====================");
		System.out.println("I/O extraction evaluation");

		for (String serviceName : filteredList) {
			Service service = serviceList.get(serviceName);
			// Collect stats
			int a = service.getInputs() == null ? 0 : service.getInputs().size();
			int b = service.getOutputs() == null ? 0 : service.getOutputs().size();
			int c = service.getExtractedInputs() == null ? 0 : service.getExtractedInputs().size();
			int d = service.getExtractedOutputs() == null ? 0 : service.getExtractedOutputs().size();

			int e = service.getOntoRelationsMatrix().stream().flatMapToInt(in -> in.stream().mapToInt(List::size))
					.sum();
			int f = service.getTextRelationsMatrix().stream().flatMapToInt(in -> in.stream().mapToInt(List::size))
					.sum();
			nbInputs += a;
			nbOutputs += b;
			nbExtrInputs += c;
			nbExtrOutputs += d;
			nbExtrOnto += e;
			nbExtrText += f;

			if (a == c && b == d) {
				nbAllIOServices++;
			}
			if (e > 0) {
				nbAllORelServices++;
			}
			if (f > 0) {
				nbAllTRelServices++;
			}
			if (e > 0 && f > 0) {
				serviceList.UsableServices.add(serviceName);
				nbAllRelServices++;
			}
		}
		sb.append("nbrServices=" + nbrServices + "\n");
		sb.append("nbInputs=" + nbInputs + "\n");
		sb.append("nbOutputs=" + nbOutputs + "\n");
		sb.append("nbExtrInputs=" + nbExtrInputs + "\n");
		sb.append("nbExtrOutputs=" + nbExtrOutputs + "\n");
		sb.append("nbAllIOServices=" + nbAllIOServices + "\n");
		sb.append("nbExtrOnto=" + nbExtrOnto + "\n");
		sb.append("nbExtrText=" + nbExtrText + "\n");
		sb.append("nbAllORelServices=" + nbAllORelServices + "\n");
		sb.append("nbAllTRelServices=" + nbAllTRelServices + "\n");
		sb.append("nbAllRelServices=" + nbAllRelServices + "\n");
		System.out.println(sb.toString());
		return sb.toString();
	}

	public static void main(String[] args) {
		File directory = new File(Params.OWLSTC_DIRECTORY);
		Collection<File> files = FileUtils.listFiles(directory, OWLSExtractor.EXTENSIONS, false);
		for (File file : files) {
			Service service = OWLSExtractor.getService(file);
			if (isTotallyDBpedia(service.getInputsURIs()) && isTotallyDBpedia(service.getOutputsURIs())) {
				System.out.println(service.getName());
			}
		}
	}

}
