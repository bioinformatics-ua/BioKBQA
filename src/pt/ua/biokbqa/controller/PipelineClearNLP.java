package pt.ua.biokbqa.controller;

import java.util.List;
import pt.ua.biokbqa.cache.CachedParseTreeClearnlp;
import pt.ua.biokbqa.data.blueprint.Answer;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.nlp.MutableTreePruner;
import pt.ua.biokbqa.nlp.SentenceToSequence;
import pt.ua.biokbqa.questionprocessor.Annotater;
import pt.ua.biokbqa.questionprocessor.SPARQL;
import pt.ua.biokbqa.questionprocessor.SPARQLQueryBuilder;
import pt.ua.biokbqa.spotter.Fox;

@SuppressWarnings("deprecation")
public class PipelineClearNLP extends AbstractPipeline {
	private Fox nerdModule;
	private CachedParseTreeClearnlp cParseTree;
	private SentenceToSequence sentenceToSequence;
	private MutableTreePruner pruner;
	private Annotater annotater;
	private SPARQLQueryBuilder queryBuilder;
	private Cardinality cardinality;
	private QueryTypeClassifier queryTypeClassifier;

	public PipelineClearNLP() {
		queryTypeClassifier = new QueryTypeClassifier();
		nerdModule = new Fox();
		cParseTree = new CachedParseTreeClearnlp();
		cardinality = new Cardinality();
		sentenceToSequence = new SentenceToSequence();
		pruner = new MutableTreePruner();
		SPARQL sparql = new SPARQL();
		annotater = new Annotater(sparql);
		queryBuilder = new SPARQLQueryBuilder(sparql);
	}

	@SuppressWarnings({ "static-access" })
	@Override
	public List<Answer> getAnswersToQuestion(KBQAQuestion q) {
		System.out.println("Question: " + q.getLanguageToQuestion().get("en"));
		System.out.println("Classify question type.");
		q.setIsClassifiedAsASKQuery(queryTypeClassifier.isASKQuery(q.getLanguageToQuestion().get("en")));
		System.out.println("Named entity recognition.");
		q.setLanguageToNamedEntites(nerdModule.getEntities(q.getLanguageToQuestion().get("en")));
		System.out.println("Noun phrase combination.");
		sentenceToSequence.combineSequences(q);
		System.out.println("Dependency parsing.");
		q.setTree(cParseTree.process(q));
		System.out.println("Cardinality calculation.");
		q.setCardinality(cardinality.cardinality(q));
		System.out.println("Pruning tree.");
		q.setTree(pruner.prune(q));
		System.out.println("Semantically annotating the tree.");
		annotater.annotateTree(q);
		System.out.println("Calculating SPARQL representations.");
		List<Answer> answers = queryBuilder.build(q);
		return answers;
	}
}
