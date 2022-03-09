package pt.ua.biokbqa.questionprocessor;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

public interface QueryExecutionFactoryQuery {
    QueryExecution createQueryExecution(Query query);
}