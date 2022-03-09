package pt.ua.biokbqa.annotation;

public abstract interface ScoredMarking extends Marking {
	public abstract double getConfidence();
	public abstract void setConfidence(double paramDouble);
}
