package fr.dauphine.sde.model.extraction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.jena.rdf.model.impl.Util;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import fr.dauphine.sde.model.Params;
import fr.dauphine.sde.model.Service;

/**
 * Contains methods to extract the local names of inputs, outputs and the
 * textual service description of from OWLS service description. Recommendation
 * : Please use a separate instance of this extractor for each OWLS file to
 * reduce file access costs.
 * 
 * @author aminos
 *
 */
public class OWLSExtractor {

	public final static String[] EXTENSIONS = { "owls" };

	/**
	 * A HashMap containing the OWLS-TC services descriptions, i.e: Inputs,
	 * Outputs and Text. The HashMap keys are the file names. The detailed
	 * contents are:
	 * <ul>
	 * <li>Text description from owls file under profile:textDescription</li>
	 * <li>List of INPUT elements as String: can either be:
	 * <ul>
	 * <li>TODO A label obtained by following the rdfs:label link</li>
	 * <li>A URI's local name in case there is no rdfs:label as in OWLS-TC</li>
	 * </ul>
	 * </li>
	 * <li>List of OUTPUT elements as String: can either be:
	 * <ul>
	 * <li>TODO A label obtained by following the rdfs:label link</li>
	 * <li>A URI's local name in case there is no rdfs:label as in OWLS-TC</li>
	 * </ul>
	 * </li>
	 * </ul>
	 */
	private Map<String, Triple<List<String>, List<String>, String>> OWLSTCServices;

	public OWLSExtractor(File owlsDirectory, Boolean localNames) {
		OWLSTCServices = extractAllFromDirectory(owlsDirectory, localNames);
	}

	public OWLSExtractor() {
	}

	/**
	 * Returns a file corresponding to the given filename. The interest of this
	 * method is to avoid using full paths elsewhere
	 * 
	 * @param owlsFilename
	 * @return
	 */
	public static File file(String owlsFilename) {
		return new File(Params.OWLSTC_DIRECTORY + owlsFilename);
	}

	private Map<String, Triple<List<String>, List<String>, String>> extractAllFromDirectory(File owlsDirectory,
			Boolean localNames) {
		Map<String, Triple<List<String>, List<String>, String>> result = new HashMap<>();
		Collection<File> files = FileUtils.listFiles(owlsDirectory, EXTENSIONS, false);
		for (File file : files) {
			result.put(file.getName(), extractFromFile(file, localNames));
		}
		return result;
	}

	/**
	 * Create a triple containing the Inputs, Outputs and Text elements after an
	 * extracting them from the given OWLS file
	 * 
	 * @param owlsFile
	 */
	public static Triple<List<String>, List<String>, String> extractFromFile(File owlsFile, Boolean localNames) {

		List<String> inputs = null;

		List<String> outputs = null;

		String text = null;

		Document document;
		try {

			inputs = new ArrayList<>();
			outputs = new ArrayList<>();

			/* Create an instance of SAXBuilder */
			SAXBuilder sxb = new SAXBuilder();
			document = sxb.build(owlsFile.getAbsolutePath());

			Element racine = document.getRootElement();

			String xpStrDesc = ".//profile:textDescription";
			String xpStrIn = ".//process:Input/process:parameterType";
			String xpStrOut = ".//process:Output/process:parameterType";

			/* Recherche de la liste des Input/Output */
			XPath xpaD = XPath.newInstance(xpStrDesc);
			XPath xpaI = XPath.newInstance(xpStrIn);
			XPath xpaO = XPath.newInstance(xpStrOut);

			// Extract Text description first

			List<?> offers = xpaD.selectNodes(racine);
			Iterator<?> iterIn = offers.iterator();

			Element noeudCourant = null;
			noeudCourant = (Element) iterIn.next();
			// Clean text from line breaks (exceptionally for OWLS-TC)
			// result.add(noeudCourant.getText());
			text = noeudCourant.getText().replace('\n', ' ');

			// Extract Inputs

			offers = xpaI.selectNodes(racine);
			iterIn = offers.iterator();

			while (iterIn.hasNext()) {
				noeudCourant = (Element) iterIn.next();
				String url = noeudCourant.getText();
				String localName = url.substring(url.lastIndexOf("#") + 1);
				if (localNames)
					inputs.add(localName);
				else
					inputs.add(url);
			}

			// Extract Outputs

			offers = xpaO.selectNodes(racine);
			iterIn = offers.iterator();

			while (iterIn.hasNext()) {
				noeudCourant = (Element) iterIn.next();
				String url = noeudCourant.getText();
				String localName = url.substring(url.lastIndexOf("#") + 1);
				if (localNames)
					outputs.add(localName);
				else
					outputs.add(url);
			}

		} catch (IOException e) {
			System.out.println("Erreur lors de la lecture du fichier " + e.getMessage());
			e.printStackTrace();
		} catch (JDOMException e) {
			System.out.println("Erreur lors de la construction du document JDOM " + e.getMessage());
			e.printStackTrace();
		}
		return Triple.of(inputs, outputs, text);

	}

	/**
	 * Returns the textual description of the OWLS service situated at
	 * profile:textDescription
	 * 
	 * @param file
	 *            an OWLS description file
	 * @return
	 */
	public static String getTextDescription(File file) {
		return extractFromFile(file, true).getRight();

	}

	/**
	 * Returns the list of Inputs of an OWLS service extracted from its
	 * description file
	 * 
	 * @param file
	 *            an OWLS description file
	 * @return
	 */
	public static List<String> getInputs(File file, Boolean localNames) {
		return extractFromFile(file, localNames).getLeft();
	}

	/**
	 * Returns the list of Outputs of an OWLS service extracted from its
	 * description file
	 * 
	 * @param file
	 *            an OWLS description file
	 * @return
	 * 
	 */
	public static List<String> getOutputs(File file, Boolean localNames) {
		return extractFromFile(file, localNames).getMiddle();
	}

	/**
	 * Returns a {@link Service} representing an OWLS service and sets the name,
	 * text, inputs, outputs, inputsURIs, outputsURIs
	 * 
	 * @param owlsFile
	 *            an OWLS description file
	 * @return {@link Service}
	 */
	public static Service getService(File owlsFile) {
		Triple<List<String>, List<String>, String> t = extractFromFile(owlsFile, false);
		return new Service(owlsFile.getName(), t.getRight(),
				t.getLeft().stream().map(s -> s.substring(Util.splitNamespaceXML(s))).collect(Collectors.toList()),
				t.getMiddle().stream().map(s -> s.substring(Util.splitNamespaceXML(s))).collect(Collectors.toList()),
				t.getLeft(), t.getMiddle());
	}

	/**
	 * Creates and returns a pair (Inputs, Outputs) containing the IO elements
	 * of an OWLS service
	 * 
	 * @param owlsFile
	 *            an OWLS description file
	 * @return {@link Triple}
	 */
	@Deprecated
	public static Pair<List<String>, List<String>> getServiceIO(File owlsFile, Boolean localnames) {
		return Pair.of(getInputs(owlsFile, localnames), getOutputs(owlsFile, localnames));
	}

	@Override
	public String toString() {
		return OWLSTCServices.toString();
	}

}
