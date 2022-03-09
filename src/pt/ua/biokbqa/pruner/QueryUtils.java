package pt.ua.biokbqa.pruner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections15.ListUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.util.VarUtils;
import org.apache.jena.vocabulary.RDF;

public class QueryUtils extends ElementVisitorBase {

	private Set<Triple> triplePattern;
	private Set<Triple> optionalTriplePattern;
	private boolean inOptionalClause = false;
	private int unionCount = 0;
	private int optionalCount = 0;
	private int filterCount = 0;

	public Set<Var> getVariables(final Query query) {
		Set<Var> vars = new HashSet<>();
		Set<Triple> triplePatterns = extractTriplePattern(query, false);
		for (Triple tp : triplePatterns) {
			if (tp.getSubject().isVariable()) {
				vars.add(Var.alloc(tp.getSubject()));
			} else if (tp.getObject().isVariable()) {
				vars.add(Var.alloc(tp.getObject()));
			} else if (tp.getPredicate().isVariable()) {
				vars.add(Var.alloc(tp.getPredicate()));
			}
		}
		return vars;
	}

	public Set<Var> getSubjectVariables(final Query query) {
		Set<Var> vars = new HashSet<>();
		Set<Triple> triplePatterns = extractTriplePattern(query, false);
		for (Triple tp : triplePatterns) {
			if (tp.getSubject().isVariable()) {
				vars.add(Var.alloc(tp.getSubject()));
			}
		}
		return vars;
	}

	public Set<Var> getObjectVariables(final Query query) {
		Set<Var> vars = new HashSet<>();
		Set<Triple> triplePatterns = extractTriplePattern(query, false);
		for (Triple tp : triplePatterns) {
			if (tp.getObject().isVariable()) {
				vars.add(Var.alloc(tp.getObject()));
			}
		}
		return vars;
	}

	public Set<Triple> getRDFTypeTriples(final Query query) {
		Set<Triple> triplePatterns = extractTriplePattern(query);
		for (Iterator<Triple> iterator = triplePatterns.iterator(); iterator.hasNext();) {
			Triple triple = iterator.next();
			if (!triple.getPredicate().matches(RDF.type.asNode())) {
				iterator.remove();
			}
		}
		return triplePatterns;
	}

	public Set<Triple> extractOutgoingTriplePatterns(final Query query, final Node node) {
		Set<Triple> triplePatterns = extractTriplePattern(query, false);
		for (Iterator<Triple> iterator = triplePatterns.iterator(); iterator.hasNext();) {
			Triple triple = iterator.next();
			if (!triple.subjectMatches(node)) {
				iterator.remove();
			}
		}
		return triplePatterns;
	}

	public Set<Triple> extractIncomingTriplePatterns(final Query query, final Node node) {
		Set<Triple> triplePatterns = extractTriplePattern(query, false);
		for (Iterator<Triple> iterator = triplePatterns.iterator(); iterator.hasNext();) {
			Triple triple = iterator.next();
			if (!triple.objectMatches(node)) {
				iterator.remove();
			}
		}
		return triplePatterns;
	}

	public Set<Triple> extractIngoingTriplePatterns(final Query query, final Node node) {
		Set<Triple> triplePatterns = extractTriplePattern(query, false);
		for (Iterator<Triple> iterator = triplePatterns.iterator(); iterator.hasNext();) {
			Triple triple = iterator.next();
			if (!triple.objectMatches(node)) {
				iterator.remove();
			}
		}
		return triplePatterns;
	}

	public Set<Triple> extractTriplePatterns(final Query query, final Node node) {
		Set<Triple> triplePatterns = new HashSet<>();
		triplePatterns.addAll(extractIngoingTriplePatterns(query, node));
		triplePatterns.addAll(extractOutgoingTriplePatterns(query, node));
		return triplePatterns;
	}

	public Set<Triple> extractNonOptionalTriplePatterns(final Query query, final Node node) {
		Set<Triple> triplePatterns = new HashSet<>();
		triplePatterns.addAll(extractIngoingTriplePatterns(query, node));
		triplePatterns.addAll(extractOutgoingTriplePatterns(query, node));
		triplePatterns.removeAll(optionalTriplePattern);
		return triplePatterns;
	}

	public Map<Var, Set<Triple>> extractTriplePatternsForProjectionVars(final Query query) {
		Map<Var, Set<Triple>> var2TriplePatterns = new HashMap<>();
		for (Var var : query.getProjectVars()) {
			Set<Triple> triplePatterns = new HashSet<>();
			triplePatterns.addAll(extractIngoingTriplePatterns(query, var));
			triplePatterns.addAll(extractOutgoingTriplePatterns(query, var));
			var2TriplePatterns.put(var, triplePatterns);
		}
		return var2TriplePatterns;
	}

