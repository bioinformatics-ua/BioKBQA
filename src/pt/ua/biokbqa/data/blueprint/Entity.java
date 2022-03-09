package pt.ua.biokbqa.data.blueprint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.jena.rdf.model.Resource;

public class Entity implements Serializable, Comparable<Entity> {
	private static final long serialVersionUID = 7859357081713774767L;
	public String label = "";
	public String type = "";
	public List<Resource> posTypesAndCategories = new ArrayList<>();
	public List<Resource> uris = new ArrayList<>();
	public int offset;

	public Entity(final String label, final String type) {
		this.label = label;
		this.type = type;
	}

	public Entity() {
	}

	public String getLabel() {
		return label;
	}

	public String getType() {
		return type;
	}

	public List<Resource> getPosTypesAndCategories() {
		return posTypesAndCategories;
	}

	public List<Resource> getUris() {
		return uris;
	}

	@Override
	public int compareTo(final Entity o) {
		int thisLength = label.length();
		int otherLength = o.label.length();
		if (thisLength < otherLength) {
			return -1;
		}
		if (thisLength > otherLength) {
			return 1;
		}
		return 0;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entity other = (Entity) obj;
		if (uris.get(0) == null) {
			if (other.uris.get(0) != null)
				return false;
		} else if (!uris.get(0).equals(other.uris.get(0)))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return label + "(" + type + ")";
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(final int offset) {
		this.offset = offset;
	}
}
