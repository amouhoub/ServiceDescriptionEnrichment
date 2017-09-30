package fr.dauphine.sde.gui;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.fxmisc.richtext.InlineCssTextArea;

import fr.dauphine.sde.gui.threads.EvaluationForAllService;
import fr.dauphine.sde.gui.threads.EvaluationService;
import fr.dauphine.sde.gui.threads.ExperimentsService;
import fr.dauphine.sde.gui.threads.ExtractionForAllService;
import fr.dauphine.sde.gui.threads.ExtractionIOTextService;
import fr.dauphine.sde.gui.threads.ExtractionOntoService;
import fr.dauphine.sde.gui.utils.UIUtils;
import fr.dauphine.sde.model.Params;
import fr.dauphine.sde.model.Service;
import fr.dauphine.sde.model.ServiceList;
import fr.dauphine.sde.model.extraction.OWLSExtractor;
import fr.dauphine.sde.nlp.similarity.FuzzySimilarity;
import fr.dauphine.sde.nlp.similarity.Word2Vec;
import fr.dauphine.sde.nlp.similarity.WordSimilarity;
import fr.dauphine.sde.relations.RelationsExtractor;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * A JavaFX controller for the Desktop Application
 * 
 * @author Mohamed LAmine Mouhoub
 *
 */
public class Controller implements Initializable {

	@FXML
	private Button btnLoadServices;

	@FXML
	private Button btnExtractRels;

	@FXML
	private Button btnEvaluateRels;

	@FXML
	private Button btnExtractRelsForAll;

	@FXML
	public Button btnEvaluateRelsForAll;

	@FXML
	public Button btnRunExperiments;

	@FXML
	public ListView<String> lvServices;

	@FXML
	public ListView<String> lvInputs;

	@FXML
	public ListView<String> lvOutputs;

	@FXML
	private ListView<String> lvExtrInputs;

	@FXML
	private ListView<String> lvExtrOutputs;

	@FXML
	public ListView<String> lvOntoRelations;

	@FXML
	public ListView<String> lvTextRelations;

	@FXML
	private Spinner<Integer> spnMaxHierarchy;

	@FXML
	private Spinner<Integer> spnMaxDistance;

	@FXML
	private Slider sldrThreshold;

	@FXML
	private TextField tfThreshold;

	@FXML
	private ComboBox<String> cmbSparqlEndpoint;

	@FXML
	private ChoiceBox<WordSimilarity> chbTexSimtMethod;

	@FXML
	private TextField tfWord2vecAPIURL;

	@FXML
	private TextField tfOwlstcPath;

	@FXML
	private TextField tfSearch;

	@FXML
	private Label lblServiceNameLbl;

	@FXML
	public Label lblLog;

	@FXML
	public Label lblOntoInfo;

	@FXML
	public Label lblTextInfo;

	@FXML
	private Label lblServiceListCount;

	@FXML
	private ToggleGroup propPattern;

	@FXML
	private RadioButton rdbDomRngPattern;

	@FXML
	private RadioButton rdbOwlstcPattern;

	@FXML
	private CheckBox ckbFilterDCG;

	@FXML
	private CheckBox ckbFilterSameOntology;

	@FXML
	private CheckBox ckbFilterSubClass;

	@FXML
	private CheckBox ckbSLFiltersameOnto;

	@FXML
	private CheckBox chkbSLFilterAtLeast;

	@FXML
	public ProgressIndicator piTextIO = new ProgressIndicator();

	@FXML
	public ProgressIndicator piOnto = new ProgressIndicator();

	@FXML
	public ProgressIndicator piServiceList = new ProgressIndicator();

	@FXML
	private InlineCssTextArea rtfxText;

	public RelationsExtractor extractor;

	public FilteredList<String> filteredServiceList;

	ObservableList<String> serviceNamesList;

	ObservableList<WordSimilarity> dSimilarityMethod;

	ObservableList<String> dInputsList;
	ObservableList<String> dOutputsList;
	public ObservableList<String> dExtrInputsList;
	public ObservableList<String> dExtrOutputsList;

