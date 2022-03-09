package pt.ua.biokbqa.ranking;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import pt.ua.biokbqa.controller.PipelineClearNLP;
import pt.ua.biokbqa.data.blueprint.Answer;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.nlp.MutableTree;
import pt.ua.biokbqa.nlp.MutableTreeNode;
import pt.ua.biokbqa.nlp.MutableTreeNodeIterator;

public class TierRanker implements Ranking {
	private MutableTree tree = new MutableTree();
	private MutableTreeNodeIterator it;

	public TierRanker() {
	}

	public TierRanker(final MutableTree tree) {
		init(tree);
	}

	public void init(final MutableTree tree) {
		this.tree = tree;
		it = new MutableTreeNodeIterator(this.tree.getRoot());

	}

	protected double getScore(final Query query) {
		double ret = 0.0;
		Set<String> clause = queryToNodeList(query);
		while (it.hasNext()) {
			MutableTreeNode cur = it.next();
			int nodeTier = it.getTier();
			double curScore = getScoreForNode(cur, nodeTier, clause);
			ret += curScore;
		}
		it.reset();
		return ret;
	}

	protected static double getScoreForNode(final MutableTreeNode node, final int nodeTier, final Set<String> clause) {
		double ret = 0;
		if (clause.contains(node.label)) {
			ret = 1;
		}
		double tier = 1.0 / Math.pow(2, nodeTier);
		return ret * tier;
	}

	private static Set<String> queryToNodeList(final Query q) {
		final Set<String> subjects = new HashSet<>();
		ElementWalker.walk(q.getQueryPattern(), new ElementVisitorBase() {
			@Override
			public void visit(final ElementPathBlock el) {
				Iterator<TriplePath> triples = el.patternElts();
				while (triples.hasNext()) {
					TriplePath t = triples.next();
					subjects.add(getString(t.getSubject()));
					subjects.add(getString(t.getObject()));
					subjects.add(getString(t.getPredicate()));
				}
			}
		});
		return subjects;
	}

	private static String getString(final Node n) {
		if (n.isURI()) {
			return n.getURI();
		}
		if (n.isVariable()) {
			return n.toString();
		}
		if (n.isBlank()) {
			return n.getBlankNodeLabel();
		}
		if (n.isLiteral()) {
			return n.toString(false);
		}
		return "";
	}

	protected static String resolvedClause(final Query q) {
		Map<String, String> map = q.getPrefixMapping().getNsPrefixMap();
		for (String key : map.keySet()) {
			q.getPrefixMapping().removeNsPrefix(key);
		}
		return q.getQueryPattern().toString();
	}

	@Override
	public List<Answer> rank(final List<Answer> answers, final KBQAQuestion q) {
		init(q.getTree());
		for (Answer answer : answers) {
			double rank = getScore(QueryFactory.create(answer.queryString));
			System.out.println(answer.queryString + " ranked with score: " + rank);
			answer.score = rank;
		}
		Collections.sort(answers);
		Collections.reverse(answers);
		return answers;
	}

	public static void main(final String args[]) {
		PipelineClearNLP pipeline = new PipelineClearNLP();
		KBQAQuestion q = new KBQAQuestion();
		q.getLanguageToQuestion().put("en", "What is the capital of Spain called?");
		String correctAnswer = "[http://dbpedia.org/resource/Madrid]";
		System.out.println(
				"Run pipeline on " + q.getLanguageToQuestion().get("en") + ", expecting answer: " + correctAnswer);
		List<Answer> answers = pipeline.getAnswersToQuestion(q);
		System.out.println("Run ranking");
		@SuppressWarnings("unused")
		int maximumPositionToMeasure = 10;
		TierRanker tier = new TierRanker();
		System.out.println("Tier-based ranking");
		List<Answer> rankedAnswer = tier.rank(answers, q);
		double maxScore = 0.0;
		double correctScore = 0.0;
		for (Answer ans : rankedAnswer) {
			if (ans.score > maxScore) {
				maxScore = ans.score;
			}
			if ((ans.answerSet.toString().equals(correctAnswer)) && (ans.score > correctScore)) {
				correctScore = ans.score;
			}
		}
		System.out.println("maxScore = " + maxScore);
		System.out.println("correctScore = " + correctScore);
	}
}
