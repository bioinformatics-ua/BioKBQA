package pt.ua.biokbqa.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import com.google.common.base.Joiner;
import edu.stanford.nlp.international.Language;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import pt.ua.biokbqa.data.blueprint.Entity;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.nlp.MutableTree;
import pt.ua.biokbqa.nlp.MutableTreeNode;
import pt.ua.biokbqa.nlp.SentenceToSequence;
import pt.ua.biokbqa.nlp.UnitController;

public class StanfordNLPConnector {

	private StanfordCoreNLP stanfordPipe;
	private int nodeNumber;
	private Set<IndexedWord> visitedNodes;
	public static StringBuilder out = new StringBuilder();

	public StanfordNLPConnector(final String annotators) {
		Properties props = new Properties();
		props.setProperty("annotators", annotators);
		stanfordPipe = new StanfordCoreNLP(props);
	}

	public StanfordNLPConnector() {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit,pos,lemma, ner,parse");
		stanfordPipe = new StanfordCoreNLP(props);
	}

	private String replaceNamedEntitysWithURL(final KBQAQuestion q) {
		String sentence = q.getLanguageToQuestion().get("en");
		if (!q.getLanguageToNamedEntites().isEmpty()) {
			sentence = replaceLabelsByIdentifiedURIs(sentence, q.getLanguageToNamedEntites().get("en"));
			System.out.println(sentence);
		}
		if (!q.getLanguageToNounPhrases().isEmpty()) {
			sentence = replaceLabelsByIdentifiedURIs(sentence, q.getLanguageToNounPhrases().get("en"));
			System.out.println(sentence);
		}
		return sentence;
	}

	private String replaceLabelsByIdentifiedURIs(final String sentence, final List<Entity> list) {
		List<String> textParts = new ArrayList<>();
		list.sort(Comparator.comparing(Entity::getOffset).reversed());
		int startFormerLabel = sentence.length();
		for (Entity currentNE : list) {
			int currentNEStartPos = currentNE.getOffset();
			int currentNEEndPos = currentNEStartPos + currentNE.label.length();
			if (startFormerLabel >= currentNEEndPos) {
				textParts.add(sentence.substring(currentNEEndPos, startFormerLabel));
				textParts.add(currentNE.uris.get(0).getURI());
				startFormerLabel = currentNEStartPos;
			}
		}
		if (startFormerLabel > 0) {
			textParts.add(sentence.substring(0, startFormerLabel));
		}
		StringBuilder textWithMarkups = new StringBuilder();
		for (int i = textParts.size() - 1; i >= 0; --i) {
			textWithMarkups.append(textParts.get(i));
		}
		return textWithMarkups.toString();
	}

	public Annotation runAnnotation(final KBQAQuestion q) {
		Annotation annotationDocument = new Annotation(q.getLanguageToQuestion().get("en"));
		this.stanfordPipe.annotate(annotationDocument);
		return annotationDocument;
	}

	public Annotation runAnnotation(final String s) {
		Annotation annotationDocument = new Annotation(s);
		this.stanfordPipe.annotate(annotationDocument);
		return annotationDocument;
	}

	public MutableTree process(final Annotation document) {
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		CoreMap sen = sentences.get(0);
		SemanticGraph graph = sen.get(CollapsedCCProcessedDependenciesAnnotation.class);
		MutableTree tree = semanticGraphToMutableTree(graph, null);
		System.out.println(tree.toString());
		return tree;
	}

	public MutableTree combineSequences(final KBQAQuestion q) {
		return this.combineSequences(q, null);
	}

	public MutableTree combineSequences(final KBQAQuestion q, final UnitController numberToDigit) {
		String sentence = this.replaceNamedEntitysWithURL(q);
		System.out.println(sentence);
		if (numberToDigit != null) {
			sentence = numberToDigit.normalizeNumbers("en", sentence);
			System.out.println(sentence);
		}
		Annotation document = this.runAnnotation(preprocessStringForStanford(sentence));
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		CoreMap sen = sentences.get(0);
		SemanticGraph graph = sen.get(CollapsedCCProcessedDependenciesAnnotation.class);
		MutableTree tree = semanticGraphToMutableTree(graph, q);
		System.out.println(tree.toString());
		return tree;
	}

