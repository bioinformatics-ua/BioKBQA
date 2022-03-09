package pt.ua.biokbqa.nlp;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.utils.JSONStatusBuilder;

public class MutableTreePruner {

	public MutableTree prune(KBQAQuestion q) {
		System.out.println(q.getTree().toString());
		removalRules(q);
		removalBasedOnDependencyLabels(q);
		applyInterrogativeRules(q);
		sortTree(q.getTree());
		System.out.println(q.getTree().toString());
		q.setTree_pruned(JSONStatusBuilder.treeToJSON(q.getTree()));
		return q.getTree();
	}

	private void sortTree(MutableTree tree) {
		Queue<MutableTreeNode> queue = new LinkedList<MutableTreeNode>();
		queue.add(tree.getRoot());
		while (!queue.isEmpty()) {
			MutableTreeNode tmp = queue.poll();
			Collections.sort(tmp.getChildren());
			queue.addAll(tmp.getChildren());
		}
	}

	private void removalBasedOnDependencyLabels(KBQAQuestion q) {
		for (String depLabel : Lists.newArrayList("auxpass", "aux")) {
			inorderRemovalBasedOnDependencyLabels(q.getTree().getRoot(), q.getTree(), depLabel);
		}
	}

	private boolean inorderRemovalBasedOnDependencyLabels(MutableTreeNode node, MutableTree tree, String depLabel) {
		if (node.depLabel.matches(depLabel)) {
			tree.remove(node);
			return true;
		} else {
			for (Iterator<MutableTreeNode> it = node.getChildren().iterator(); it.hasNext();) {
				MutableTreeNode child = it.next();
				if (inorderRemovalBasedOnDependencyLabels(child, tree, depLabel)) {
					it = node.getChildren().iterator();
				}
			}
			return false;
		}
	}

	private void applyInterrogativeRules(KBQAQuestion q) {
		MutableTreeNode root = q.getTree().getRoot();
		if (root.label.equals("Give")) {
			for (Iterator<MutableTreeNode> it = root.getChildren().iterator(); it.hasNext();) {
				MutableTreeNode next = it.next();
				if (next.label.equals("me")) {
					it.remove();
					q.getTree().remove(root);
				}
			}
		}
		if (root.label.equals("List")) {
			q.getTree().remove(root);
		}
		if (root.label.equals("Give")) {
			q.getTree().remove(root);
		}
	}

	private void removalRules(KBQAQuestion q) {
		MutableTreeNode root = q.getTree().getRoot();
		for (String posTag : Lists.newArrayList(".", "WDT", "POS", "WP\\$", "PRP\\$", "RB", "PRP", "DT", "IN", "PDT")) {
			Queue<MutableTreeNode> queue = Queues.newLinkedBlockingQueue();
			queue.add(root);
			while (!queue.isEmpty()) {
				MutableTreeNode tmp = queue.poll();
				if (tmp.posTag.matches(posTag)) {
					q.getTree().remove(tmp);
				}
				for (MutableTreeNode n : tmp.getChildren()) {
					queue.add(n);
				}
			}
		}
	}
}
