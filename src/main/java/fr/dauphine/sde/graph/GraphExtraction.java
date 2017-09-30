package fr.dauphine.sde.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.vocabulary.RDFS;

import fr.dauphine.sde.model.Params;
import fr.dauphine.sde.model.Service;

/**
 * A toolset for extracting I/O relations from ontologies using SPARQL queries
 * Can be also used in general for extracting all possible paths between two
 * nodes in any ontology. <br>
 * <b>REQUIRES</b> a set of parameters that should be set in the {@link Params}
 * TODO: use method parameters instead of {@link Params}
 * 
 * @author Mohamed LAmine Mouhoub
 *
 */
public abstract class GraphExtraction {

	/**
	 * Returns a matrix of matrices (n*m*k*l) The super Matrix is the matrix of
	 * all combinations of inputs (in rows) and outputs (in columns). Each cell
	 * in the upper level (super) matrix is a matrix of all the paths (rows)
	 * between Ii and Oj, the details of each path (steps) are stored in columns
	 * 
	 * @param service
	 * @return
	 */
	public static List<List<List<List<Node>>>> extractIORelationsMatrix(Service service) {
		List<List<List<List<Node>>>> result = new ArrayList<>();
		List<List<List<Node>>> outputRels;
		for (String i : service.getInputsURIs()) {
			outputRels = new ArrayList<>();
			for (String o : service.getOutputsURIs()) {
				// System.out.println("Extracting OntoRel(" + i + "," + o +
				// ")");
				outputRels.add(getRelationsforIO(i, o));
			}
			result.add(outputRels);
		}
		return result;
	}

	private static List<List<Node>> getRelationsforIO(String i, String o) {
		Query ioRelQuery = relationQueryWHierarchy(i, o, Params.maxDistance, Params.maxHierarchy);
		// System.out.println(ioRelQuery);
		return GetBGPPath(ioRelQuery, Params.ONTOLOGY_ENDPOINT);
	}

	/**
	 * @param var
	 *            In/Out variable name
	 * @param depth
	 *            Depth of the hierarchy
	 * @param inOut
	 *            Indices order is ASC for inputs and DESC for outputs True if
	 *            Input, False if Output
	 * @return
	 */
	private static ElementTriplesBlock getSuperClassesBlock(Var var, int depth, boolean inOut) {
		ElementTriplesBlock result = new ElementTriplesBlock();
		if (depth > 0) {
			result.addTriple(new org.apache.jena.graph.Triple(var, RDFS.subClassOf.asNode(),
					Var.alloc(inOut ? "sIn" + 1 : "sOut" + 1)));
		}
		for (int i = 1; i < depth; i++) {
			result.addTriple(new org.apache.jena.graph.Triple(Var.alloc(inOut ? "sIn" + i : "sOut" + i),
					RDFS.subClassOf.asNode(), Var.alloc(inOut ? "sIn" + (i + 1) : "sOut" + (i + 1))));
		}
		return result;
	}

