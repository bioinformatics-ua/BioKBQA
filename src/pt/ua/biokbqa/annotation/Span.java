package pt.ua.biokbqa.annotation;

public interface Span extends Marking {
	public int getStartPosition();
	public void setStartPosition(int startPosition);
	public int getLength();
	public void setLength(int length);
}
