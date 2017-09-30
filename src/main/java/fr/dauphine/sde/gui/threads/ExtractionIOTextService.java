package fr.dauphine.sde.gui.threads;

import fr.dauphine.sde.gui.Controller;
import fr.dauphine.sde.gui.ExceptionAlert;
import fr.dauphine.sde.gui.utils.UIUtils;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * A JavaFX Service to extract I/O relations from text for a single service
 * 
 * @author Mohamed LAmine Mouhoub
 *
 */
public class ExtractionIOTextService extends Service<Void> {

	Controller controller;

	public ExtractionIOTextService(Controller controller) {
		this.controller = controller;
	}

	@Override
	protected Task<Void> createTask() {
		return new Task<Void>() {

			long start, end;

			@Override
			public Void call() {
				start = System.currentTimeMillis();
				controller.extractor.recognizeIOinText(controller.currService);
				updateProgress(50, 100);
				controller.extractor.extractTextRelations(controller.currService);
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
						// For extracted IO
						controller.updateServiceIO();
						UIUtils.colorIOCells(controller.lvInputs, controller.dExtrInputsList);
						UIUtils.colorIOCells(controller.lvOutputs, controller.dExtrOutputsList);

						// For text relations
						controller.piTextIO.setVisible(false);
						controller.lvTextRelations.setDisable(false);
						controller.updateServiceText();
						controller.lblTextInfo.setText((end - start) + "ms");
					}
				});
			}

			@Override
			protected void failed() {
				super.failed();
				updateMessage("Failed to extract IO and text relations. see log for error details");
				System.err.println("+++++IOTEXT EXTRACTION ERROR");
				getException().printStackTrace(System.err);
				new ExceptionAlert(getException(), "I/O or Text relations extraction error",
						"Failed to extract either the I/O or the text relations. see log for error details");
			}
		};
	}

}
