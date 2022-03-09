package pt.ua.biokbqa.app;

import java.util.List;
// import pt.ua.biokbqa.benchmark.Evaluation;
// import pt.ua.biokbqa.benchmark.Evaluator;
import pt.ua.biokbqa.data.blueprint.Answer;
import pt.ua.biokbqa.questionprocessor.QueryExecutor;

@javax.ws.rs.Path("/biokbqa")
public class BioKBQA {

	public static void main(String[] args) throws Exception {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tc %2$s%n%4$s: %5$s%6$s%n");
		new pt.ua.biokbqa.conf.ConfLoader().loadConfig("conf.xml");
		org.eclipse.jetty.servlet.ServletContextHandler context = new org.eclipse.jetty.servlet.ServletContextHandler(
				org.eclipse.jetty.servlet.ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		org.eclipse.jetty.server.Server jettyServer = new org.eclipse.jetty.server.Server(51001);
		jettyServer.setHandler(context);
		org.eclipse.jetty.servlet.ServletHolder jerseyServlet = context
				.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
		jerseyServlet.setInitOrder(0);
		jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", BioKBQA.class.getCanonicalName());
		try {
			jettyServer.start();
			jettyServer.join();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jettyServer.isStarted()) {
				jettyServer.destroy();
			}
		}
	}

	// http://localhost:51001/biokbqa/nl/{dataset}/{question}
	@javax.ws.rs.GET
	@javax.ws.rs.Path("/nl/{dataset}/{question}")
	@javax.ws.rs.Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
	public List<Answer> getAnswers(@javax.ws.rs.PathParam("dataset") String dataset,
			@javax.ws.rs.PathParam("question") String question) {
		QueryExecutor searchExecutor = new QueryExecutor();
		return searchExecutor.runPipeline(question);
	}
}
