package fr.dauphine.sde.nlp;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Test;

import fr.dauphine.sde.gui.Main;
import fr.dauphine.sde.model.Params;

public class VerifyResourcePresence {

	/**
	 * Verify if the OWLS-TCv4 folder is present
	 */
	@Test
	public void verifyOWLSTCDirectory() {
		assertNotNull("OWLS-TC Directory not found", getClass().getResource(Params.OWLSTC_DIRECTORY).getFile());
	}

	/**
	 * Verify if the annotations file is present
	 */
	@Test
	public void verifyOWLSTCAnnotations() {
		assertNotNull("Annotations file not found", getClass().getResource(Params.OWLSTC_ANNOTATIONS_PATH).getFile());
	}

	/**
	 * Verify the existence and integrity of the config.properties file . It is
	 * not mandatory to have it filled for the moment, properties can be empty,
	 * but their keys should be all found
	 */
	@Test
	public void verifyPropertiesFile() {
		assertNotNull("confi.properties file not found", getClass().getResource("../config.properties").getFile());
		Properties prop = new Properties();
		InputStream input = null;

		try {
			// A JUnit tests if it is present, so it is definitely there
			input = Main.class.getResourceAsStream("../config.properties");

			// load a properties file from class path, inside static method
			prop.load(input);

			// verify if all the properties' keys are present (i.e if the file
			// is valid)
			assertNotNull("confi.properties not valid", prop.getProperty("WORD2VEC_API_URL"));
			assertNotNull("confi.properties not valid", prop.getProperty("ONTOLOGY_ENDPOINT"));
			assertNotNull("confi.properties not valid", prop.getProperty("OWLSTC_DIRECTORY"));
			assertNotNull("confi.properties not valid", prop.getProperty("OWLSTC_ANNOTATIONS_PATH"));

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