	public Map<String, String> generatePOSTags(final Annotation document) {
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		CoreMap sen = sentences.get(0);
		List<String> tokens = new LinkedList<>();
		Map<String, String> label2pos = new HashMap<>();
		for (CoreLabel token : sen.get(TokensAnnotation.class)) {
			String word = token.get(TextAnnotation.class);
			String pos = token.get(PartOfSpeechAnnotation.class);
			tokens.add(word);
			label2pos.put(word, pos);
		}
		return label2pos;
	}

	private MutableTree semanticGraphToMutableTree(final SemanticGraph graph, final KBQAQuestion q) {
		nodeNumber = 0;
		MutableTree tree = new MutableTree();
		MutableTreeNode mutableRoot;
		this.visitedNodes = new HashSet<>();
		IndexedWord graphRoot = graph.getFirstRoot();
		mutableRoot = new MutableTreeNode(postprocessStringForStanford(graphRoot), graphRoot.tag(), "root", null,
				nodeNumber++, graphRoot.lemma());
		tree.head = mutableRoot;
		convertGraphStanford(mutableRoot, graphRoot, graph, q);
		return tree;
	}

	public String preprocessStringForStanford(final String input) {
		return input.replaceAll("[()]", "////");
	}

	public String postprocessStringForStanford(final IndexedWord node) {
		return node.word().replaceAll("(////)(.+)(////)", "($2)");
	}

	private void convertGraphStanford(final MutableTreeNode parentMutableNode, final IndexedWord parentGraphWord,
			final SemanticGraph graph, final KBQAQuestion q) {
		visitedNodes.add(parentGraphWord);
		parentGraphWord.setWord(postprocessStringForStanford(parentGraphWord));
		if (parentGraphWord.word().contains("http://dbpedia.org/resource/")) {
			parentMutableNode.posTag = "ADD";
		}
		Set<IndexedWord> notCyclicChildren = new HashSet<>(graph.getChildren(parentGraphWord));
		notCyclicChildren.removeAll(visitedNodes);
		if (notCyclicChildren.isEmpty()) {
			return;
		}
		if (q != null && !parentMutableNode.posTag.equals("ADD")) {
			GrammaticalRelation gr = new GrammaticalRelation(Language.UniversalEnglish, "compound", null, null);
			ArrayList<IndexedWord> compounds = new ArrayList<>();
			compounds.addAll(graph.getChildrenWithReln(parentGraphWord, gr));
			ArrayList<IndexedWord> removeMe = new ArrayList<>();
			for (IndexedWord child : compounds)
				if (child.word().contains("http://dbpedia.org/resource/")) {
					removeMe.add(child);
				}
			compounds.removeAll(removeMe);
			if (!compounds.isEmpty()) {
				compounds.add(parentGraphWord);
				Collections.sort(compounds);
				ArrayList<String> orderlyWords = new ArrayList<>();
				for (IndexedWord compoundChild : compounds) {
					orderlyWords.add(compoundChild.word());
				}
				SentenceToSequence.transformTree(orderlyWords, q);
				parentMutableNode.setPosTag("CombinedNN");
				parentMutableNode.setLabel(Joiner.on(" ").join(orderlyWords));
				parentMutableNode.lemma = "#url#";
				compounds.remove(parentGraphWord);
				for (IndexedWord compoundChild : compounds) {
					notCyclicChildren.addAll(graph.getChildList(compoundChild));
				}
				notCyclicChildren.removeAll(compounds);
			}
		}
		for (IndexedWord child : notCyclicChildren) {
			SemanticGraphEdge edge = graph.getEdge(parentGraphWord, child);
			String depLabel = edge.getRelation().getShortName();
			MutableTreeNode childMutableNode = new MutableTreeNode(child.word(), child.tag(), depLabel,
					parentMutableNode, nodeNumber++, child.lemma());
			parentMutableNode.addChild(childMutableNode);
			convertGraphStanford(childMutableNode, child, graph, q);
		}
	}
}