	ObservableList<String> dOntoRelsList;
	ObservableList<String> dTextRelsList;

	ObservableList<String> dOntoEndpointsList;
	ObservableList<String> dSimilarityMethodsList;

	// ExecutorService executor;
	ExtractionOntoService extractorOnto;
	ExtractionIOTextService extractorIOText;
	ExtractionForAllService extractorForAll;
	EvaluationService evaluator;
	EvaluationForAllService evaluatorForAll;
	ExperimentsService experimentor;

	public ServiceList serviceList;
	public Service currService;

	String OWLSTC_DIR;

	@FXML
	private void onSetFalseInClicked(MouseEvent event) {
		if (!currService.extractedInputsEval.put(lvExtrInputs.getSelectionModel().getSelectedItem(), true))
			lblLog.setText("ERROR, couldn't attribute value");
	}

	@FXML
	private void onSetFalseOutClicked(MouseEvent event) {
		if (!currService.extractedOutputsEval.put(lvExtrOutputs.getSelectionModel().getSelectedItem(), true))
			lblLog.setText("ERROR, couldn't attribute value");
	}

	@FXML
	private void onSetFalseRelClicked(MouseEvent event) {
		if (!currService.ontoRelationsEval.put(lvOntoRelations.getSelectionModel().getSelectedItem(), true))
			lblLog.setText("ERROR, couldn't attribute value");
	}

	@FXML
	public void loadServiceList() {
		serviceList = new ServiceList();
		System.out.println("Loading files from directory " + tfOwlstcPath.getText());
		serviceNamesList.clear();
		File directory = new File(tfOwlstcPath.getText());
		if (directory.isDirectory()) {
			OWLSTC_DIR = tfOwlstcPath.getText();
			Collection<File> files = FileUtils.listFiles(directory, OWLSExtractor.EXTENSIONS, false);
			System.out.println("loaded " + files.size() + " services successfully");
			for (File file : files) {
				serviceNamesList.add(file.getName());
				// Load Basic info as well
				serviceList.add(OWLSExtractor.getService(file));
			}
		} else {
			System.out.println("directory is not a directory : " + directory.getAbsolutePath());
		}
		lblServiceListCount.setText("#" + serviceNamesList.size());
	}

	public void updateServiceList() {
		lblServiceListCount.setText("#" + filteredServiceList.size());
	}

	public void onCheckedServiceFilter() {
		boolean sameOnto = ckbSLFiltersameOnto.isSelected(), atLeast = chkbSLFilterAtLeast.isSelected();
		filteredServiceList.setPredicate(serviceName -> {
			Service s = serviceList.contains(serviceName) ? serviceList.get(serviceName)
					: OWLSExtractor.getService(new File(OWLSTC_DIR + serviceName));
			if (sameOnto) {
				if (atLeast)
					return UIUtils.isIntraOntology(s) && UIUtils.isAtLeastIO(s);
				else
					return UIUtils.isIntraOntology(s);
			} else if (atLeast)
				return UIUtils.isAtLeastIO(s);
			return true;
		});
		updateServiceList();
	}

	public void filterEvaluatedServices() {
		filteredServiceList.setPredicate(serviceName -> serviceList.UsableServices.contains(serviceName));
		updateServiceList();
	}

	@FXML
	public void loadService() {
		String serviceName = lvServices.getSelectionModel().getSelectedItem();
		if (serviceName != null) {

			if (serviceList.contains(serviceName)) {
				currService = serviceList.get(serviceName);
			} else {
				currService = OWLSExtractor.getService(new File(OWLSTC_DIR + serviceName));
				serviceList.add(currService);
			}
			showBasicServiceInfo();
		}
	}

	public void showBasicServiceInfo() {
		rtfxText.clear();
		rtfxText.insertText(0, currService.getText());
		lblServiceNameLbl.setText(currService.getName());
		dInputsList.clear();
		dOutputsList.clear();
		dOntoRelsList.clear();
		dTextRelsList.clear();
		dExtrInputsList.clear();
		dExtrOutputsList.clear();

		dInputsList.addAll(currService.getInputsURIs());
		dOutputsList.addAll(currService.getOutputsURIs());

		UIUtils.resetMatchedCellsStyle(lvInputs);
		UIUtils.resetMatchedCellsStyle(lvOutputs);

		lblOntoInfo.setText("");
		lblTextInfo.setText("");

		showExtractedServiceInfo();
	}

