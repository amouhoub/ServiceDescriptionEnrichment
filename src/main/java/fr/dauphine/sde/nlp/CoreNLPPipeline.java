/**
 * 
 */
package fr.dauphine.sde.nlp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import fr.dauphine.sde.model.Params;
import fr.dauphine.sde.model.extraction.OWLSExtractor;

/**
 * This class contains the required methods to preprocess and annotate the OWLS
 * text descriptions. It contains a HashMap containing serializable annotations
 * of OWLS-TC services.
 * 
 * @author Mohamed LAmine Mouhoub
 *
 */
public class CoreNLPPipeline {

	/**
	 * a HashMap containing all the annotations for all OWLS-TC services. The
	 * <String,...> key represents the filename whereas the <...,Annotation>
	 * value contains the pre-processed annotations through the pipeline.
	 */
	public HashMap<String, Annotation> OWLSTC;

	private StanfordCoreNLP pipeline;

	/**
	 * Default constructor that initializes the pipeline with
	 * {@link #defaultPipeline()}
	 */
	public CoreNLPPipeline() {
		// OWLSTC = new HashMap<>();
		pipeline = defaultPipeline();
		// OWLSTC = fstload(OWLSTC_ANNOTATIONS_PATH);
	}

	/**
	 * Constructor that loads the pre-processed OWLS-TC annotations from their
	 * default location declared in {@link #OWLSTC_ANNOTATIONS_PATH} If no
	 * annotations are found then do a cold-start
	 */
	public CoreNLPPipeline(boolean owlsTC) {
		if (owlsTC) {
			// pipeline = defaultPipeline();
			// if(Params.OWLSTC_ANNOTATIONS_PATH)
			OWLSTC = fstload(Params.OWLSTC_ANNOTATIONS_PATH);
			if (OWLSTC == null) {
				pipeline = defaultPipeline();
			}
		}
	}

	/**
	 * Loads the pre-processed annotations from the given file without
	 * initializing the pipeline
	 * 
	 * @param loadFile
	 *            path to annotations file
	 */
	public CoreNLPPipeline(String loadFile) {
		// pipeline = defaultPipeline();
		OWLSTC = fstload(loadFile);
	}

	/**
	 * creates and returns a StanfordCoreNLP object, with POS tagging,
	 * lemmatization, NER, parsing, and coreference resolution
	 * 
	 * @return a default pipeline
	 */
	private StanfordCoreNLP defaultPipeline() {
		//
		System.out.println("Initializing default CoreNLP pipeline");
		long startTime = System.nanoTime();
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		long endTime = System.nanoTime();
		System.out.println("Initialization complete in " + (endTime - startTime) / 1000000 + " Milliseconds");
		return pipeline;
	}

	/**
	 * This method iterates over all the OWLS-TC files in the given directory
	 * and annotates their text descriptions. It returns a HashMap linking each
	 * file name to its CoreNLP annotation.
	 * 
	 * @param owlstcDirectory
	 *            path to the OWLS-TC services collection.
	 * @return
	 */
	private HashMap<String, Annotation> processOWLSTC(String owlstcDirectory) {
		System.out.println("Processing OWLS-TC through the default pipeline");
		long startTime = System.nanoTime();
		HashMap<String, Annotation> result = new HashMap<>();
		File directory = new File(owlstcDirectory);
		Collection<File> files = FileUtils.listFiles(directory, OWLSExtractor.EXTENSIONS, false);
		for (File file : files) {
			Annotation current = new Annotation(OWLSExtractor.getTextDescription(file));
			pipeline.annotate(current);
			result.put(file.getName(), current);
			System.out.println("File annotated : " + file.getName());
		}
		long endTime = System.nanoTime();
		System.out.println("Processing complete in " + (endTime - startTime) / 1000000 + " Milliseconds");
		return result;
	}

	/**
	 * This method runs the {@link processOWLSTC(String)} method to annotate the
	 * OWLS-TC collection and saves the annotations HashMap in a file at the
	 * given path It uses the Fast Serialization tool by Ruediger Moeller
	 * 
	 * @param path
	 *            path to save annotations file
	 * @see {@link http://ruedigermoeller.github.io/fast-serialization/}
	 */
	private void fstRunAndSave(String savePath) {
		OWLSTC = processOWLSTC(Params.OWLSTC_DIRECTORY);
		System.out.println("Saving OWLS-TC annotations");
		long startTime = System.nanoTime();
		try (BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(savePath));) {
			System.out.println("FST: Saving OWLS-TC annotations");
			FSTObjectOutput out = new FSTObjectOutput(bout);
			out.writeObject(OWLSTC, HashMap.class);
			out.close();
			// fstWrite(bout, OWLSTC);
			System.out.println("FST: Saving complete");
		} catch (IOException e) {
			System.err.println("FST: COULD NOT SAVE ANNOTATIONS: ");
			e.printStackTrace();
		}
		long endTime = System.nanoTime();
		System.out.println("FST: Saving complete in " + (endTime - startTime) / 1000000 + " Milliseconds");
	}

	/**
	 * This method loads an annotations HashMap from the file in the given path.
	 * It uses the Fast Serialization Tool by Ruediger Moeller
	 * 
	 * @param path
	 *            path to the annotations file
	 * 
	 * @see {@link http://ruedigermoeller.github.io/fast-serialization/}
	 *
	 */
	@SuppressWarnings("unchecked")
	private HashMap<String, Annotation> fstload(String path) {
		long startTime = System.nanoTime();
		System.out.println("FST: Loading OWLS-TC annotations");
		System.out.println("---" + getClass().getResource(Params.OWLSTC_ANNOTATIONS_PATH).toString());
		HashMap<String, Annotation> loadedAnnotations = null;
		// new FileInputStream(path)
		try (FSTObjectInput in = new FSTObjectInput(new BufferedInputStream(getClass().getResourceAsStream(path)));) {
			loadedAnnotations = (HashMap<String, Annotation>) in.readObject(HashMap.class);
			in.close();
		} catch (Exception e) {
			System.err.println("FST: COULD NOT LOAD ANNOTATIONS: ");
			e.printStackTrace();
		}
		long endTime = System.nanoTime();
		System.out.println("FST: Loading complete in " + (endTime - startTime) / 1000000 + " Milliseconds");
		return loadedAnnotations;
	}

	/**
	 * The main pre-processes the OWLS-TCv4 service collection to annotation
	 * textural descriptions of services to avoid running the system on a
	 * cold(slow) start. The annotations are serialized using a Fast
	 * Serialization Tool for a fast deserialization (90% faster)
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		CoreNLPPipeline nlp = new CoreNLPPipeline();
		System.out.println(nlp.getClass().getResource(Params.OWLSTC_ANNOTATIONS_PATH).getFile());
		System.out.println(nlp.getClass().getResource(Params.OWLSTC_DIRECTORY).getFile());
		nlp.fstRunAndSave(nlp.getClass().getResource(Params.OWLSTC_ANNOTATIONS_PATH).getFile());
		nlp.fstload(nlp.getClass().getResource(Params.OWLSTC_ANNOTATIONS_PATH).getFile());
	}

}
