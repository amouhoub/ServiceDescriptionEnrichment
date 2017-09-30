package fr.dauphine.sde.gui.threads;

import fr.dauphine.sde.gui.Controller;
import fr.dauphine.sde.gui.ExceptionAlert;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * A JavaFX Service to extract I/O relations from both text and ontologies for
 * all the Semantic Web Services in the loaded repository
 * 
 * @author Mohamed LAmine Mouhoub
 *
 */
public class ExtractionForAllService extends Service<Void> {

	Controller controller;

	public ExtractionForAllService(Controller controller) {
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
						return null;
					}
					updateMessage("[" + i + "/" + size + "] Processing " + serviceName);
					controller.extractor.extractOntologyRelations(controller.serviceList.get(serviceName));
					controller.extractor.recognizeIOinText(controller.serviceList.get(serviceName));
					controller.extractor.extractTextRelations(controller.serviceList.get(serviceName));
					e = System.currentTimeMillis();
					controller.serviceList.get(serviceName).execTimeExtr = e - s;
					i++;
					updateProgress(i, size);
				}
				end = System.currentTimeMillis();

				return null;
			}

			@Override
			protected void succeeded() {
				super.succeeded();
				updateMessage("Processed relations extraction for " + size + " services in " + (end - start) / 1000
						+ " seconds");
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						controller.piServiceList.setVisible(false);
						controller.lvServices.setDisable(false);
						controller.btnRunExperiments.setDisable(false);
						controller.btnEvaluateRelsForAll.setDisable(false);
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
				updateMessage("Failed at [" + i + "/" + size
						+ "]to extract relations for all services . see log for error details");
				System.err.println("++++EXTRACTION FOR ALL ERROR");
				getException().printStackTrace(System.err);
				new ExceptionAlert(getException(), "I/O, Ontology, or Text relations extraction error",
						"Failed to extract either the I/O, the ontology or the text relations. see log for error details");
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
