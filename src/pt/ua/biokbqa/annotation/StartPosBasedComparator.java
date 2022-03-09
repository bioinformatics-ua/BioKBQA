package pt.ua.biokbqa.annotation;

import java.util.Comparator;

public class StartPosBasedComparator implements Comparator<Span> {

	@Override
	public int compare(Span a1, Span a2) {
		int diff = a1.getStartPosition() - a2.getStartPosition();
		if (diff < 0) {
			return -1;
		} else if (diff > 0) {
			return 1;
		} else {
			return 0;
		}
	}
}
