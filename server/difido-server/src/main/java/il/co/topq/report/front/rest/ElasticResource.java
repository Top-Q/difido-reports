package il.co.topq.report.front.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import il.co.topq.report.Common;
import il.co.topq.report.StopWatch;
import il.co.topq.report.business.elastic.ESUtils;
import il.co.topq.report.business.elastic.ElasticsearchTest;

@RestController
@Path("api/elastic")
public class ElasticResource {

	private static final Logger log = LoggerFactory.getLogger(ElasticResource.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ElasticsearchTest[] getTests(@QueryParam("query") String query) {
		log.debug("GET - Qurying Elastic with query '" + query + "'");
		if (null == query){
			log.debug("Recieved null query. Aborting");
			return null;
		}
		List<ElasticsearchTest> tests = null;
		StopWatch stopWatch = new StopWatch(log).start("Qurying Elastic with query '" + query + "'");
		try {
			tests = ESUtils.getAllByQuery(Common.ELASTIC_INDEX, "test", ElasticsearchTest.class, query);
		} catch (Exception e) {
			log.warn("Failed to query using query " + query);
		} finally {
			stopWatch.stopAndLog();
		}
		return tests.toArray(new ElasticsearchTest[]{});
	}

}
