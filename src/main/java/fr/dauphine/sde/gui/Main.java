package fr.dauphine.sde.gui;

import java.io.IOException;
import java.io.InputStream;
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

	private void loadParamsFromProperties() {
		Properties prop = new Properties();
		InputStream input = null;

		try {
			// A JUnit tests if it is present, so it is definitely there
			System.out.println(getClass().getResource("/fr/dauphine/sde/config.properties").getFile());
			input = getClass().getResourceAsStream("/fr/dauphine/sde/config.properties");

			// load a properties file from class path, inside static method
			prop.load(input);

			if (!prop.getProperty("WORD2VEC_API_URL").isEmpty()) {
				Params.WORD2VEC_API_URL = prop.getProperty("WORD2VEC_API_URL");
			}
			if (!prop.getProperty("ONTOLOGY_ENDPOINT").isEmpty()) {
				Params.ONTOLOGY_ENDPOINT = prop.getProperty("ONTOLOGY_ENDPOINT");
			}
			if (!prop.getProperty("OWLSTC_DIRECTORY").isEmpty()) {
				Params.OWLSTC_DIRECTORY = prop.getProperty("OWLSTC_DIRECTORY");
			}
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
