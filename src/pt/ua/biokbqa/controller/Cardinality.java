package pt.ua.biokbqa.controller;

import pt.ua.biokbqa.data.blueprint.KBQAQuestion;
import pt.ua.biokbqa.nlp.MutableTreeNode;

public class Cardinality {

	public int cardinality(KBQAQuestion q) {
		int cardinality = 12;
		MutableTreeNode root = q.getTree().getRoot();
		if (root.posTag.matches("VB(.)*")) {
			MutableTreeNode firstChild = root.children.get(0);
			String posTag = firstChild.posTag;
			if (posTag.equals("NNS")) {
				cardinality = 12;
			} else if (posTag.matches("WP||WRB||ADD||NN||VBZ||IN")) {
				cardinality = 1;
			} else if (posTag.matches("IN")) {
				MutableTreeNode secondChild = firstChild.getChildren().get(0);
				posTag = secondChild.posTag;
				if (posTag.equals("NN")) {
					cardinality = 1;
				} else {
					cardinality = 12;
				}
			} else {
				cardinality = 12;
			}
		} else {
			String posTag = root.posTag;
			if (posTag.matches("NNS||NNP(.)*")) {
				cardinality = 12;
			} else {
				cardinality = 1;
			}
		}
		return cardinality;
	}
}
