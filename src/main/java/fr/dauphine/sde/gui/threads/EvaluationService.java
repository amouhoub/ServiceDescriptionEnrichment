package fr.dauphine.sde.gui.threads;

import fr.dauphine.sde.gui.Controller;
import fr.dauphine.sde.gui.ExceptionAlert;
import fr.dauphine.sde.relations.RelationMatching;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * A JavaFX Service to match and evaluate all I/O relations of a single service
 * 
 * @author Mohamed LAmine Mouhoub
 *
 */
public class EvaluationService extends Service<Void> {

	Controller controller;

	public EvaluationService(Controller controller) {
		this.controller = controller;
	}

	@Override
	protected Task<Void> createTask() {
		// TODO Auto-generated method stub
		return new Task<Void>() {
			@Override
			public Void call() {
				long start = System.currentTimeMillis();
				RelationMatching.evaluateRelations(controller.currService);
				long end = System.currentTimeMillis();
				updateMessage("Finished successfully the relations evaluation in " + (end - start) + "ms");
				updateProgress(90, 100);
				return null;
			}

			@Override
			protected void failed() {
				super.failed();
				updateMessage("Failed to evaluate relations. see log for error details");
				System.err.println("-----EVALUATION FOR ALL ERROR");
				getException().printStackTrace(System.err);
				new ExceptionAlert(getException(), "Relations evaluation error",
						"Failed to evaluate relations. see log for error details");

			}
		};
	}

}
