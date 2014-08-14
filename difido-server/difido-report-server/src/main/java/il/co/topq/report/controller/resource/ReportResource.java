package il.co.topq.report.controller.resource;

import il.co.topq.report.Common;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.model.ExecutionReport;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Logger;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;

@Path("/reports")
public class ReportResource {

	private static final Logger log = Logger.getLogger(ReportResource.class.getName());

	private static File reportsFolder;

	static {
		reportsFolder = new File(Configuration.INSTANCE.read(ConfigProps.DOC_ROOT_FOLDER) + File.separator
				+ Common.REPORTS_FOLDER_NAME);
	}

	@DELETE
	public void delete() {
		if (null == reportsFolder || !reportsFolder.exists() || !reportsFolder.isDirectory()) {
			return;
		}
		try {
			FileUtils.deleteDirectory(reportsFolder);
		} catch (IOException e) {
			log.warning("Falied to delete log folder due to " + e.getMessage());
		}
		// TODO: Notify that all active executions should be closed.

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ExecutionReport[] get(@PathParam("execution") int execution) {

		if (null == reportsFolder || !reportsFolder.exists() || !reportsFolder.isDirectory()) {
			return new ExecutionReport[] {};
		}
		final File[] executionFolders = reportsFolder.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (name.startsWith(Common.EXECUTION_REPORT_FOLDER_PREFIX)) {
					return true;
				}
				return false;
			}
		});
		if (null == executionFolders || executionFolders.length == 0) {
			return new ExecutionReport[] {};
		}
		final ExecutionReport[] reports = new ExecutionReport[executionFolders.length];
		for (int i = 0; i < executionFolders.length; i++) {
			Date timestamp;
			try {
				timestamp = Common.EXECUTION_REPROT_TIMESTAMP_FORMATTER.parse(executionFolders[i].getName().replace(
						Common.EXECUTION_REPORT_FOLDER_PREFIX + "_", ""));
			} catch (ParseException e) {
				// TODO: report error
				continue;
			}
			ExecutionReport singleReport = new ExecutionReport();
			singleReport.setId(i);
			singleReport.setDate(Common.API_DATE_FORMATTER.format(timestamp));
			singleReport.setTime(Common.API_TIME_FORMATTER.format(timestamp));
			singleReport.setFolderName(executionFolders[i].getName());
			singleReport.setUri(Common.REPORTS_FOLDER_NAME + "/" + executionFolders[i].getName() + "/index.html");
			reports[i] = singleReport;
		}
		return reports;
	}

}
