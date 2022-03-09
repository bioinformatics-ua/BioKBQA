package pt.ua.biokbqa.benchmark;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import pt.ua.biokbqa.data.blueprint.Answer;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;

public class Evaluator {

	public static double precision(final Set<RDFNode> systemAnswer, final KBQAQuestion question) {
		if (systemAnswer == null) {
			return 0;
		}
		double precision = 0;
		Set<RDFNode> goldenRDFNodes = answersToRDFNode(question.getGoldenAnswers());
		if (question.getPseudoSparqlQuery() != null) {
			if (isSelectType(question.getPseudoSparqlQuery())) {
				SetView<RDFNode> intersection = Sets.intersection(goldenRDFNodes, systemAnswer);
				if (systemAnswer.size() != 0) {
					precision = (double) intersection.size() / (double) systemAnswer.size();
				}
			} else if (isAskType(question.getPseudoSparqlQuery())) {
				if (systemAnswer.size() == 1) {
					RDFNode ans = systemAnswer.iterator().next();
					RDFNode goldstandardAns = goldenRDFNodes.iterator().next();
					if (ans.toString().equals(goldstandardAns.toString())) {
						precision = 1;
					}
				}
			} else {
				System.out.println("Unsupported Query Type" + question.getPseudoSparqlQuery());
			}
		} else if (question.getSparqlQuery() != null) {
			if (isSelectType(question.getSparqlQuery())) {
				SetView<RDFNode> intersection = Sets.intersection(goldenRDFNodes, systemAnswer);
				if (systemAnswer.size() != 0) {
					precision = (double) intersection.size() / (double) systemAnswer.size();
				}
			} else if (isAskType(question.getSparqlQuery())) {
				if (systemAnswer.size() == 1) {
					RDFNode ans = systemAnswer.iterator().next();
					RDFNode goldstandardAns = goldenRDFNodes.iterator().next();
					if (ans.toString().equals(goldstandardAns.toString())) {
						precision = 1;
					}
				}
			} else {
				System.out.println("Unsupported Query Type" + question.getSparqlQuery());
			}
		}
		return precision;
	}

	public static double recall(final Set<RDFNode> systemAnswer, final KBQAQuestion question) {
		if (systemAnswer == null) {
			return 0;
		}
		double recall = 0;
		Set<RDFNode> goldenRDFNodes = answersToRDFNode(question.getGoldenAnswers());
		if (question.getPseudoSparqlQuery() != null) {
			if (isSelectType(question.getPseudoSparqlQuery())) {
				if (question.getAggregation()) {
					recall = 1;
				}
				SetView<RDFNode> intersection = Sets.intersection(systemAnswer, goldenRDFNodes);
				if (goldenRDFNodes.size() != 0) {
					recall = (double) intersection.size() / (double) goldenRDFNodes.size();
				}
			} else if (isAskType(question.getPseudoSparqlQuery())) {
				recall = 1;
			} else {
				System.out.println("Unsupported Query Type" + question.getPseudoSparqlQuery());
			}
		} else if (question.getSparqlQuery() != null) {
			if (isSelectType(question.getSparqlQuery())) {
				if (question.getAggregation()) {
					recall = 1;
				}
				SetView<RDFNode> intersection = Sets.intersection(systemAnswer, goldenRDFNodes);
				if (goldenRDFNodes.size() != 0) {
					recall = (double) intersection.size() / (double) goldenRDFNodes.size();
				}
			} else if (isAskType(question.getSparqlQuery())) {
				recall = 1;
			} else {
				System.out.println("Unsupported Query Type" + question.getSparqlQuery());
			}
		}
		return recall;
	}

	public static double fMeasure(final Set<RDFNode> systemAnswers, final KBQAQuestion question) {
		double precision = precision(systemAnswers, question);
		double recall = recall(systemAnswers, question);
		double fMeasure = 0;
		if (precision + recall > 0) {
			fMeasure = 2 * precision * recall / (precision + recall);
		}
		return fMeasure;
	}

	public Evaluation getEvaluation(String dataset, String benchmark) {
		return null;
	}

	public List<Evaluation> measure(final List<Answer> rankedAnswer, final KBQAQuestion q, final int maxK) {
		// calculate precision, recall, f1 measure for each answer
		List<Evaluation> list = Lists.newArrayList();
		for (Answer answer : rankedAnswer) {
			Set<RDFNode> answerSet = answer.answerSet;
			double precision = Evaluator.precision(answerSet, q);
			double recall = Evaluator.recall(answerSet, q);
			double fMeasure = Evaluator.fMeasure(answerSet, q);
			System.out.println("Measure @" + (list.size() + 1) + "P=" + precision + " R=" + recall + " F=" + fMeasure);
			list.add(new Evaluation(q.getId(), q.getLanguageToQuestion().get("en"), fMeasure, precision, recall,
					"Measure @" + (list.size() + 1), answer));
			if (list.size() > maxK) {
				break;
			}
		}
		return list;
	}

	public static boolean isAskType(final String sparqlQuery) {
		if (sparqlQuery == null) {
			return false;
		}
		return sparqlQuery.contains("\nASK\n") || sparqlQuery.contains("ASK ");
	}

	private static boolean isSelectType(final String sparqlQuery) {
		return sparqlQuery.contains("\nSELECT\n") || sparqlQuery.contains("SELECT ");
	}

	private static Set<RDFNode> answersToRDFNode(final Set<String> answers) {
		Set<RDFNode> tmp = new HashSet<>();
		for (String s : answers) {
			tmp.add(new ResourceImpl(s));
		}
		return tmp;
	}
}
