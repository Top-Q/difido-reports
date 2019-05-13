package il.co.topq.difido.updater;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "Metajson2DB", header = "%n@|green Difido Meta.json file to database file|@")
public class Main implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(Main.class);
	private static final String EXECUTION_FILE_NAME = "meta.json";

	@Option(names = { "-n",
			"--parallel" }, required = false, description = "should the porting done in parallel for better performance")
	private boolean parallel = true;

	@Option(names = { "-m", "--metadata" }, required = false, description = "location of the meta.json file")
	private String metaJsonLocation = EXECUTION_FILE_NAME;

	@Option(names = { "-h", "--help" }, usageHelp = true, description = "display a help message")
    private boolean helpRequested = false;
	
	@Override
	public void run() {
		try {
			new DbCreator().create();
			final JdbcTemplate template = JdbcTemplateBuilder.build();
			final MetaFileReader reader = new MetaFileReader(EXECUTION_FILE_NAME);
			final Map<Integer, OldMetadata> data = reader.read();
			final Worker worker = new Worker(template, data, parallel);
			long start = System.currentTimeMillis();
			worker.work();
			log.info("Finished in " + (System.currentTimeMillis() - start) / 1000 + " seconds.");
		} catch (Exception e) {
			log.error("Failure in porting json to database",e);
		}

	}

	public static void main(String[] args) {
		CommandLine.run(new Main(), args);
	}

}
