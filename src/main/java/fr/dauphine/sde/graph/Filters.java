package fr.dauphine.sde.graph;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_NotEquals;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.E_StrStartsWith;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

/**
 * This class contains methods to add SPARQL filters for SW service queries
 * using multiple criteria
 * 
 * @author Mohamed LAmine Mouhoub
 *
 */
public abstract class Filters {

	protected static final String NS_DBR = "http://dbpedia.org/resource/";
	protected static final String NS_DBO = "http://dbpedia.org/ontology/";
	protected static final String NS_DBP = "http://dbpedia.org/property/";
	protected static final String NS_SKOS = "http://www.w3.org/2004/02/skos/core#";
	protected static final String NS_RDF = RDF.getURI();
	protected static final String NS_DCT = DCTerms.getURI();
	protected static final String NS_OWL = OWL.getURI();

	protected static final Node DBO = NodeFactory.createURI(NS_DBO);

	/**
	 * returns a list of SPARQL filters for preventing rdfs:subClassOf
	 * properties in the query results
	 * 
	 * @param maxDistance
	 * @return
	 */
	public static List<ElementFilter> getPropertyFilters(int maxDistance) {
		List<ElementFilter> result = new ArrayList<>();
		// Properties only
		if (maxDistance > 0) {
			ElementFilter filterP;
			// Avoid subClassOf
			for (Var v : GraphUtils.getPropertyVars(maxDistance)) {
				filterP = new ElementFilter(
						new E_NotEquals(new ExprVar(v), new NodeValueNode(RDFS.subClassOf.asNode())));
				result.add(filterP);
			}
		}
		return result;
	}

	/**
	 * returns a list of SPARQL filters to keep only nodes/results from DBPedia
	 * 
	 * @param vars
	 * @param distance
	 * @return
	 */
	public static List<ElementFilter> getDBPediaFilters(List<Var> vars, int distance) {
		List<ElementFilter> result = new ArrayList<>();
		ElementFilter filter;
		// Applies only to intermediate nodes
		for (Var v : GraphUtils.getIntermediateVars(vars, distance)) {
			// Make sure all is from dbpedia
			// TODO: use default filters instead
			filter = new ElementFilter(
					new E_StrStartsWith(new E_Str(new ExprVar(v)), new E_Str(new NodeValueNode(Filters.DBO))));
			result.add(filter);

		}
		return result;
	}

	/**
	 * returns a list of SPARQL filters for preventing intermediate nodes from
	 * equaling I/O nodes
	 * 
	 * @param vars
	 * @param maxDistance
	 * @param depth
	 * @return
	 */
	public static List<ElementFilter> getDifferentFromInOutFilters(List<Var> vars, int maxDistance, int depth) {
		List<ElementFilter> result = new ArrayList<>();
		Var inVar = vars.get(0);
		Var outVar = vars.get(vars.size() - 1);
		ElementFilter filter;

		// Applies only to intermediate nodes
		for (Var v : GraphUtils.getIntermediateVars(vars, maxDistance)) {
			// Avoid O_i == inVar or ourVar
			filter = new ElementFilter(new E_LogicalAnd(new E_NotEquals(new ExprVar(inVar), new ExprVar(v)),
					new E_NotEquals(new ExprVar(outVar), new ExprVar(v))));
			result.add(filter);

		}
		return result;
	}

	/**
	 * returns a list of SPARQL filters for preventing Directed Cyclic Graphs
	 * from occurring in the query results. (prevent loops in I/O relations)
	 * 
	 * @param vars
	 *            Query variables
	 * @param distance
	 *            current distance
	 * @param maxDistance
	 *            maximum distance
	 * @param depth
	 *            current depth depth
	 * @return
	 */
	public static List<ElementFilter> getAntiDCGFilters(List<Var> vars, int distance, int maxDistance, int depth) {
		List<ElementFilter> result = new ArrayList<>();
		ElementFilter filter;

		// Applies only to intermediate nodes
		for (Var v : GraphUtils.getIntermediateVars(vars, distance)) {
			// Avoid loops and Directed Cyclic Graphs
			for (Var v2 : GraphUtils.getCurrentSuperVars(vars, maxDistance, depth)) {
				if (v != v2) {
					filter = new ElementFilter(new E_NotEquals(new ExprVar(v), new ExprVar(v2)));
					result.add(filter);
				}
			}

		}
		return result;
	}

	/**
	 * returns a list of SPARQL filters for preventing :
	 * <ul>
	 * <li>intermediate nodes (M or O) from equaling I/O</li>
	 * <li>rdfs:subClassOf properties</li>
	 * </ul>
	 * 
	 * @param vars
	 * @param maxDistance
	 * @param depth
	 * @return
	 */
	public static List<ElementFilter> getAllFilters(List<Var> vars, int maxDistance, int depth) {

		List<ElementFilter> result = new ArrayList<>();

		result.addAll(getPropertyFilters(maxDistance));

		result.addAll(getDifferentFromInOutFilters(vars, maxDistance, depth));

		// Useless for the moment
		// result.addAll(getAntiDACFilters(vars, maxDistance, depth));

		return result;
	}

}
