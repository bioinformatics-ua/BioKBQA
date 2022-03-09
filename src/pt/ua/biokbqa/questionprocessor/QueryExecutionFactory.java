package pt.ua.biokbqa.questionprocessor;

public interface QueryExecutionFactory extends QueryExecutionFactoryString, QueryExecutionFactoryQuery, AutoCloseable {
	String getId();
	String getState();
	<T> T unwrap(Class<T> clazz);
}
