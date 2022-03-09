package pt.ua.biokbqa.questionprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.index.DBOIndex;
import pt.ua.biokbqa.index.IndexDBO_classes;
import pt.ua.biokbqa.index.IndexDBO_properties;
import pt.ua.biokbqa.nlp.MutableTree;
import pt.ua.biokbqa.nlp.MutableTreeNode;
import pt.ua.biokbqa.utils.JSONStatusBuilder;

public class Annotater {
	IndexDBO_classes classesIndex = new IndexDBO_classes();
	IndexDBO_properties propertiesIndex = new IndexDBO_properties();
	DBOIndex dboIndex = new DBOIndex();
	Set<String> blacklist = Sets.newHashSet("people");
	private SPARQL sparql;

	public Annotater(final SPARQL sparql) {
		this.sparql = sparql;
	}

	public void annotateTree(final KBQAQuestion q) {
		MutableTree tree = q.getTree();
		annotateProjectionLeftTree(tree);
		annotateVerbs(tree);
		annotateNouns(tree);
		q.setTree_final(JSONStatusBuilder.treeToJSON(q.getTree()));
	}

	private void annotateNouns(final MutableTree tree) {
		Stack<MutableTreeNode> stack = new Stack<>();
		stack.push(tree.getRoot());
		while (!stack.isEmpty()) {
			MutableTreeNode tmp = stack.pop();
			String label = tmp.label;
			String posTag = tmp.posTag;
			if (!blacklist.contains(label)) {
				if (posTag.matches("NN(.)*") && tmp.getAnnotations().isEmpty()) {
					ArrayList<String> search = classesIndex.search(label);
					if (!search.isEmpty()) {
						for (String uri : search) {
							tmp.addAnnotation(uri);
						}
					} else if (!propertiesIndex.search(label).isEmpty()) {
						search = propertiesIndex.search(label);
						for (String uri : search) {
							tmp.addAnnotation(uri);
						}
					} else {
						search = dboIndex.search(label);
						for (String uri : search) {
							tmp.addAnnotation(uri);
						}
					}
					if (tmp.getAnnotations().isEmpty()) {
						if (tmp.lemma != null) {
							label = tmp.lemma;
						}
						search = classesIndex.search(label);
						for (String uri : search) {
							tmp.addAnnotation(uri);
						}
						search = propertiesIndex.search(label);
						for (String uri : search) {
							tmp.addAnnotation(uri);
						}
						search = dboIndex.search(label);
						for (String uri : search) {
							tmp.addAnnotation(uri);
						}
					}
				} else {
					System.out.println("Not annotated node: " + tmp);
				}
				for (MutableTreeNode child : tmp.getChildren()) {
					stack.push(child);
				}
			}
		}
	}

	private void annotateVerbs(final MutableTree tree) {
		Stack<MutableTreeNode> stack = new Stack<>();
		stack.push(tree.getRoot());
		while (!stack.isEmpty()) {
			MutableTreeNode tmp = stack.pop();
			String label = tmp.label;
			String posTag = tmp.posTag;
			if (posTag.matches("VB(.)*")) {
				List<String> search = propertiesIndex.search(label);
				if (search.isEmpty() && tmp.lemma != null) {
					search = propertiesIndex.search(tmp.lemma);
				} else if (search.isEmpty()) {
					search = dboIndex.search(label);
				}
				search = rank(search);
				for (String uri : search) {
					tmp.addAnnotation(uri);
				}
				System.out.println(Joiner.on(", ").join(tmp.getAnnotations()));
			}
			for (MutableTreeNode child : tmp.getChildren()) {
				stack.push(child);
			}
		}
	}

	private List<String> rank(final List<String> search) {
		List<String> predicates = Lists.newArrayList();
		try {
			int maxNum = 0;
			String maxPred = "";
			for (String predicate : search) {
				QueryExecution qe = sparql.qef
						.createQueryExecution("SELECT count(*) WHERE { ?const <" + predicate + "> ?var.}");
				if (qe != null) {
					ResultSet results = qe.execSelect();
					while (results.hasNext()) {
						int predicateCount = results.next().get(".1").asLiteral().getInt();
						System.out.println(predicate + "\t" + predicateCount);
						if (predicateCount > maxNum && !(predicate.contains("Year") || predicate.contains("Date"))) {
							maxNum = predicateCount;
							maxPred = predicate;
						}
					}
				}
			}
			predicates.add(maxPred);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return predicates;

	}

	private void annotateProjectionLeftTree(final MutableTree tree) {
		Stack<MutableTreeNode> stack = new Stack<>();
		if (tree.getRoot() != null && tree.getRoot().getChildren() != null && !tree.getRoot().getChildren().isEmpty()) {
			stack.push(tree.getRoot().getChildren().get(0));
			while (!stack.isEmpty()) {
				MutableTreeNode tmp = stack.pop();
				String label = tmp.label;
				String posTag = tmp.posTag;
				if (!blacklist.contains(label)) {
					if (tmp.children.size() == 0) {
						if (posTag.matches("WRB|WP")) {
							if (label.equals("Where")) {
								tmp.addAnnotation("http://dbpedia.org/ontology/Place");
							} else if (label.equals("Who")) {
								tmp.addAnnotation("http://dbpedia.org/ontology/Agent");
							}
						} else if (posTag.matches("NN(.)*")) {
							if (posTag.matches("NNS")) {
								if (tmp.lemma != null)
									label = tmp.lemma;
							}
							if (classesIndex.search(label).size() > 0) {
								ArrayList<String> uris = classesIndex.search(label);
								for (String resourceURL : uris) {
									tmp.addAnnotation(resourceURL);
								}
							} else {
							}
						} else {
							System.out.println("Not annotated node: " + tmp);
						}
					} else {
						if (posTag.matches("NN(.)*")) {
							if (posTag.matches("NNS")) {
								if (tmp.lemma != null)
									label = tmp.lemma;
							}
							if (classesIndex.search(label).size() > 0 || propertiesIndex.search(label).size() > 0) {
								ArrayList<String> uris = classesIndex.search(label);
								for (String resourceURL : uris) {
									tmp.addAnnotation(resourceURL);
								}
								uris = propertiesIndex.search(label);
								for (String resourceURL : uris) {
									tmp.addAnnotation(resourceURL);
								}
							} else if (dboIndex.search(label).size() > 0) {
								ArrayList<String> uris = dboIndex.search(label);
								for (String resourceURL : uris) {
									tmp.addAnnotation(resourceURL);
								}
							} else {
								System.out.println("Not annotated node: " + tmp);
							}
						} else if (posTag.matches("WRB|WP")) {
							if (label.equals("Where")) {
								tmp.addAnnotation("http://dbpedia.org/ontology/Place");
							} else if (label.equals("Who")) {
								tmp.addAnnotation("http://dbpedia.org/ontology/Agent");
							}
						} else {
						}
					}
				}
				break;
			}
		}
	}
}