	public Map<Var, Set<Triple>> extractOutgoingTriplePatternsForProjectionVars(final Query query) {
		Map<Var, Set<Triple>> var2TriplePatterns = new HashMap<>();
		for (Var var : query.getProjectVars()) {
			Set<Triple> triplePatterns = new HashSet<>();
			triplePatterns.addAll(extractOutgoingTriplePatterns(query, var));
			var2TriplePatterns.put(var, triplePatterns);
		}
		return var2TriplePatterns;
	}

	public Set<Triple> getOptionalTriplePatterns() {
		return optionalTriplePattern;
	}

	public Map<Var, Set<Triple>> extractIncomingTriplePatternsForProjectionVars(final Query query) {
		Map<Var, Set<Triple>> var2TriplePatterns = new HashMap<>();
		for (Var var : query.getProjectVars()) {
			Set<Triple> triplePatterns = new HashSet<>();
			triplePatterns.addAll(extractIncomingTriplePatterns(query, var));
			var2TriplePatterns.put(var, triplePatterns);
		}
		return var2TriplePatterns;
	}

	public Map<Var, Set<Triple>> extractIngoingTriplePatternsForProjectionVars(final Query query) {
		Map<Var, Set<Triple>> var2TriplePatterns = new HashMap<>();
		for (Var var : query.getProjectVars()) {
			Set<Triple> triplePatterns = new HashSet<>();
			triplePatterns.addAll(extractIngoingTriplePatterns(query, var));
			var2TriplePatterns.put(var, triplePatterns);
		}
		return var2TriplePatterns;
	}

	public Set<Triple> extractTriplePattern(final Query query) {
		return extractTriplePattern(query, false);
	}

	public Set<Triple> extractTriplePattern(final Query query, final boolean ignoreOptionals) {
		triplePattern = new HashSet<>();
		optionalTriplePattern = new HashSet<>();
		query.getQueryPattern().visit(this);
		if (!ignoreOptionals) {
			if (query.isSelectType()) {
				for (Triple t : optionalTriplePattern) {
					if (!ListUtils.intersection(new ArrayList<>(VarUtils.getVars(t)), query.getProjectVars())
							.isEmpty()) {
						triplePattern.add(t);
					}
				}
			}
		}
		return triplePattern;
	}

	public boolean isOptional(final Triple triple) {
		return optionalTriplePattern.contains(triple);
	}

	public Set<Triple> extractTriplePattern(final ElementGroup group) {
		return extractTriplePattern(group, false);
	}

	public Set<Triple> extractTriplePattern(final ElementGroup group, final boolean ignoreOptionals) {
		triplePattern = new HashSet<>();
		optionalTriplePattern = new HashSet<>();
		group.visit(this);
		if (!ignoreOptionals) {
			for (Triple t : optionalTriplePattern) {
				triplePattern.add(t);
			}
		}
		return triplePattern;
	}

	@Override
	public void visit(final ElementGroup el) {
		for (Element e : el.getElements()) {
			e.visit(this);
		}
	}

	@Override
	public void visit(final ElementOptional el) {
		optionalCount++;
		inOptionalClause = true;
		el.getOptionalElement().visit(this);
		inOptionalClause = false;
	}

	@Override
	public void visit(final ElementTriplesBlock el) {
		for (Iterator<Triple> iterator = el.patternElts(); iterator.hasNext();) {
			Triple t = iterator.next();
			if (inOptionalClause) {
				optionalTriplePattern.add(t);
			} else {
				triplePattern.add(t);
			}
		}
	}

	@Override
	public void visit(final ElementPathBlock el) {
		for (Iterator<TriplePath> iterator = el.patternElts(); iterator.hasNext();) {
			TriplePath tp = iterator.next();
			if (inOptionalClause) {
				optionalTriplePattern.add(tp.asTriple());
			} else {
				if (tp.isTriple()) {
					triplePattern.add(tp.asTriple());
				}
			}
		}
	}

	@Override
	public void visit(final ElementUnion el) {
		unionCount++;
		for (Element e : el.getElements()) {
			e.visit(this);
		}
	}

	@Override
	public void visit(final ElementFilter el) {
		filterCount++;
	}

	public int getUnionCount() {
		return unionCount;
	}

	public int getOptionalCount() {
		return optionalCount;
	}

	public int getFilterCount() {
		return filterCount;
	}

	public static void main(final String[] args) throws Exception {
		Query q = QueryFactory.create(
				"PREFIX  dbp:  <http://dbpedia.org/resource/>\n" + "PREFIX  dbo: <http://dbpedia.org/ontology/>\n"
						+ "SELECT  ?thumbnail\n" + "WHERE\n" + "  { dbp:total !dbo:thumbnail ?thumbnail }");
		QueryUtils triplePatternExtractor = new QueryUtils();
		triplePatternExtractor.extractIngoingTriplePatterns(q, q.getProjectVars().get(0));
	}
}
