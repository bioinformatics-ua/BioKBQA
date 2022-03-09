package pt.ua.biokbqa.annotation;

import java.util.Set;

public abstract interface TypedMarking extends Marking {
	public abstract Set<String> getTypes();
	public abstract void setTypes(Set<String> paramSet);
}