	public void showExtractedServiceInfo() {
		dExtrInputsList.clear();
		dExtrOutputsList.clear();
		dOntoRelsList.clear();
		dTextRelsList.clear();
		if (currService.getExtractedInputs() != null)
			dExtrInputsList.addAll(currService.getExtractedInputs());
		if (currService.getExtractedOutputs() != null)
			dExtrOutputsList.addAll(currService.getExtractedOutputs());
		if (currService.getOntoRelations() != null)
			dOntoRelsList.addAll(currService.getOntoRelations());
		if (currService.getTextRelations() != null)
			dTextRelsList.addAll(currService.getTextRelations());

		lvExtrInputs.setItems(dExtrInputsList);
		lvExtrOutputs.setItems(dExtrOutputsList);
		lvOntoRelations.setItems(dOntoRelsList);
		lvTextRelations.setItems(dTextRelsList);

		for (String item : dExtrInputsList) {
			if (dInputsList.contains(item)) {
			} else {

			}
		}
	}

	public void highlightIOOnClick(ListView<String> list) {
		rtfxText.clearStyle(0, rtfxText.getText().length() - 1);

		String io = list.getSelectionModel().getSelectedItem();
		if (list.equals(lvExtrInputs)) {
			if (currService.getInMatches() != null && currService.getInMatches().get(io) != null) {
				if (list.equals(lvExtrInputs))
					for (Triple<String, Integer, Integer> token : currService.getInMatches().get(io)) {
						int strtIndex = currService.getText().indexOf(token.getLeft(),
								UIUtils.indexUntilToken(token.getMiddle(), token.getRight(), currService));
						int endIndex = strtIndex + token.getLeft().length();
						rtfxText.setStyle(strtIndex, endIndex, "-rtfx-background-color:lightblue");
					}

			}
		} else if (list.equals(lvExtrOutputs)) {
			if (currService.getOutMatches() != null && currService.getOutMatches().get(io) != null) {
				for (Triple<String, Integer, Integer> token : currService.getOutMatches().get(io)) {
					int strtIndex = currService.getText().indexOf(token.getLeft(),
							UIUtils.indexUntilToken(token.getMiddle(), token.getRight(), currService));
					int endIndex = strtIndex + token.getLeft().length();
					rtfxText.setStyle(strtIndex, endIndex, "-rtfx-background-color:lightcoral");
				}
			}
		}
	}

	public void updateServiceIO() {
		dExtrInputsList.clear();
		dExtrOutputsList.clear();
		if (currService.getExtractedInputs() != null)
			dExtrInputsList.addAll(currService.getExtractedInputs());
		if (currService.getExtractedOutputs() != null)
			dExtrOutputsList.addAll(currService.getExtractedOutputs());
	}

	public void updateServiceText() {
		dTextRelsList.clear();
		// Take all cells of super matrix (all lists of relations)
		// For each one, convert the relations inner matrix into a list of
		// relations expressed as strings.
		currService.getTextRelationsMatrix()
				.forEach(in -> in.forEach(row -> row.stream().filter(rel -> !rel.isEmpty()).collect(Collectors.toList())
						.forEach(rel -> dTextRelsList
								.add(rel.stream().filter(s -> s != null).collect(Collectors.toList()).toString()))));
	}

	public void updateServiceOnto() {
		dOntoRelsList.clear();
		currService.getOntoRelationsMatrix()
				.forEach(in -> in.forEach(row -> row.stream().filter(rel -> !rel.isEmpty()).collect(Collectors.toList())
						.forEach(rel -> dOntoRelsList.add(rel.stream().filter(n -> n != null).map(n -> n.getLocalName())
								.collect(Collectors.toList()).toString()))));
	}

	@FXML
	public void runExperiments() {
		updateParams();
		experimentor.restart();
	}

