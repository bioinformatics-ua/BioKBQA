package pt.ua.biokbqa.nlp;

import java.io.Serializable;
import java.util.List;

public class MutableTree implements Serializable {
	private static final long serialVersionUID = 1286195006804443794L;
	public MutableTreeNode head = null;

	public MutableTreeNode getRoot() {
		return head;
	}

	public boolean remove(MutableTreeNode target) {
		if (target.equals(head)) {
			if (head.children.size() == 1) {
				head = head.children.get(0);
				return true;
			} else {
				return false;
			}
		} else {
			List<MutableTreeNode> children = target.children;
			MutableTreeNode parent = target.parent;
			List<MutableTreeNode> parentsChildren = parent.children;
			parentsChildren.addAll(children);
			for (MutableTreeNode grandchild : children) {
				grandchild.parent = parent;
			}
			parentsChildren.remove(target);
			return true;
		}
	}

	@Override
	public String toString() {
		return TreeTraversal.inorderTraversal(head, 0, null);
	}
}
