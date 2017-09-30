package fr.dauphine.sde.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.jena.graph.Node;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import edu.stanford.nlp.pipeline.Annotation;

/**
 * A class to represent a service (Semantic Web Service) with all its
 * interesting elements. Used for serialization a service as a JSON object for
 * export purposes
 * 
 * @author Mohamed LAmine Mouhoub
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "Name", "Text", "Inputs", "Outputs", "ExtractedInputs", "ExtractedOutputs", "TextRelations",
		"OntoRelations" })
public class Service {

	@JsonIgnore
	private Annotation annotation;

	@JsonProperty("Name")
	private String name;

	@JsonProperty("Text")
	private String text;

	@JsonProperty("Inputs")
	private List<String> inputs;

	@JsonProperty("Outputs")
	private List<String> outputs;

	@JsonIgnore
	private List<List<String>> tokens;

	@JsonIgnore
	private List<String> inputsURIs;

	@JsonIgnore
	private List<String> outputsURIs;

	@JsonIgnore
	private Map<String, List<Triple<String, Integer, Integer>>> inMatches;

	@JsonIgnore
	private Map<String, List<Triple<String, Integer, Integer>>> outMatches;

	@JsonProperty("ExtractedInputs")
	private List<String> extractedInputs;

	@JsonProperty("ExtractedOutputs")
	private List<String> extractedOutputs;

	@JsonProperty("TextRelations")
	private List<String> textRelations;

	@JsonProperty("OntoRelations")
	private List<String> ontoRelations;

	@JsonIgnore
	private List<List<List<List<Node>>>> ontoRelationsMatrix;

	@JsonIgnore
	private List<List<List<List<String>>>> textRelationsMatrix;

	@JsonIgnore
	private Map<List<String>, double[]> textRelationsT;

	@JsonIgnore
	private Map<List<Node>, Map<List<String>, double[]>> relationsScores;

	private Map<String, Map<String, double[]>> relations;

	@JsonIgnore
	private Map<List<Node>, double[]> ontoRelationsT;

	// Evaluation properties

	@JsonIgnore
	public long execTimeExtr, execTimeEval;

	@JsonProperty("ExtractedInputs")
	public Map<String, Boolean> extractedInputsEval;

	@JsonProperty("ExtractedOutputs")
	public Map<String, Boolean> extractedOutputsEval;

	@JsonProperty("TextRelations")
	public Map<String, Boolean> textRelationsEval;

	@JsonProperty("OntoRelations")
	public Map<String, Boolean> ontoRelationsEval;

	public Service(String name, String text, List<String> inputs, List<String> outputs, List<String> inputsURIs,
			List<String> outputsURIs) {
		this.name = name;
		this.text = text;
		this.inputs = inputs;
		this.outputs = outputs;
		this.inputsURIs = inputsURIs;
		this.outputsURIs = outputsURIs;
	}

	public Service() {
	}

	@JsonIgnore
	private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

	@JsonProperty("ExtractedInputs")
	public List<String> getExtractedInputs() {
		return extractedInputs;
	}

	@JsonProperty("ExtractedInputs")
	public void setExtractedInputs(List<String> extractedInputs) {
		this.extractedInputs = extractedInputs;
	}

	@JsonProperty("Outputs")
	public List<String> getOutputs() {
		return outputs;
	}

	@JsonProperty("Outputs")
	public void setOutputs(List<String> outputs) {
		this.outputs = outputs;
	}

	@JsonProperty("ExtractedOutputs")
	public List<String> getExtractedOutputs() {
		return extractedOutputs;
	}

	@JsonProperty("ExtractedOutputs")
	public void setExtractedOutputs(List<String> extractedOutputs) {
		this.extractedOutputs = extractedOutputs;
	}

	@JsonProperty("Text")
	public String getText() {
		return text;
	}

	@JsonProperty("Text")
	public void setText(String text) {
		this.text = text;
	}

	@JsonProperty("Inputs")
	public List<String> getInputs() {
		return inputs;
	}

	@JsonProperty("Inputs")
	public void setInputs(List<String> inputs) {
		this.inputs = inputs;
	}

	@JsonProperty("Name")
	public String getName() {
		return name;
	}

	@JsonProperty("Name")
	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("TextRelations")
	public List<String> getTextRelations() {
		return textRelations;
	}

	@JsonProperty("TextRelations")
	public void setTextRelations(List<String> textRelations) {
		this.textRelations = textRelations;
	}

	@JsonProperty("OntoRelations")
	public List<String> getOntoRelations() {
		return ontoRelations;
	}

	@JsonProperty("OntoRelations")
	public void setOntoRelations(List<String> ontoRelations) {
		this.ontoRelations = ontoRelations;
	}

	public Map<List<String>, double[]> getTextRelationsT() {
		return textRelationsT;
	}

	public void setTextRelationsT(Map<List<String>, double[]> textRelationsT) {
		this.textRelationsT = textRelationsT;
	}

	public Map<List<Node>, double[]> getOntoRelationsT() {
		return ontoRelationsT;
	}

	public void setOntoRelationsT(Map<List<Node>, double[]> ontoRelationsT) {
		this.ontoRelationsT = ontoRelationsT;
	}

	public List<String> getInputsURIs() {
		return inputsURIs;
	}

	public void setInputsURIs(List<String> inputsURIs) {
		this.inputsURIs = inputsURIs;
	}

	public List<String> getOutputsURIs() {
		return outputsURIs;
	}

	public void setOutputsURIs(List<String> outputsURIs) {
		this.outputsURIs = outputsURIs;
	}

	public Map<String, List<Triple<String, Integer, Integer>>> getInMatches() {
		return inMatches;
	}

	public void setInMatches(Map<String, List<Triple<String, Integer, Integer>>> inMatches) {
		this.inMatches = inMatches;
	}

	public Map<String, List<Triple<String, Integer, Integer>>> getOutMatches() {
		return outMatches;
	}

	public void setOutMatches(Map<String, List<Triple<String, Integer, Integer>>> outMatches) {
		this.outMatches = outMatches;
	}

	public List<List<List<List<Node>>>> getOntoRelationsMatrix() {
		return ontoRelationsMatrix;
	}

	public void setOntoRelationsMatrix(List<List<List<List<Node>>>> ontoRelationsMatrix) {
		this.ontoRelationsMatrix = ontoRelationsMatrix;
	}

	public List<List<List<List<String>>>> getTextRelationsMatrix() {
		return textRelationsMatrix;
	}

	public void setTextRelationsMatrix(List<List<List<List<String>>>> textRelationsMatrix) {
		this.textRelationsMatrix = textRelationsMatrix;
	}

	public Annotation getAnnotation() {
		return annotation;
	}

	public void setAnnotation(Annotation annotation) {
		this.annotation = annotation;
	}

	public Map<List<Node>, Map<List<String>, double[]>> getRelationsScores() {
		return relationsScores;
	}

	public void setRelationsScores(Map<List<Node>, Map<List<String>, double[]>> relationsScores) {
		this.relationsScores = relationsScores;
	}

	public Map<String, Map<String, double[]>> getRelations() {
		return relations;
	}

	public void setRelations(Map<String, Map<String, double[]>> relations) {
		this.relations = relations;
	}

	public List<List<String>> getTokens() {
		return tokens;
	}

	public void setTokens(List<List<String>> tokens) {
		this.tokens = tokens;
	}

	// "any getter" needed for serialization
	@JsonAnyGetter
	public Map<String, Object> any() {
		return additionalProperties;
	}

	@JsonAnySetter
	public void set(String name, Object value) {
		additionalProperties.put(name, value);
	}

}