	@FXML
	public void extractRelations() {
		updateParams();

		piTextIO.setVisible(true);
		piOnto.setVisible(true);
		lvOntoRelations.setDisable(true);
		lvTextRelations.setDisable(true);

		piTextIO.progressProperty().bind(extractorIOText.progressProperty());
		piOnto.progressProperty().bind(extractorOnto.progressProperty());

		extractorOnto.restart();
		extractorIOText.restart();
	}

	@FXML
	public void extractRelationsForAll() {

		if (extractorForAll.isRunning()) {
			extractorForAll.cancel();
		}

		updateParams();

		piServiceList.setVisible(true);
		lvServices.setDisable(true);

		piServiceList.progressProperty().bind(extractorForAll.progressProperty());
		lblLog.textProperty().bind(extractorForAll.messageProperty());

		extractorForAll.restart();
	}

	@FXML
	public void evaluateRelations() {
		updateParams();
		lblLog.textProperty().bind(evaluator.messageProperty());
		evaluator.restart();
	}

	@FXML
	public void evaluateRelationsForAll() {

		if (evaluatorForAll.isRunning()) {
			evaluatorForAll.cancel();
			lblLog.setText("Extraction canceled ..." + evaluatorForAll.getState());
		}

		updateParams();

		piServiceList.setVisible(true);
		lvServices.setDisable(true);

		piServiceList.progressProperty().bind(evaluatorForAll.progressProperty());
		lblLog.textProperty().bind(evaluatorForAll.messageProperty());

		evaluatorForAll.restart();
	}

	public void updateParams() {
		Params.WORD2VEC_API_URL = tfWord2vecAPIURL.getText();
		Params.SIMILARITY_THRESHOLD = Double.parseDouble(tfThreshold.getText());
		Params.similarityMethod = chbTexSimtMethod.getSelectionModel().getSelectedItem();
		chbTexSimtMethod.getSelectionModel().getSelectedItem().setThreshold(Params.SIMILARITY_THRESHOLD);

		Params.ONTOLOGY_ENDPOINT = cmbSparqlEndpoint.getSelectionModel().getSelectedItem();
		Params.OWLSTC_DIRECTORY = OWLSTC_DIR;
		Params.maxHierarchy = spnMaxHierarchy.getValue();
		Params.maxDistance = spnMaxDistance.getValue();
		Params.FilterDCG = ckbFilterDCG.isSelected();
		Params.FilterSameOntology = ckbFilterSameOntology.isSelected();
		Params.FilterSubClass = ckbFilterSubClass.isSelected();
		Params.OWLS_TC_Pattern = (boolean) propPattern.getSelectedToggle().getUserData();
	}

	@FXML
	public void onServiceListKeyPressed(KeyEvent event) {
		switch (event.getCode()) {
		case SPACE:
			extractRelations();
			break;
		case ENTER:
			extractRelations();
			break;
		default:
			break;
		}
	}

