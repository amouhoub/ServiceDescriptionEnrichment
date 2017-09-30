package fr.dauphine.sde.graph;

import java.util.ArrayList;
import java.util.List;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;

import fr.dauphine.sde.model.Params;

/**
 * A toolset for manipulating the I/O relations SPARQL queries and their results
 * 
 * 
 * @author Mohamed LAmine Mouhoub
 *
 */
public abstract class GraphUtils {

	/**
	 * Returns a list of all binary combinations on a given number of bits in a
	 * string format.
	 * 
	 * @param nbrOfBits
	 * @return Example: ["000", "001", "010", ... "111"]
	 */
	protected static ArrayList<String> listAllBinaryCombinations(int nbrOfBits) {
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i < Math.pow(2, nbrOfBits + 1); i++) {
			StringBuilder binary = new StringBuilder(Integer.toBinaryString(i));
			for (int j = binary.length(); j < nbrOfBits + 1; j++) {
				binary.insert(0, '0');
			}
			result.add(binary.toString());
		}
		return result;
	}

	/**
	 * Converts a binary combination string (Examples :"000", "001", "010", ...
	 * "111") that serve as path patterns into BGP paths with query variables
	 * 
	 * @param vars
	 *            query variables
	 * @param combi
	 *            binary combination string (eg. "000", "001", "010", ... "111")
	 * @param distance
	 *            current path distance
	 * @param depthI
	 *            current Input class depth
	 * @param depthO
	 *            current Output class depth
	 * @param maxDepth
	 *            maximum depth
	 * @return
	 */
	protected static ElementTriplesBlock combinationToBGP(List<Var> vars, String combi, int distance, int depthI,
			int depthO, int maxDepth) {
		return Params.OWLS_TC_Pattern ? combinationToOwlsBGP(vars, combi, distance, depthI, depthO, maxDepth)
				: combinationToNormalBGP(vars, combi, distance, depthI, depthO, maxDepth);
	}

	/**
	 * Creates a triples block (BGP) from a given path combination between I and
	 * O. A binary combination string is used as a template for the path
	 * 
	 * @param vars
	 * @param combi
	 * @param distance
	 * @param depthI
	 * @param depthO
	 * @param maxDepth
	 * @return
	 */
	private static ElementTriplesBlock combinationToNormalBGP(List<Var> vars, String combi, int distance, int depthI,
			int depthO, int maxDepth) {

		// vars=[In, ..., sIni, pk, rk, Ok , sOutj,.., Out]

		ElementTriplesBlock resBlock = new ElementTriplesBlock();

		int p = maxDepth + 1;
		int r, o2;
		int o1 = depthI;

		for (int i = 0; i < combi.length(); i++) {
			r = p + 1;
			o2 = i == combi.length() - 1 ? vars.size() - depthO - 1 : p + 2;
			if (combi.charAt(i) == '0') {
				resBlock.addTriple(new Triple(vars.get(p), RDFS.domain.asNode(), vars.get(o1)));
				resBlock.addTriple(new Triple(vars.get(p), RDFS.range.asNode(), vars.get(o2)));
			} else {
				resBlock.addTriple(new Triple(vars.get(r), RDFS.domain.asNode(), vars.get(o2)));
				resBlock.addTriple(new Triple(vars.get(r), RDFS.range.asNode(), vars.get(o1)));
			}
			p = p + 3;
			o1 = o2;
		}
		return resBlock;
	}

	private static ElementTriplesBlock combinationToOwlsBGP(List<Var> vars, String combi, int distance, int depthI,
			int depthO, int maxDepth) {

		// vars=[In, ..., sIni, pk, rk, Ok , sOutj,.., Out]

		ElementTriplesBlock resBlock = new ElementTriplesBlock();

		int p = maxDepth + 1;
		int r, o2;
		int o1 = depthI;

		for (int i = 0; i < combi.length(); i++) {
			r = p + 1;
			o2 = i == combi.length() - 1 ? vars.size() - depthO - 1 : p + 2;
			if (combi.charAt(i) == '0') {
				resBlock.addTriple(new Triple(vars.get(o1), RDFS.subClassOf.asNode(), Var.alloc("x" + i)));
				resBlock.addTriple(new Triple(Var.alloc("x" + i), OWL.onProperty.asNode(), vars.get(p)));
				resBlock.addTriple(new Triple(Var.alloc("x" + i), Var.alloc("y" + i), vars.get(o2)));
			} else {
				resBlock.addTriple(new Triple(vars.get(o2), RDFS.subClassOf.asNode(), Var.alloc("x" + i)));
				resBlock.addTriple(new Triple(Var.alloc("x" + i), OWL.onProperty.asNode(), vars.get(r)));
				resBlock.addTriple(new Triple(Var.alloc("x" + i), Var.alloc("y" + i), vars.get(o1)));
			}
			p = p + 3;
			o1 = o2;
		}
		return resBlock;
	}

	/**
	 * Converts a SPARQL query solution into a list of nodes using the list of
	 * query vars as a reference to prevent empty nodes in the results
	 * 
	 * @param vars
	 * @param soln
	 * @return
	 */
	protected static List<Node> solutionToNodeList(List<Var> vars, QuerySolution soln) {
		List<Node> results = new ArrayList<>();
		// System.out.println("------>" + soln.toString());
		for (Var var : vars) {
			if (soln.get(var.getName()) != null)
				results.add(soln.get(var.getName()).asNode());
			else
				results.add(null);
			// System.out.println(soln.get(var.getName()));
		}
		// System.out.println("======>" + results.toString());
		return results;
	}

	/**
	 * Generates and returns the list of "Property" variables of the query
	 * 
	 * @param distance
	 *            Maximum Distance
	 * @return
	 */
	protected static List<Var> getPropertyVars(int distance) {
		List<Var> result = new ArrayList<>();

		if (distance > 0) {
			for (int i = 0; i < distance; i++) {
				result.add(Var.alloc("p" + i));
				result.add(Var.alloc("r" + i));
			}
			result.add(Var.alloc("p" + distance));
			result.add(Var.alloc("r" + distance));
		} else {
			result.add(Var.alloc("p" + 0));
			result.add(Var.alloc("r" + 0));
		}
		return result;
	}

	/**
	 * Generates and returns the list of query variables corresponding to
	 * "Intermediate nodes"
	 * 
	 * @param vars
	 * @param distance
	 * @return
	 */
	protected static List<Var> getIntermediateVars(List<Var> vars, int distance) {
		List<Var> result = new ArrayList<>();

		for (int i = 1; i <= distance; i++) {
			result.add(Var.alloc("O" + i));
		}

		return result;
	}

	/**
	 * Generates and returns the list of query variables corresponding to I/O
	 * and their super classes
	 * 
	 * @param vars
	 * @param i
	 * @param j
	 * @return
	 */
	protected static List<Var> getCurrentSuperVars(List<Var> vars, int i, int j) {
		List<Var> result = new ArrayList<>();

		result.add(vars.get(i));

		result.add(vars.get(vars.size() - j - 1));

		return result;
	}

}
