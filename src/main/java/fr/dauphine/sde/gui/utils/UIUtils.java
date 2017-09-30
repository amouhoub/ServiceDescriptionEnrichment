package fr.dauphine.sde.gui.utils;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.rdf.model.impl.Util;

import fr.dauphine.sde.model.Params;
import fr.dauphine.sde.model.Service;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;

/**
 * A set of methods to customize the display of the extracted I/O relations
 * (Highlight the matches in green, the missing stuff in red, etc).
 * 
 * @author Mohamed LAmine Mouhoub
 *
 */
public class UIUtils {

	public static boolean isIntraOntology(Service s) {
		// If inputs is empty, outputs shouldn't logically.
		String ontoPrefix = s.getInputsURIs().size() == 0 ? s.getOutputsURIs().get(0) : s.getInputsURIs().get(0);
		ontoPrefix = ontoPrefix.substring(0, Util.splitNamespaceXML(ontoPrefix));
		// System.out.println("prefix=" + ontoPrefix);
		for (String in : s.getInputsURIs()) {
			if (!ontoPrefix.equals(in.substring(0, Util.splitNamespaceXML(in)))) {
				return false;
			}
		}
		for (String out : s.getOutputsURIs()) {
			if (!ontoPrefix.equals(out.substring(0, Util.splitNamespaceXML(out)))) {
				return false;
			}
		}
		return true;
	}

	public static boolean isAtLeastIO(Service service) {
		if (service.getInputs().isEmpty() || service.getOutputs().isEmpty()) {
			return false;
		}
		return true;
	}

	public static int indexUntilToken(int sentenceIndex, int tokenIndex, Service currService) {
		int index = 0;
		for (int i = 0; i < sentenceIndex; i++) {
			index = index + currService.getTokens().get(i).stream().mapToInt(String::length).sum();
		}
		for (int i = 0; i < tokenIndex; i++) {
			index = index + currService.getTokens().get(sentenceIndex).get(i).length();
		}
		return index;
	}

	public static void colorIOCells(ListView<String> listView, List<String> matchingList) {

		listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
			@Override
			public ListCell<String> call(ListView<String> stringListView) {
				return new ListCell<String>() {
					@Override
					protected void updateItem(String s, boolean b) {
						super.updateItem(s, b);
						if (!b) {
							if (matchingList.contains(s.substring(Util.splitNamespaceXML(s)))) {
								// System.out.println("MATCH"+s);
								setText(b ? null : s);
								// setStyle("-fx-background-color:lightgreen");
								// setStyle("-fx-text-fill:lightgreen");
								// setBackground(Background);
							} else {
								setText(b ? null : s);
								// setStyle("-fx-background-color:lightcoral");
								setStyle("-fx-text-fill:lightcoral");
								// System.out.println("NOT"+s);
							}
						} else {
							setText(null);
							setGraphic(null);
							setStyle(null);
						}
					}
				};
			}
		});
	}

	public static void colorRelsCells(ListView<String> listView, String matchingList, Service currService) {

		listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
			@Override
			public ListCell<String> call(ListView<String> stringListView) {
				return new ListCell<String>() {
					@Override
					protected void updateItem(String s, boolean b) {
						super.updateItem(s, b);
						if (!b) {
							if (currService.getRelations() != null
									&& currService.getRelations().containsKey(matchingList)
									&& currService.getRelations().get(matchingList).containsKey(s)) {
								double simValue[] = currService.getRelations().get(matchingList).get(s);
								setTooltip(new Tooltip(Arrays.toString(simValue)));
								// System.out.println("SimValue=" +
								// Arrays.toString(simValue));
								// TODO create a list<Node> of all relations
								if (Arrays.stream(simValue).max().getAsDouble() > Params.SIMILARITY_THRESHOLD) {
									setText(b ? null : s);
									setStyle("-fx-background-color:lightgreen");
									// setStyle("-fx-text-fill:white");
									// setBackground(Background);
								} else {
									setText(b ? null : s);
									setStyle("-fx-background-color:lightcoral");
									// setStyle("-fx-text-fill:white");
								}
							} else {
								setText(b ? null : s);
								setGraphic(null);
								setStyle(null);
							}
						} else {
							setText(null);
							setGraphic(null);
							setStyle(null);
						}
					}
				};
			}
		});
	}

	public static void resetMatchedCellsStyle(ListView<String> listView) {

		listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
			@Override
			public ListCell<String> call(ListView<String> stringListView) {
				return new ListCell<String>() {
					@Override
					protected void updateItem(String s, boolean b) {
						super.updateItem(s, b);
						if (!b) {
							setText(b ? null : s);
							setStyle("-fx-text-fill:black");
						} else {
							setText(null);
							setGraphic(null);
							setStyle(null);
						}
					}
				};
			}
		});
	}

}
