package pt.ua.biokbqa.annotation;

public interface ScoredMeaning extends Meaning {
	public double getConfidence();
	public void setConfidence(double confidence);
}
