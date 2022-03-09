package pt.ua.biokbqa.controller;

import java.util.List;
import pt.ua.biokbqa.data.blueprint.Answer;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.nlp.MutableTreePruner;
import pt.ua.biokbqa.nlp.UnitController;
import pt.ua.biokbqa.questionprocessor.Annotater;
import pt.ua.biokbqa.questionprocessor.SPARQL;
import pt.ua.biokbqa.questionprocessor.SPARQLQueryBuilder;
import pt.ua.biokbqa.spotter.ASpotter;
import pt.ua.biokbqa.spotter.Spotlight;
// import pt.ua.biokbqa.spotter.TagMe;

public class PipelineStanford extends AbstractPipeline {
	private ASpotter nerdModule;
	private MutableTreePruner pruner;
	private Annotater annotater;
	private SPARQLQueryBuilder queryBuilder;
	private Cardinality cardinality;
	private QueryTypeClassifier queryTypeClassifier;
	private StanfordNLPConnector stanfordConnector;
	private UnitController numberToDigit;

	public PipelineStanford() {
		queryTypeClassifier = new QueryTypeClassifier();
		nerdModule = new Spotlight();
		// nerdModule = new TagMe();
		this.stanfordConnector = new StanfordNLPConnector();
		this.numberToDigit = new UnitController();
		numberToDigit.instantiateEnglish(stanfordConnector);
		cardinality = new Cardinality();
		pruner = new MutableTreePruner();
		SPARQL sparql = new SPARQL();
		annotater = new Annotater(sparql);
		queryBuilder = new SPARQLQueryBuilder(sparql);
	}

	@Override
	public List<Answer> getAnswersToQuestion(final KBQAQuestion q) {
		System.out.println("Question: " + q.getLanguageToQuestion().get("en"));
		System.out.println("Classify question type.");
		q.setIsClassifiedAsASKQuery(queryTypeClassifier.isASKQuery(q.getLanguageToQuestion().get("en")));
		// disambiguate parts of the query
		System.out.println("Named entity recognition.");
		q.setLanguageToNamedEntites(nerdModule.getEntities(q.getLanguageToQuestion().get("en")));
		// noun combiner, decrease #nodes in the DEPTree
		System.out.println("Noun phrase combination / Dependency Parsing");
		q.setTree(stanfordConnector.combineSequences(q, this.numberToDigit));
		// cardinality identifies the integer i used for LIMIT i
		System.out.println("Cardinality calculation.");
		q.setCardinality(cardinality.cardinality(q));
		// apply pruning rules
		System.out.println("Pruning tree.");
		q.setTree(pruner.prune(q));
		// annotate tree
		System.out.println("Semantically annotating the tree.");
		annotater.annotateTree(q);
		// calculating all possible SPARQL BGPs with given semantic annotations
		System.out.println("Calculating SPARQL representations.");
		List<Answer> answers = queryBuilder.build(q);
		return answers;
	}

	public static void main(final String[] args) {
		PipelineStanford p = new PipelineStanford();
		KBQAQuestion q = new KBQAQuestion();
		q.getLanguageToQuestion().put("en", "Which anti-apartheid activist was born in Mvezo?");
		p.getAnswersToQuestion(q);
	}
}
