package fr.dauphine.sde.gui.threads;

import fr.dauphine.sde.gui.Controller;
import fr.dauphine.sde.gui.ExceptionAlert;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * A JavaFX Service to extract I/O relations from ontologies for a single
 * service
 * 
 * @author Mohamed LAmine Mouhoub
 *
 */
public class ExtractionOntoService extends Service<Void> {

	Controller controller;

	public ExtractionOntoService(Controller controller) {
		this.controller = controller;
	}

	@Override
	protected Task<Void> createTask() {
		return new Task<Void>() {
			long start, end;

			@Override
			public Void call() {
				if (controller.currService == null) {
					updateMessage("Service not found, Please select a service");
				}
				start = System.currentTimeMillis();
				controller.extractor.extractOntologyRelations(controller.currService);
				end = System.currentTimeMillis();
				updateProgress(90, 100);
				return null;
			}

			@Override
			protected void succeeded() {
				super.succeeded();

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						controller.updateServiceOnto();
						controller.piOnto.setVisible(false);
						controller.lvOntoRelations.setDisable(false);
						controller.lblOntoInfo.setText((end - start) + "ms");
					}
				});
			}

			@Override
			protected void failed() {
				super.failed();
				updateMessage("Failed to extract ontology relations. see log for error details");
				System.err.println("++++ONTO EXTRACTION ERROR");
				getException().printStackTrace(System.err);
				new ExceptionAlert(getException(), "Ontology relations extraction error",
						"Failed to extract ontology relations. see log for error details");
			}
		};
	}

}
