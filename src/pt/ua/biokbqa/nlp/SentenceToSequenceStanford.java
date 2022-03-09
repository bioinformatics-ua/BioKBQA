package pt.ua.biokbqa.nlp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.tokenization.AbstractTokenizer;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import pt.ua.biokbqa.data.blueprint.Entity;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;

public class SentenceToSequenceStanford {
	private static String language = AbstractReader.LANG_EN;
	static AbstractTokenizer tokenizer = NLPGetter.getTokenizer(language);

	public static void combineSequences(final KBQAQuestion q) {
		MutableTree tree = q.getTree();
		String sentence = q.getLanguageToQuestion().get("en");
		List<String> tokens = tokenizer.getTokens(sentence);
		Map<String, String> label2pos = generatePOSTags(q);
		List<String> subsequence = Lists.newArrayList();
		for (int tcounter = 0; tcounter < tokens.size(); tcounter++) {
			String token = tokens.get(tcounter);
			String pos = label2pos.get(token);
			String nextPos = tcounter + 1 == tokens.size() ? null : label2pos.get(tokens.get(tcounter + 1));
			String lastPos = tcounter == 0 ? null : label2pos.get(tokens.get(tcounter - 1));
			if (subsequence.isEmpty() && null != pos && pos.matches("CD|JJ|NN(.)*|RB(.)*")) {
				subsequence.add(token);
			} else if (!subsequence.isEmpty() && null != pos && tcounter + 1 < tokens.size() && null != nextPos
					&& pos.matches("IN") && !token.matches("of") && nextPos.matches("(W)?DT|NNP(S)?")) {
				if (subsequence.size() > 1) {
					transformTree(subsequence, q);
				}
				subsequence = Lists.newArrayList();
			} else if (!subsequence.isEmpty() && null != pos && null != lastPos && lastPos.matches("NNS")
					&& pos.matches("NNP(S)?")) {
				if (subsequence.size() > 2) {
					transformTree(subsequence, q);
				}
				subsequence = Lists.newArrayList();
			} else if (!subsequence.isEmpty() && !lastPos.matches("JJ|HYPH")
					&& (null == pos || pos.matches("VB(.)*|\\.|WDT") || (pos.matches("IN") && nextPos == null)
							|| (pos.matches("IN") && nextPos.matches("DT")))) {
				if (subsequence.size() > 1) {
					transformTree(subsequence, q);
				}
				subsequence = Lists.newArrayList();
			} else if (!subsequence.isEmpty() && null != pos && pos.matches("NN(.)*|RB|CD|CC|JJ|DT|IN|PRP|HYPH|VBN")) {
				subsequence.add(token);
			} else {
				subsequence = Lists.newArrayList();
			}
		}
		if (!q.getLanguageToNamedEntites().isEmpty()) {
			sentence = replaceLabelsByIdentifiedURIs(sentence, q.getLanguageToNamedEntites().get("en"));
			System.out.println(sentence);
		}
		if (!q.getLanguageToNounPhrases().isEmpty()) {
			sentence = replaceLabelsByIdentifiedURIs(sentence, q.getLanguageToNounPhrases().get("en"));
			System.out.println(sentence);
		}
		q.setTree(tree);
	}

	private static Map<String, String> generatePOSTags(final KBQAQuestion q) {
		Map<String, String> label2pos = Maps.newHashMap();
		Stack<MutableTreeNode> stack = new Stack<>();
		stack.push(q.getTree().getRoot());
		while (!stack.isEmpty()) {
			MutableTreeNode tmp = stack.pop();
			label2pos.put(tmp.label, tmp.posTag);
			for (MutableTreeNode child : tmp.getChildren()) {
				stack.push(child);
			}
		}
		return label2pos;
	}

	private static void transformTree(final List<String> subsequence, final KBQAQuestion q) {
		String combinedNN = Joiner.on(" ").join(subsequence);
		String combinedURI = "http://aksw.org/combinedNN/" + Joiner.on("_").join(subsequence);
		MutableTree tree = q.getTree();
		Entity tmpEntity = new Entity();
		tmpEntity.label = combinedNN;
		tmpEntity.uris.add(new ResourceImpl(combinedURI));
		List<Entity> nounphrases = q.getLanguageToNounPhrases().get("en");
		if (null == nounphrases) {
			nounphrases = Lists.newArrayList();
		}
		nounphrases.add(tmpEntity);
		Stack<MutableTreeNode> stack = new Stack<>();
		stack.push(tree.getRoot());
		List<MutableTreeNode> removables = new ArrayList<>();
		while (!stack.isEmpty()) {
			MutableTreeNode thisNode = stack.pop();
			String label = thisNode.label;
			for (String s : subsequence) {
				if (label.contains(s)) {
					thisNode.label = combinedNN;
					thisNode.posTag = "CombinedNN";
					if (!thisNode.equals(tree.getRoot())) {
						if (thisNode.label == thisNode.parent.label) {
							removables.add(thisNode);
						}
					}
				}
			}
			for (MutableTreeNode child : thisNode.getChildren()) {
				stack.push(child);
			}
		}
		for (MutableTreeNode m : removables) {
			Log.info(SentenceToSequenceStanford.class, "Removing node " + m.nodeNumber + ": " + m.toString());
			tree.remove(m);
		}
		q.setTree(tree);
	}

	private static String replaceLabelsByIdentifiedURIs(String sentence, final List<Entity> list) {
		for (Entity entity : list) {
			if (!entity.label.equals("")) {
				sentence = sentence.replace(entity.label, entity.uris.get(0).getURI() + " ").trim();
			}
		}
		return sentence;
	}
}
