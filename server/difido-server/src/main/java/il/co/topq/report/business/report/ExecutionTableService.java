package il.co.topq.report.business.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import il.co.topq.difido.DateTimeConverter;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.StopWatch;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.front.rest.ReportsResource.DataTable;
import il.co.topq.report.persistence.ExecutionState;

@Service
public class ExecutionTableService {

	private static final Logger log = LoggerFactory.getLogger(ExecutionTableService.class);

	private static final String ID = "ID";
	private static final String DESCRIPTION = "Description";
	private static final String LINK = "Link";
	private static final String SERIAL = "Serial";
	private static final String DATE = "Date";
	private static final String TIME = "Time";
	private static final String DURATION = "Duration";
	private static final String NUM_OF_TESTS = "# Tests";
	private static final String NUM_OF_SUCCESSFUL = "# Successful";
	private static final String NUM_OF_WARNINGS = "# Warnings";
	private static final String NUM_OF_FAILS = "# Failed";
	private static final String NUM_OF_MACHINES = "# Machines";
	private static final String ACTIVE = "Active";
	private static final String LOCKED = "Locked";
	private static final String[] DEFAULT_HEADERS = new String[] { ID, DESCRIPTION, LINK, DATE, TIME, DURATION,
			NUM_OF_TESTS, NUM_OF_SUCCESSFUL, NUM_OF_WARNINGS, NUM_OF_FAILS, NUM_OF_MACHINES, ACTIVE, LOCKED };

	public DataTable initTable(List<ExecutionMetadata> metaData, List<ExecutionState> stateList) {
		DataTable table = new DataTable();
		List<String> headers;
		if (!Configuration.INSTANCE.readList(ConfigProps.EXECUTION_TABLE_HEADERS).isEmpty()) {
			headers = new ArrayList<String>();
			// There are some columns that we always want to display. And also,
			// it is really hard to to handle the 'description' column when it
			// is too dynamic in the client side.
			headers.add(ID);
			headers.add(DESCRIPTION);
			headers.add(LINK);
			for (String desiredHeader : Configuration.INSTANCE.readList(ConfigProps.EXECUTION_TABLE_HEADERS)) {
				desiredHeader = desiredHeader.trim();
				if (desiredHeader.equals(ID) || desiredHeader.equals(DESCRIPTION) || desiredHeader.equals(LINK)) {
					// We already added those headers.
					continue;
				}
				headers.add(desiredHeader);
			}
		} else {
			headers = new LinkedList<String>(Arrays.asList(DEFAULT_HEADERS));
			for (ExecutionMetadata meta : metaData) {
				if (null == meta) {
					continue;
				}
				if (null == meta.getProperties()) {
					continue;
				}
				for (String header : meta.getProperties().keySet()) {
					if (!headers.contains(header)) {
						headers.add(header);
					}
				}
			}
		}

		for (String header : headers) {
			table.columns.add(header.trim());
		}

		StopWatch stopWatch = new StopWatch(log).start("Populating rows");
		Map<Integer, ExecutionState> idToState = new HashMap<>();
		for (ExecutionState state : stateList) {
			idToState.put(state.getId(), state);
		}
		for (ExecutionMetadata meta : metaData) {
			final List<String> row = new ArrayList<>();
			for (String header : headers) {
				populateRow(table, row, header, meta, idToState.get(meta.getId()));
			}
			table.data.add(row);
		}
		stopWatch.stopAndLog();
		return table;
	}

	private void populateRow(DataTable table, List<String> row, String header, ExecutionMetadata meta,
			ExecutionState state) {
		if (!table.columns.contains(header)) {
			table.columns.add(header);
		}
		if (header.equalsIgnoreCase(ID)) {
			row.add("" + meta.getId());
			return;
		}
		if (header.equalsIgnoreCase(DESCRIPTION)) {
			if (meta.getDescription() != null && !meta.getDescription().isEmpty()) {
				row.add(meta.getDescription());
			} else {
				row.add(meta.getFolderName());
			}
			return;
		}
		if (header.equalsIgnoreCase(SERIAL)) {
			row.add(meta.getSerialNum());
			return;
		}
		if (header.equalsIgnoreCase(LINK)) {
			row.add(meta.getUri());
			return;
		}
		if (header.equalsIgnoreCase(DATE)) {
			row.add(DateTimeConverter.fromDateObject(meta.getTimestamp()).toDateString());
			return;
		}
		if (header.equalsIgnoreCase(TIME)) {
			row.add(DateTimeConverter.fromDateObject(meta.getTimestamp()).toTimeString());
			return;
		}
		if (header.equalsIgnoreCase(NUM_OF_TESTS)) {
			row.add("" + meta.getNumOfTests());
			return;
		}
		if (header.equalsIgnoreCase(NUM_OF_SUCCESSFUL)) {
			row.add("" + meta.getNumOfSuccessfulTests());
			return;
		}
		if (header.equalsIgnoreCase(NUM_OF_WARNINGS)) {
			row.add("" + meta.getNumOfTestsWithWarnings());
			return;
		}
		if (header.equalsIgnoreCase(NUM_OF_FAILS)) {
			row.add("" + meta.getNumOfFailedTests());
			return;
		}
		if (header.equalsIgnoreCase(NUM_OF_MACHINES)) {
			row.add("" + meta.getNumOfMachines());
			return;
		}
		if (header.equalsIgnoreCase(DURATION)) {
			if (meta.getDuration() == 0) {
				row.add("");
				return;
			}
			long durInSec = Math.round(meta.getDuration() / 1000);
			int days = 0;
			if(durInSec > 86400) {
				days = (int)(durInSec / 60 / 60 / 24);
			}
			long durationHour = (long) Math.floor(((durInSec % 31536000) % 86400) / 3600);
			long durationMin = (long) Math.floor((((durInSec % 31536000) % 86400) % 3600) / 60);
			long durationSec = (((durInSec % 31536000) % 86400) % 3600) % 60;
			if(days > 0) {
				row.add(days + "d" + durationHour + "h" + durationMin + "m" + durationSec + "s");
			} else {
				row.add(durationHour + "h" + durationMin + "m" + durationSec + "s");
			}
			return;
		}
		if (state != null) {
			if (header.equalsIgnoreCase(ACTIVE)) {
				row.add(state.isActive() + "");
				return;
			}
			if (header.equalsIgnoreCase(LOCKED)) {
				row.add(state.isLocked() + "");
				return;
			}

		} else {
			if (header.equalsIgnoreCase(ACTIVE)) {
				row.add(false + "");
				return;
			}
			if (header.equalsIgnoreCase(LOCKED)) {
				row.add(false + "");
				return;
			}

		}
		if (meta == null || meta.getProperties() == null) {
			return;
		}
		String value = meta.getProperties().get(header);
		row.add(value != null ? value : "");
	}

}
