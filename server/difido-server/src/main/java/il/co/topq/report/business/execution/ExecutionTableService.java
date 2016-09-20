package il.co.topq.report.business.execution;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.front.rest.ReportsResource.DataTable;

@Service
public class ExecutionTableService {
	
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
	private static final String[] DEFAULT_HEADERS = new String[]{ID, DESCRIPTION, LINK, DATE, TIME, NUM_OF_TESTS, NUM_OF_SUCCESSFUL,
			NUM_OF_WARNINGS, NUM_OF_FAILS, NUM_OF_MACHINES, ACTIVE, LOCKED};

	public DataTable initTable(ExecutionMetadata[] metaData) {
		DataTable table = new DataTable();	
		String headersStr = Configuration.INSTANCE.readString(ConfigProps.HEADERS);
		List<String> headers = Arrays.asList(headersStr.split(","));
		if (headersStr == null || headersStr.length() == 0 || headers == null || headers.size() == 0) {
			headers = Arrays.asList(DEFAULT_HEADERS);
		}
		for (String header : headers) {
			table.headers.add(header.trim());
		}
		
		for (ExecutionMetadata meta : metaData) {
			final Map<String, Object> row = new HashMap<String, Object>();
			List<String> executionHeaders = new LinkedList<>();
			executionHeaders.addAll(headers);
			if (meta != null && meta.getProperties() != null) {
				executionHeaders.addAll(meta.getProperties().keySet());
			}
			for(String header : executionHeaders) {
				populateRow(table, row, header, meta);
			}
			table.data.add(row);
		}
		return table;
	}

	private void populateRow(DataTable table, Map<String, Object> row, String header, ExecutionMetadata meta) {
		if( ! table.headers.contains(header)) {
			table.headers.add(header);
		}
		if (header.equalsIgnoreCase(ID)) {
			  row.put(ID, meta.getId());
			  return;
		}
		if (header.equalsIgnoreCase(DESCRIPTION)) {
			if (meta.getDescription() != null && !meta.getDescription().isEmpty()){
				row.put(DESCRIPTION, meta.getDescription());
			} else {
				row.put(DESCRIPTION, meta.getFolderName());
			}
			return;
		}
		if ( header.equalsIgnoreCase(LINK)) {
			row.put(LINK, meta.getUri());
			return;
		}
		if ( header.equalsIgnoreCase(DATE)) {
			row.put(DATE, meta.getDate());
			return;
		}
		if ( header.equalsIgnoreCase(TIME)) {
			row.put(TIME, meta.getTime());
			return;
		}
		if ( header.equalsIgnoreCase(NUM_OF_TESTS)) {
			row.put(NUM_OF_TESTS, meta.getNumOfTests());
			return;
		}
		if ( header.equalsIgnoreCase(NUM_OF_SUCCESSFUL)) {
			row.put(NUM_OF_SUCCESSFUL, meta.getNumOfSuccessfulTests());
			return;
		}
		if ( header.equalsIgnoreCase(NUM_OF_WARNINGS)) {
			row.put(NUM_OF_WARNINGS, meta.getNumOfTestsWithWarnings());
			return;
		}
		if ( header.equalsIgnoreCase(NUM_OF_FAILS)) {
			row.put(NUM_OF_FAILS, meta.getNumOfFailedTests());
			return;
		}
		if ( header.equalsIgnoreCase(NUM_OF_MACHINES)) {
			row.put(NUM_OF_MACHINES, meta.getNumOfMachines());
			return;
		}
		if ( header.equalsIgnoreCase(ACTIVE)) {
			row.put(ACTIVE, meta.isActive());
		    return;
		}	
		if ( header.equalsIgnoreCase(LOCKED)) {
			row.put(LOCKED, meta.isLocked());
			return;
		}
		if ( meta == null || meta.getProperties() == null) {
			return;
		}
		String value = meta.getProperties().get(header);
		row.put(header, value != null ? value : "");
	}

}
