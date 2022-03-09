package pt.ua.biokbqa.cache;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.nlp.MutableTree;
import pt.ua.biokbqa.nlp.MutableTreeNode;
import pt.ua.biokbqa.nlp.ParseTree;
import pt.ua.biokbqa.utils.JSONStatusBuilder;

public class CachedParseTreeClearnlp {
	private ParseTree parseTree;
	private boolean useCache = false;
	private int i;

	public MutableTree process(KBQAQuestion q) {
		if (isStored(q) != null && useCache) {
			return StorageHelper.readFromFileSavely(isStored(q));
		} else {
			System.out.println("Tree not cached.");
			if (parseTree == null) {
				parseTree = new ParseTree();
			}
			DEPTree t = parseTree.process(q);
			MutableTree mutableTree = depToMutableDEP(t);
			System.out.println(mutableTree.toString());
			q.setTree_full(JSONStatusBuilder.treeToJSON(mutableTree));
			store(q, mutableTree);
			return mutableTree;
		}
	}

	private void store(KBQAQuestion q, MutableTree DEPtoMutableDEP) {
		String question = q.getLanguageToQuestion().get("en");
		int hash = question.hashCode();
		String serializedFileName = "cache/" + hash + ".tree";
		StorageHelper.storeToFileSavely(DEPtoMutableDEP, serializedFileName);
	}

	private String isStored(KBQAQuestion q) {
		String question = q.getLanguageToQuestion().get("en");
		int hash = question.hashCode();
		String serializedFileName = "cache/" + hash + ".tree";
		File ser = new File(serializedFileName);
		if (ser.exists()) {
			return serializedFileName;
		} else {
			System.out.println("Question not stored in CachedParseTree");
			return null;
		}
	}

	private MutableTree depToMutableDEP(DEPTree tmp) {
		MutableTree tree = new MutableTree();
		i = 0;
		addNodeRecursivly(tree, tree.head, tmp.getFirstRoot());
		return tree;
	}

	private void addNodeRecursivly(MutableTree tree, MutableTreeNode parent, DEPNode depNode) {
		MutableTreeNode newParent = null;
		if (parent == null) {
			newParent = new MutableTreeNode(depNode.form, depNode.pos, depNode.getLabel(), null, i, depNode.lemma);
			tree.head = newParent;
		} else {
			newParent = new MutableTreeNode(depNode.form, depNode.pos, depNode.getLabel(), parent, i, depNode.lemma);
			parent.addChild(newParent);
		}
		for (DEPNode tmpChilds : depNode.getDependentNodeList()) {
			i++;
			addNodeRecursivly(tree, newParent, tmpChilds);
		}
	}

	public static void main(String args[]) {
		KBQAQuestion q = new KBQAQuestion();
		Map<String, String> languageToQuestion = new HashMap<String, String>();
		languageToQuestion.put("en", "Which anti-apartheid activist was born in Mvezo?");
		q.setLanguageToQuestion(languageToQuestion);
		new CachedParseTreeClearnlp().process(q);
	}
}
