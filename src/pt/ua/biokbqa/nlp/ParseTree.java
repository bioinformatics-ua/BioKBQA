package pt.ua.biokbqa.nlp;

import java.io.IOException;
import java.util.List;
import java.util.Stack;
import com.clearnlp.component.AbstractComponent;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.nlp.NLPMode;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.tokenization.AbstractTokenizer;
import com.google.common.base.Joiner;
import pt.ua.biokbqa.data.blueprint.Entity;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;

public class ParseTree {
	final static String language = AbstractReader.LANG_EN;
	final static String modelType = "general-en";
	private static AbstractTokenizer tokenizer = NLPGetter.getTokenizer(language);
	private static AbstractComponent tagger;
	private static AbstractComponent parser;
	private static AbstractComponent identifier;
	private static AbstractComponent classifier;
	private static AbstractComponent labeler;
	private static AbstractComponent[] components;

	protected static synchronized void initialize() {
		if (components == null) {
			try {
				tagger = NLPGetter.getComponent(modelType, language, NLPMode.MODE_POS);
				parser = NLPGetter.getComponent(modelType, language, NLPMode.MODE_DEP);
				identifier = NLPGetter.getComponent(modelType, language, NLPMode.MODE_PRED);
				classifier = NLPGetter.getComponent(modelType, language, NLPMode.MODE_ROLE);
				labeler = NLPGetter.getComponent(modelType, language, NLPMode.MODE_SRL);
				components = new AbstractComponent[] { tagger, parser, identifier, classifier, labeler };
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public ParseTree() {
		initialize();
	}

	public DEPTree process(KBQAQuestion q) {
		return process(tokenizer, components, q);

	}

	private DEPTree process(AbstractTokenizer tokenizer, AbstractComponent[] components, KBQAQuestion q) {
		String sentence = q.getLanguageToQuestion().get("en");
		if (!q.getLanguageToNamedEntites().isEmpty()) {
			sentence = replaceLabelsByIdentifiedURIs(sentence, q.getLanguageToNamedEntites().get("en"));
			System.out.println(sentence);
		}
		if (!q.getLanguageToNounPhrases().isEmpty()) {
			sentence = replaceLabelsByIdentifiedURIs(sentence, q.getLanguageToNounPhrases().get("en"));
			System.out.println(sentence);
		}
		DEPTree tree = NLPGetter.toDEPTree(tokenizer.getTokens(sentence));
		for (AbstractComponent component : components)
			component.process(tree);
		System.out.println(TreeTraversal.inorderTraversal(tree.getFirstRoot(), 0, null));
		System.out.println(tree.toStringSRL());
		resolveCompoundNouns(tree, q.getLanguageToNounPhrases().get("en"));
		System.out.println(TreeTraversal.inorderTraversal(tree.getFirstRoot(), 0, null));
		return tree;
	}

	private void resolveCompoundNouns(DEPTree tree, List<Entity> list) {
		Stack<DEPNode> stack = new Stack<DEPNode>();
		stack.push(tree.getFirstRoot());
		while (!stack.isEmpty()) {
			DEPNode thisNode = stack.pop();
			String label = thisNode.form;
			if (label.contains("aksw.org")) {
				thisNode.form = Joiner.on(" ").join(label.replace("http://aksw.org/combinedNN/", "").split("_"));
				thisNode.pos = "CombinedNN";
			}
			for (DEPNode child : thisNode.getDependentNodeList()) {
				stack.push(child);
			}
		}
	}

	private String replaceLabelsByIdentifiedURIs(String sentence, List<Entity> list) {
		for (Entity entity : list) {
			if (!entity.label.equals("")) {
				sentence = sentence.replace(entity.label, entity.uris.get(0).getURI() + " ").trim();
			} else {
				System.out.println("Entity has no label in sentence: " + sentence);
			}
		}
		return sentence;
	}
}
