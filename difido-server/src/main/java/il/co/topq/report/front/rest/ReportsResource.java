package il.co.topq.report.front.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.business.execution.MetadataProvider;

@RestController
@Path("api/reports")
public class ReportsResource {

	private static final String ID = "Id";
	private static final String DESCRIPTION = "Description";
	private static final String LINK = "Link";
	private static final String DATE = "Date";
	private static final String TIME = "Time";
	private static final String NUM_OF_TESTS = "# Tests";
	private static final String NUM_OF_SUCCESSFUL = "# Successful";
	private static final String NUM_OF_WARNINGS = "# Warnings";
	private static final String NUM_OF_FAILS = "# Failed";
	private static final String NUM_OF_MACHINES = "# Machines";
	private static final String ACTIVE = "Active";
	private static final String LOCKED = "Locked";

	private static final Logger log = LoggerFactory.getLogger(ExecutionResource.class);

	private final MetadataProvider metadataProvider;
	
	@Autowired
	public ReportsResource(MetadataProvider metadataProvider) {
		this.metadataProvider = metadataProvider;
	}

	/**
	 * Get list of all the reports
	 * 
	 * @param execution
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public DataTable get(@PathParam("execution") int execution) {
		log.debug("GET - Get all reports");
		ExecutionMetadata[] metaData = metadataProvider.getAllMetaData();
		final DataTable table = new DataTable();

		table.headers.add(ID);
		table.headers.add(DESCRIPTION);
		table.headers.add(LINK);
		table.headers.add(DATE);
		table.headers.add(TIME);
		table.headers.add(NUM_OF_TESTS);
		table.headers.add(NUM_OF_SUCCESSFUL);
		table.headers.add(NUM_OF_WARNINGS);
		table.headers.add(NUM_OF_FAILS);
		table.headers.add(NUM_OF_MACHINES);
		table.headers.add(ACTIVE);
		table.headers.add(LOCKED);

		for (ExecutionMetadata meta : metaData) {
			final Map<String, Object> row = new HashMap<String, Object>();
			row.put(ID, meta.getId());
			if (meta.getDescription() != null && !meta.getDescription().isEmpty()){
				row.put(DESCRIPTION, meta.getDescription());
			} else {
				row.put(DESCRIPTION, meta.getFolderName());
			}
			row.put(LINK, meta.getUri());
			row.put(DATE, meta.getDate());
			row.put(TIME, meta.getTime());
			row.put(NUM_OF_TESTS, meta.getNumOfTests());
			row.put(NUM_OF_SUCCESSFUL, meta.getNumOfSuccessfulTests());
			row.put(NUM_OF_WARNINGS, meta.getNumOfTestsWithWarnings());
			row.put(NUM_OF_FAILS, meta.getNumOfFailedTests());
			row.put(NUM_OF_MACHINES, meta.getNumOfMachines());
			row.put(ACTIVE, meta.isActive());
			row.put(LOCKED, meta.isLocked());
			if (meta.getProperties() != null && meta.getProperties().size() > 0) {
				for (String header : meta.getProperties().keySet()) {
					table.headers.add(header);
					row.put(header, meta.getProperties().get(header));
				}
			}
			table.data.add(row);
		}
		return table;
	}

	static class DataTable {
		// Holds the headers of the table. The data structure has to be ordered
		// and not to allow duplications.
		final public Set<String> headers = new LinkedHashSet<String>();
		final public List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
	}

}