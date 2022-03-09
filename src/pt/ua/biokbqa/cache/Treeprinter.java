package pt.ua.biokbqa.cache;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import pt.ua.biokbqa.data.blueprint.KBQAQuestion;

public class Treeprinter {
	public boolean ready;
	BufferedWriter bwStanford;
	BufferedWriter bwClearnlp;

	public Treeprinter() {
		ready = initialize();
	}

	public String printTreeStanford(KBQAQuestion q) {

		String treeString = q.getTree().toString();
		if (ready) {
			try {
				bwStanford.write(q.getLanguageToQuestion().toString());
				bwStanford.newLine();
				bwStanford.write(treeString);
				bwStanford.newLine();
				bwStanford.newLine();
				bwStanford.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Printer not ready!");
		}
		return treeString;
	}

	public String printTreeStanford(KBQAQuestion q, String s) {
		String treeString = s;
		if (ready) {
			try {
				bwStanford.write(q.getLanguageToQuestion().toString());
				bwStanford.newLine();
				bwStanford.write(treeString);
				bwStanford.newLine();
				bwStanford.newLine();
				bwStanford.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Printer not ready!");
		}
		return treeString;
	}

	public String printTreeClearnlp(KBQAQuestion q) {
		String treeString = q.getTree().toString();
		if (ready) {
			try {
				bwClearnlp.write(q.getLanguageToQuestion().toString());
				bwClearnlp.newLine();
				bwClearnlp.write(q.getTree().toString());
				bwClearnlp.newLine();
				bwClearnlp.newLine();
				bwClearnlp.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Printer not ready!");
		}
		return treeString;
	}

	boolean initialize() {
		try {
			SimpleDateFormat sdfDate = new SimpleDateFormat("dd.MM_HH.mm");
			Date now = new Date();
			String strDate = sdfDate.format(now);
			bwStanford = new BufferedWriter(new FileWriter("stanford" + strDate + ".txt", true));
			bwClearnlp = new BufferedWriter(new FileWriter("clearnlp" + strDate + ".txt", true));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void close() {
		closeStanford();
		closeClearnlp();
	}

	public void closeStanford() {
		if (bwStanford != null)
			try {
				bwStanford.close();
			} catch (Exception e) {
			}
	}

	public void closeClearnlp() {
		if (bwClearnlp != null)
			try {
				bwClearnlp.close();
			} catch (Exception e) {
			}
	}
}
