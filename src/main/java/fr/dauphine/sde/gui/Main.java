package fr.dauphine.sde.gui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Properties;

import fr.dauphine.sde.model.Params;
import fr.dauphine.sde.relations.RelationsExtractor;
import fr.dauphine.sde.gui.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * The main GUI class, nothing special, just a JavaFX Application class for
 * running the application.
 * 
 * @author Mohamed LAmine Mouhoub
 *
 */
public class Main extends Application {

	@Override
	public void start(Stage primaryStage) {

		try {
			loadParamsFromProperties();
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
			Parent root = fxmlLoader.load();
			Scene scene = new Scene(root, 800, 600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			Screen screen = Screen.getPrimary();
			Rectangle2D bounds = screen.getVisualBounds();

			primaryStage.setX(bounds.getMinX());
			primaryStage.setY(bounds.getMinY());
			primaryStage.setWidth(bounds.getWidth());
			primaryStage.setHeight(bounds.getHeight());

			primaryStage.setScene(scene);
			primaryStage.show();
			Controller ctrl = (Controller) fxmlLoader.getController();
			ctrl.extractor = new RelationsExtractor();
		} catch (Exception e) {
			new ExceptionAlert(e, "Initialization error",
					"Failed to initialize the program. see the log for error details");
			e.printStackTrace();
			primaryStage.close();
		}
	}

	/**
	 * This method aims to set the resource folder location at runtime. If the
	 * application is ran from an executable jar, then the resources folder will
	 * have a different URI scheme (file:// or jar:file:// or /...).
	 * 
	 * Therefore, It is possible to set any relative or absolute path for the
	 * resources in the config.properties
	 * 
	 * @param location
	 * @return
	 */
	private String resourceLocationAtRuntime(String location) {
		// if the application is ran from within a JAR
		if (Main.class.getResource("Main.class").toString().startsWith("jar")) {
			try {
				String jarLocation = getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
				jarLocation = jarLocation.substring(0, jarLocation.lastIndexOf("/target/")) + "/target/classes/"
						+ location;
				return jarLocation;
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}

		} // if the application is ran from Eclipse or java Main.class and the
			// resource path is relative
		else if (location.startsWith("/fr/dauphine/sde/")) {
			return getClass().getResource(location).getFile();
		}
		// if the resource path is absolute (a local folder set manually in the
		// config.properties)
		return location;
	}

	/**
	 * Method to load some important parameters from the config.properties file
	 * (at fr/dauphine/sde/config/properties)
	 */
	private void loadParamsFromProperties() {
		Properties prop = new Properties();
		InputStream input = null;

		try {
			// A JUnit tests if it is present, so it is definitely there
			input = getClass().getResourceAsStream("/fr/dauphine/sde/config.properties");

			// load a properties file from class path, inside static method
			prop.load(input);

			// Word2Vec API endpoint URL
			if (!prop.getProperty("WORD2VEC_API_URL").isEmpty()) {
				Params.WORD2VEC_API_URL = prop.getProperty("WORD2VEC_API_URL");
			}
			// Ontologies SPARQL endpoint URL
			if (!prop.getProperty("ONTOLOGY_ENDPOINT").isEmpty()) {
				Params.ONTOLOGY_ENDPOINT = prop.getProperty("ONTOLOGY_ENDPOINT");
			}
			// OWLS-TCv4 directory URL, it can be absolute or relative
			if (!prop.getProperty("OWLSTC_DIRECTORY").isEmpty()) {
				Params.OWLSTC_DIRECTORY = resourceLocationAtRuntime(prop.getProperty("OWLSTC_DIRECTORY"));
			}
			// OWLS-TCv4 preannotated text descriptions file path, no need to to
			// customize it for the moment, already comes with the jar
			if (!prop.getProperty("OWLSTC_ANNOTATIONS_PATH").isEmpty()) {
				Params.OWLSTC_ANNOTATIONS_PATH = prop.getProperty("OWLSTC_ANNOTATIONS_PATH");
			}

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

	public static void main(String[] args) {
		launch(args);
	}
}
