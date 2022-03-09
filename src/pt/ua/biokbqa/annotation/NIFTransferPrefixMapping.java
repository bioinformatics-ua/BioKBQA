package pt.ua.biokbqa.annotation;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class NIFTransferPrefixMapping {

	private static final String PREFIX_TO_NS_MAPPING[][] = new String[][] { { "nif", NIF.getURI() },
			{ "itsrdf", ITSRDF.getURI() }, { "rdf", RDF.getURI() }, { "rdfs", RDFS.getURI() },
			{ "xsd", XSDDatatype.XSD + '#' } };

	private static PrefixMapping instance = null;

	public static synchronized PrefixMapping getInstance() {
		if (instance == null) {
			instance = new PrefixMappingImpl();
			for (int i = 0; i < PREFIX_TO_NS_MAPPING.length; ++i) {
				instance.setNsPrefix(PREFIX_TO_NS_MAPPING[i][0], PREFIX_TO_NS_MAPPING[i][1]);
			}
		}
		return instance;
	}
}

