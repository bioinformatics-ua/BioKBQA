package pt.ua.biokbqa.questionprocessor;

import org.apache.jena.query.QueryExecution;

public interface QueryExecutionFactoryString {
    QueryExecution createQueryExecution(String queryString);
}