	/**
	 * Generates a SPARQL query to find all the possible paths between an input
	 * and an output nodes within a maximum distance and a maximum depth
	 * (WHierarchy means : with hierarchy taken into account)
	 * 
	 * @param inStr
	 *            Input URI as a String
	 * @param outStr
	 *            Output URI as a String
	 * @param maxDistance
	 *            Maximum Relation (path) distance
	 * @param depth
	 *            maximum depth Maximum Depth (maximum rdfs:subClassOf
	 *            properties in the path)
	 * @return
	 */
	private static Query relationQueryWHierarchy(String inStr, String outStr, int maxDistance, int depth) {

		Node inNode = NodeFactory.createURI(inStr.replace('-', Character.MIN_VALUE));
		Node outNode = NodeFactory.createURI(outStr.replace('-', Character.MIN_VALUE));

		Query qRes = new Query();
		qRes.setQuerySelectType();
		qRes.setDistinct(true);
		Var inVar = Var.alloc(inNode.getLocalName());
		Var outVar = Var.alloc(outNode.getLocalName());

		// SELECT ?In, InS1, InS2, ... p0, r0, O1,, p1, r1, ..., ...,?sOut2,
		// sOut1, ?Out
		// vars=[In, ..., sIni, ..... , sOutj,.., Out]
		List<Var> vars = getQueryVars(inVar, outVar, maxDistance, depth);

		ElementGroup group = new ElementGroup();
		ElementUnion union = new ElementUnion();
		ElementGroup subQuery;

		for (int i = 0; i <= depth; i++) {
			for (int j = 0; j <= depth; j++) {

				// Get the hierarchy triples set first
				ElementTriplesBlock ins = getSuperClassesBlock(inVar, i, true);
				ElementTriplesBlock outs = getSuperClassesBlock(outVar, j, false);

				for (int distance = 0; distance <= maxDistance; distance++) {

					ElementGroup hierarchyHeader = new ElementGroup();

					// copy hierarchy triples for each combination
					if (i > 0) {
						hierarchyHeader.addElement(ins);
					}
					if (j > 0) {
						hierarchyHeader.addElement(outs);
					}

					// Generate list of all binary combinations
					List<String> binCombiList = GraphUtils.listAllBinaryCombinations(distance);

					// ElementGroup subQuery;
					for (String strComb : binCombiList) {
						subQuery = new ElementGroup();
						if (i > 0 || j > 0) {
							subQuery.addElement(hierarchyHeader);
						}
						// DBPEDIA
						// FOR OWLS-TC, use combinationToBGP_OWLS instead
						subQuery.addElement(GraphUtils.combinationToBGP(vars, strComb, distance, i, j, depth));

						// Add filters locally
						// Intermediate nodes O_i different from I/O and their
						// Supers to avoid loops (DCG)
						if (Params.FilterDCG) {
							for (ElementFilter filter : Filters.getAntiDCGFilters(vars, distance, i, j)) {
								subQuery.addElementFilter(filter);
							}
						}

						if (Params.FilterSubClass) {
							for (ElementFilter filter : Filters.getPropertyFilters(distance)) {
								subQuery.addElementFilter(filter);
							}
						}

						// Add filters locally
						// All O_i from from the same ontology as I/O
						// (DBpedia for instance)

						if (Params.FilterSameOntology) {
							for (ElementFilter filter : Filters.getDBPediaFilters(vars, distance)) {
								subQuery.addElementFilter(filter);
							}
						}
						union.addElement(subQuery);
					}
				}

			}
		}

		group.addElement(union);

		qRes.addProjectVars(vars);

		qRes.setQueryPattern(group);

		qRes.setPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		qRes.setPrefix("owl", "http://www.w3.org/2002/07/owl#");
		qRes.setPrefix("dbo", "http://dbpedia.org/ontology/");

		String better = StringUtils.removeEnd(qRes.toString(), "  }\n");
		better = better + "    VALUES " + inVar.toString() + " { <" + inStr.toString() + "> }\n";
		better = better + "    VALUES " + outVar.toString() + " { <" + outStr.toString() + "> }\n}";

		// System.out.println(better);

		return QueryFactory.create(better);

	}

	/**
	 * Generates and returns a list of query variables (project) with respect to
	 * the maximum values (number of variables) of the query
	 * 
	 * @param inVar
	 *            Input Variable
	 * @param outVar
	 *            Output Variable
	 * @param maxDistance
	 *            Maximum Distance
	 * @param maxDepth
	 *            Maximum Depth
	 * @return
	 */
	private static List<Var> getQueryVars(Var inVar, Var outVar, int maxDistance, int maxDepth) {
		List<Var> result = new ArrayList<>();

		result.add(inVar);
		for (int i = 1; i <= maxDepth; i++) {
			result.add(Var.alloc("sIn" + i));
		}
		if (maxDistance > 0) {
			for (int i = 0; i < maxDistance; i++) {
				result.add(Var.alloc("p" + i));
				result.add(Var.alloc("r" + i));
				result.add(Var.alloc("O" + (i + 1)));
			}
			result.add(Var.alloc("p" + maxDistance));
			result.add(Var.alloc("r" + maxDistance));
		} else {
			result.add(Var.alloc("p" + 0));
			result.add(Var.alloc("r" + 0));
		}
		for (int i = maxDepth; i >= 1; i--) {
			result.add(Var.alloc("sOut" + i));
		}
		result.add(outVar);
		return result;
	}

	/**
	 * <ul>
	 * <li>Runs the query against the given SPARQL
	 * <li>Parses the query solution into arrays of Nodes
	 * <li>Puts all the solutions (Node arrays) in a List
	 * </ul>
	 * 
	 * @param query
	 *            relation extraction query
	 * @param endpoint
	 *            SPARQL Endpoint, can be local or remote
	 * @return
	 */
	private static List<List<Node>> GetBGPPath(Query query, String endpoint) {

		List<List<Node>> nodes = new ArrayList<>();

		// Prepare the query execution module
		QueryExecution qExe = QueryExecutionFactory.sparqlService(endpoint, query);

		// Set timeout
		qExe.setTimeout(60, TimeUnit.SECONDS);

		// Run the query and get the results
		ResultSet results = qExe.execSelect();

		// Get the columns of the results (vars of the query)
		List<Var> columns = query.getProjectVars();

		// Transform each solution into a set of triples (one or more)
		while (results.hasNext()) {
			QuerySolution soln = results.next();
			nodes.add(GraphUtils.solutionToNodeList(columns, soln));
		}

		// VERY IMPORTANT: close the connection, even though it is
		// AutoClosable, the server freezes if not closed manually
		qExe.close();

		return nodes;
	}

}
