package il.co.topq.report.business.upgrade;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MetaFileReader {
	
	private static final Logger log = LoggerFactory.getLogger(MetaFileReader.class);
	
	private final String fileName;

	MetaFileReader(String fileName) {
		super();
		this.fileName = fileName;
	}
	
	Map<Integer, OldMetadata> read(){
		final File metaFile = new File(fileName);
		if (!metaFile.exists()) {
			log.debug("No old meta file found. No need to upgrade");
			return null;
		}
		log.info("Found " + fileName + " . Starting to upgrade by publishing data to databse");
		Map<Integer, OldMetadata> data = null;
		try {
			data = new ObjectMapper().readValue(metaFile, new TypeReference<Map<Integer, OldMetadata>>() {
			});
		} catch (IOException e) {
			log.error("Failed reading old " + null + ". Upgrade process will be aborted", e);
			return null;
		}
		log.info("Found " + data.keySet().size() + " executions");
		return data;
	}
	
	
	
	
}
