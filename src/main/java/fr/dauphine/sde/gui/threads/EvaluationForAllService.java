package fr.dauphine.sde.gui.threads;

import fr.dauphine.sde.gui.Controller;
import fr.dauphine.sde.gui.ExceptionAlert;
import fr.dauphine.sde.relations.RelationMatching;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * A JavaFX Service to match and evaluate all I/O relations of all loaded
 * semantic web services from a given repository
 * 
 * @author Mohamed LAmine Mouhoub
 *
 */
public class EvaluationForAllService extends Service<Void> {

	Controller controller;

	public EvaluationForAllService(Controller controller) {
		this.controller = controller;
	}

	@Override
	protected Task<Void> createTask() {
		// TODO Auto-generated method stub
		return new Task<Void>() {

			long start, end;
			int i, size;

			@Override
			public Void call() {
				start = System.currentTimeMillis();
				size = controller.filteredServiceList.size();
				i = 0;
				for (String serviceName : controller.filteredServiceList) {
					long s = System.currentTimeMillis(), e;
					if (isCancelled()) {
						updateMessage("Canceled");
						return null;
					}
					updateMessage("[" + i + "/" + size + "] Processing " + serviceName);
					RelationMatching.evaluateRelations(controller.serviceList.get(serviceName));
					e = System.currentTimeMillis();
					controller.serviceList.get(serviceName).execTimeEval = e - s;
					i++;
					updateProgress(i, size);
				}
				end = System.currentTimeMillis();

				return null;
			}

			@Override
			protected void succeeded() {
				super.succeeded();
				updateMessage("Finished successfully the relations evaluation in " + (end - start) + "ms");
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						// controller.updateServiceOnto();
						controller.piServiceList.setVisible(false);
						controller.lvServices.setDisable(false);
						controller.filterEvaluatedServices();
					}
				});
			}

			@Override
			protected void cancelled() {
				super.cancelled();
				updateMessage("Cancelled at [" + i + "/" + size + "]");
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						controller.piServiceList.setVisible(false);
						controller.lvServices.setDisable(false);
					}
				});
			}

			@Override
			protected void failed() {
				super.failed();
				updateMessage("Failed at [" + i + "/" + size + "]to evaluate relations. see log for error details");
				System.err.println("-----EVALUATION FOR ALL ERROR");
				getException().printStackTrace(System.err);
				new ExceptionAlert(getException(), "Relations evaluation error",
						"Failed to evaluate relations. see log for error details");
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						controller.piServiceList.setVisible(false);
						controller.lvServices.setDisable(false);
					}
				});
			}
		};
	}

}
