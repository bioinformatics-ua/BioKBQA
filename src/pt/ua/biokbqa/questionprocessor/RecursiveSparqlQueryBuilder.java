package pt.ua.biokbqa.questionprocessor;

import java.util.Set;
import org.apache.jena.query.ResultSet;
import com.google.common.collect.Sets;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.nlp.MutableTreeNode;
import pt.ua.biokbqa.utils.URLs;

public class RecursiveSparqlQueryBuilder {

	public Set<SPARQLQuery> start(final SPARQLQueryBuilder sparqlQueryBuilder, final KBQAQuestion q) {
		SPARQLQuery initialQuery = new SPARQLQuery();
		initialQuery.isASKQuery(q.getIsClassifiedAsASKQuery());
		Set<SPARQLQuery> returnSet = Sets.newHashSet(initialQuery);
		Set<String> variableSet = Sets.newHashSet("?proj", "?const");
		try {
			MutableTreeNode tmp = q.getTree().getRoot();
			recursion(returnSet, variableSet, tmp);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		Object[] objs = returnSet.toArray();
		System.out.println("SPARQL:\n" + objs[objs.length - 1]);
		return returnSet;
	}

	private void recursion(final Set<SPARQLQuery> returnSet, final Set<String> variableSet, final MutableTreeNode tmp)
			throws CloneNotSupportedException {
		Set<SPARQLQuery> sb = Sets.newHashSet();
		if (!tmp.getAnnotations().isEmpty()) {
			for (SPARQLQuery query : returnSet) {
				for (String anno : tmp.getAnnotations()) {
					if (tmp.posTag.matches("VB(.)*")) {
						// iterar variáveis - ver melhor
						SPARQLQuery variant1 = ((SPARQLQuery) query.clone());
						variant1.addConstraint("?proj <" + anno + "> ?const.");
						SPARQLQuery variant2 = ((SPARQLQuery) query.clone());
						variant2.addConstraint("?const <" + anno + "> ?proj.");
						SPARQLQuery variant3 = ((SPARQLQuery) query.clone());
						sb.add(variant1);
						sb.add(variant2);
						sb.add(variant3);
					} else if (tmp.posTag.matches("NN(.)*|WRB")) {
						// SPARQLQuery variant1 = ((SPARQLQuery) query.clone());
						// variant1.addConstraint("?proj <" + anno +
						// "> ?const.");
						SPARQLQuery variant2 = ((SPARQLQuery) query.clone());
						variant2.addConstraint("?const <" + anno + "> ?proj.");
						SPARQLQuery variant3 = ((SPARQLQuery) query.clone());
						variant3.addConstraint("?const a <" + anno + ">.");
						SPARQLQuery variant4 = ((SPARQLQuery) query.clone());
						variant4.addConstraint("?proj a <" + anno + ">.");
						// SPARQLQuery variant5 = ((SPARQLQuery) query.clone());
						// variant5.addFilterOverAbstractsContraint("?proj",
						// tmp.label);
						SPARQLQuery variant6 = ((SPARQLQuery) query.clone());
						variant6.addFilterOverAbstractsContraint("?const", tmp.label);
						SPARQLQuery variant7 = ((SPARQLQuery) query.clone());
						// sb.add(variant1);
						sb.add(variant2);
						sb.add(variant3);
						sb.add(variant4);
						// sb.add(variant5);
						sb.add(variant6);
						sb.add(variant7);
					} else if (tmp.posTag.matches("WP")) {
						SPARQLQuery variant1 = ((SPARQLQuery) query.clone());
						variant1.addConstraint("?const a <" + anno + ">.");
						SPARQLQuery variant2 = ((SPARQLQuery) query.clone());
						variant2.addConstraint("?proj a <" + anno + ">.");
						SPARQLQuery variant3 = ((SPARQLQuery) query.clone());
						sb.add(variant1);
						sb.add(variant2);
						sb.add(variant3);
					} else {
						System.out.println("Tmp: " + tmp.label + " pos: " + tmp.posTag);
					}
				}
			}
		} else {
			if (tmp.posTag.matches("CombinedNN|NNP(.)*|JJ|CD")) {
				for (SPARQLQuery query : returnSet) {
					SPARQLQuery variant1 = (SPARQLQuery) query.clone();
					variant1.addFilterOverAbstractsContraint("?proj", tmp.label);
					SPARQLQuery variant2 = (SPARQLQuery) query.clone();
					variant2.addFilterOverAbstractsContraint("?const", tmp.label);
					SPARQLQuery variant3 = (SPARQLQuery) query.clone();
					sb.add(variant1);
					sb.add(variant2);
					sb.add(variant3);
				}
			} else if (tmp.posTag.matches("VB(.)*")) {
				for (SPARQLQuery query : returnSet) {
					SPARQLQuery variant1 = (SPARQLQuery) query.clone();
					variant1.addFilterOverAbstractsContraint("?proj", tmp.label);
					SPARQLQuery variant2 = (SPARQLQuery) query.clone();
					variant2.addFilterOverAbstractsContraint("?const", tmp.label);
					SPARQLQuery variant3 = (SPARQLQuery) query.clone();
					sb.add(variant1);
					sb.add(variant2);
					sb.add(variant3);
				}
			} else if (tmp.posTag.matches("ADD")) {
				Set<String> origLabels = getOrigLabel(tmp.label);
				for (SPARQLQuery query : returnSet) {
					SPARQLQuery variant1 = (SPARQLQuery) query.clone();
					variant1.addConstraint("?proj ?pbridge <" + tmp.label + ">.");
					// SPARQLQuery variant2 = (SPARQLQuery) query.clone();
					// variant2.addFilter("?proj IN (<" + tmp.label + ">)");
					SPARQLQuery variant3 = (SPARQLQuery) query.clone();
					sb.add(variant1);
					// sb.add(variant2);
					sb.add(variant3);
					for (String origLabel : origLabels) {
						SPARQLQuery variant4 = (SPARQLQuery) query.clone();
						variant4.addFilterOverAbstractsContraint("?proj", origLabel);
						SPARQLQuery variant5 = (SPARQLQuery) query.clone();
						variant5.addFilterOverAbstractsContraint("?const", origLabel);
						sb.add(variant4);
						sb.add(variant5);
					}
				}
			} else if (tmp.posTag.matches("NN|NNS")) {
				for (SPARQLQuery query : returnSet) {
					SPARQLQuery variant1 = (SPARQLQuery) query.clone();
					variant1.addFilterOverAbstractsContraint("?proj", tmp.label);
					SPARQLQuery variant2 = (SPARQLQuery) query.clone();
					variant2.addFilterOverAbstractsContraint("?const", tmp.label);
					SPARQLQuery variant3 = (SPARQLQuery) query.clone();
					sb.add(variant1);
					sb.add(variant2);
					sb.add(variant3);
				}
			} else if (tmp.posTag.matches("WP")) {
				sb.addAll(returnSet);
			} else {
				System.out.println("Tmp: " + tmp.label + " pos: " + tmp.posTag);
			}
		}
		returnSet.clear();
		returnSet.addAll(sb);
		for (MutableTreeNode child : tmp.getChildren()) {
			System.out.println("Recursion started for :" + child);
			recursion(returnSet, variableSet, child);
		}
	}

	private Set<String> getOrigLabel(final String label) {
		Set<String> resultset = Sets.newHashSet();
		String query = "SELECT str(?proj) as ?proj WHERE {<" + label
				+ "> <http://www.w3.org/2000/01/rdf-schema#label> ?proj. FILTER(langMatches(lang(?proj), \"EN\"))}";
		org.apache.jena.sparql.engine.http.QueryEngineHTTP qe = new org.apache.jena.sparql.engine.http.QueryEngineHTTP(
				URLs.DBpediaURL, query);
		try {
			if (qe != null) {
				ResultSet results = qe.execSelect();
				while (results.hasNext()) {
					resultset.add(results.next().get("proj").toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			qe.close();
		}
		System.out.println("resultset@getOrigLabel = " + resultset);
		return resultset;
	}
}
