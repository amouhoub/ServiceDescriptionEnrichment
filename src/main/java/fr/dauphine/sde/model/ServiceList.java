package fr.dauphine.sde.model;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * A class to represent a list of Semantic Web Services. Used for storing
 * services and for export in JSON format
 * 
 * @author Mohamed LAmine Mouhoub
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "services" })
public class ServiceList {

	@JsonProperty("services")
	private List<Service> services;

	public List<String> UsableServices = new ArrayList<>();

	@JsonIgnore
	private Map<String, Service> data;

	public ServiceList() {
		data = new LinkedHashMap<>();
		services = new ArrayList<>();
	}

	public void add(Service s) {
		services.add(s);
		data.put(s.getName(), s);
	}

	public Service get(String serviceName) {
		return data.get(serviceName);
	}

	public boolean contains(String serviceName) {
		return data.containsKey(serviceName);
	}

	@JsonIgnore
	public String getJson() {
		// if you don't care about order just use a regular HashMap
		Map<String, Object> theMap = new LinkedHashMap<>();
		// put your objects in the Map with their names as keys
		theMap.put("results", data);

		// write the map using your code
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

		try {
			return ow.writeValueAsString(theMap);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "ERROR";
	}

	public void saveJSON() {

		// try-with-resources statement based on post comment below :)
		try (FileWriter file = new FileWriter("/Users/aminos/Downloads/resultsNEW.json")) {
			file.write(getJson());
			System.out.println("Successfully Copied JSON Object to File...");
			// System.out.println("\nJSON Object: " + service);
		} catch (IOException e) {
			System.err.println("Coult not save json file");
			e.printStackTrace();
		}
	}

	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("services")
	public List<Service> getServices() {
		return services;
	}

	@JsonProperty("services")
	public void setServices(List<Service> service) {
		this.services = service;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}