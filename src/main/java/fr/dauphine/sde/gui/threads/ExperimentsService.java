package fr.dauphine.sde.gui.threads;

import fr.dauphine.sde.graph.EvaluationUtils;
import fr.dauphine.sde.gui.Controller;
import fr.dauphine.sde.gui.ExceptionAlert;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * A JavaFX Service to run the experiments of I/O relation extraction and
 * evaluation from the OWLS-TCv4 benchmark. (Ontologies and Services are
 * provided)
 * 
 * @author Mohamed LAmine Mouhoub
 *
 */
public class ExperimentsService extends Service<Void> {

	Controller controller;

	public ExperimentsService(Controller controller) {
		this.controller = controller;
	}

	@Override
	protected Task<Void> createTask() {
		// TODO Auto-generated method stub
		return new Task<Void>() {

			long start, end;
			String result;
			// int i, size;

			@Override
			public Void call() {
				start = System.currentTimeMillis();
				// size = controller.filteredServiceList.size();
				// i = 0;
				if (isCancelled()) {
					updateMessage("Canceled");
					return null;
				}
				updateMessage("Running experiments...");
				result = EvaluationUtils.printExtractedIOStats(controller.serviceList, controller.filteredServiceList);
				end = System.currentTimeMillis();

				return null;
			}

			@Override
			protected void succeeded() {
				super.succeeded();
				updateMessage("Finished experiments successfully in " + (end - start) + "ms");
				controller.filterEvaluatedServices();
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("I/O extraction evaluation");
				alert.setHeaderText("Here are the results of the actual experiments");
				alert.setContentText(result);
				alert.showAndWait();
			}

			@Override
			protected void cancelled() {
				super.cancelled();
				updateMessage("Cancelled experiments!");
			}

			@Override
			protected void failed() {
				super.failed();
				updateMessage("Failed to run experiments. see log for error details");
				System.err.println("-----EVALUATION FOR ALL ERROR");
				getException().printStackTrace(System.err);
				new ExceptionAlert(getException(), "Experiments error",
						"Failed to run experiments on OWLS-TC. see log for error details");
			}
		};
	}

}
