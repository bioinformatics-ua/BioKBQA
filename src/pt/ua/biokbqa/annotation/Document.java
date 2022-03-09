package pt.ua.biokbqa.annotation;

import java.util.List;

public abstract interface Document {
	public abstract String getDocumentURI();
	public abstract void setDocumentURI(String paramString);
	public abstract String getText();
	public abstract void setText(String paramString);
	public abstract List<Marking> getMarkings();
	public abstract void setMarkings(List<Marking> paramList);
	public abstract void addMarking(Marking paramMarking);
	public abstract <T extends Marking> List<T> getMarkings(Class<T> paramClass);
}