	@FXML
	public void onServiceListClicked(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY) {
			if (event.getClickCount() == 2) {
				extractRelations();
			} else {
				loadService();
			}
		}
	}

	@FXML
	public void onOntoRelsListKeyTyped(KeyEvent event) {
		switch (event.getCode()) {
		case SPACE:
			evaluateRelations();
			break;
		case ENTER:
			evaluateRelations();
			break;
		default:
			break;
		}
	}

	@FXML
	public void onOntoRelsListClicked(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY) {
			if (event.getClickCount() == 2) {
				evaluateRelations();
			}
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		serviceList = new ServiceList();

		// Ajust the service description TextArea size for a better display
		// rtfxText.textProperty().addListener(new ChangeListener<String>() {
		// @Override
		// public void changed(ObservableValue<? extends String> ob, String o,
		// String n) {
		// textContainer.setPrefColumnCount(textContainer.getText().length()
		// + 1);
		// }
		// });

		SpinnerValueFactory<Integer> maxHValFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 0);
		SpinnerValueFactory<Integer> maxDValFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 0);
		spnMaxHierarchy.setValueFactory(maxHValFactory);
		spnMaxDistance.setValueFactory(maxDValFactory);

		dSimilarityMethod = FXCollections.observableArrayList(new Word2Vec(), new FuzzySimilarity());
		chbTexSimtMethod.setItems(dSimilarityMethod);
		chbTexSimtMethod.getSelectionModel().select(0);

		dInputsList = FXCollections.observableArrayList();
		dOutputsList = FXCollections.observableArrayList();
		dExtrInputsList = FXCollections.observableArrayList();
		dExtrOutputsList = FXCollections.observableArrayList();
		dOntoRelsList = FXCollections.observableArrayList();
		dTextRelsList = FXCollections.observableArrayList();

		serviceNamesList = FXCollections.observableArrayList();

		lvInputs.setItems(dInputsList);
		lvOutputs.setItems(dOutputsList);
		lvExtrInputs.setItems(dExtrInputsList);
		lvExtrOutputs.setItems(dExtrOutputsList);
		lvTextRelations.setItems(dTextRelsList);
		lvOntoRelations.setItems(dOntoRelsList);

		dSimilarityMethodsList = FXCollections.observableArrayList();
		dOntoEndpointsList = FXCollections.observableArrayList("http://localhost:3030/ds/query",
				"http://dbpedia.org/sparql");
		cmbSparqlEndpoint.setItems(dOntoEndpointsList);
		cmbSparqlEndpoint.getSelectionModel().select(0);
		
		tfWord2vecAPIURL.setText(Params.WORD2VEC_API_URL);

		sldrThreshold.valueProperty()
				.addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
					tfThreshold.setText(String.valueOf(new_val.doubleValue()));
				});

		tfThreshold.textProperty().addListener(obs -> {
			sldrThreshold.setValue(tfThreshold.getText() == null ? Params.SIMILARITY_THRESHOLD
					: Double.parseDouble(tfThreshold.getText()));
		});

		propPattern = new ToggleGroup();
		rdbDomRngPattern.setToggleGroup(propPattern);
		rdbOwlstcPattern.setToggleGroup(propPattern);
		rdbDomRngPattern.setUserData(false);
		rdbOwlstcPattern.setUserData(true);

		tfOwlstcPath.setText(getClass().getResource(Params.OWLSTC_DIRECTORY).getFile());
		loadServiceList();
		filteredServiceList = new FilteredList<>(serviceNamesList, s -> true);
		lvServices.setItems(filteredServiceList);

		//extractor = new RelationsExtractor();

		extractorOnto = new ExtractionOntoService(this);
		extractorIOText = new ExtractionIOTextService(this);
		extractorForAll = new ExtractionForAllService(this);
		evaluator = new EvaluationService(this);
		evaluatorForAll = new EvaluationForAllService(this);
		experimentor = new ExperimentsService(this);

		lvServices.getSelectionModel().selectedItemProperty()
				.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
					loadService();
				});

		lvExtrInputs.getSelectionModel().selectedItemProperty()
				.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
					highlightIOOnClick(lvExtrInputs);
				});

		lvExtrOutputs.getSelectionModel().selectedItemProperty()
				.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
					highlightIOOnClick(lvExtrOutputs);
				});

		tfSearch.textProperty().addListener(obs -> {
			String filter = tfSearch.getText();
			if (filter == null || filter.length() == 0) {
				filteredServiceList.setPredicate(s -> true);
			} else {
				filteredServiceList.setPredicate(s -> s.contains(filter));
			}
			lblServiceListCount.setText("#" + filteredServiceList.size());
		});

		lvExtrInputs.setOnMouseClicked(e -> {
			if (currService != null && lvExtrInputs.getSelectionModel().getSelectedIndex() != -1)
				highlightIOOnClick(lvExtrInputs);
		});

		lvExtrOutputs.setOnMouseClicked(e -> {
			if (currService != null && lvExtrOutputs.getSelectionModel().getSelectedIndex() != -1)
				highlightIOOnClick(lvExtrOutputs);
		});

		lvOntoRelations.getSelectionModel().selectedItemProperty()
				.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
					UIUtils.colorRelsCells(lvTextRelations, lvOntoRelations.getSelectionModel().getSelectedItem(),
							currService);
				});

	}

}
