package il.co.topq.difido.retriever;

import java.io.File;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "Metadata Retriever", header = "%n@|green Difido metadata retriever|@")
public class Main implements Runnable {

	private static final String META_FILE_NAME = "meta.json";

	@Option(names = {"-d", "--docroot"}, required = true, description = "The Difido server docRoot folder with the report folders")
	private File docRootFolder;

	@Option(names = {"-m", "--metadata"}, required = false, description = "Destination folder of the generated metadata file (meta.json). default is the current folder")
	private File metaFolder;

	public void run() {
		File metaFile = null;
		if (null == metaFolder || !metaFolder.exists()) {
			metaFile = new File(META_FILE_NAME);
		} else {
			metaFile = new File(metaFolder,META_FILE_NAME);
		}
		Retriever retriever = new Retriever(docRootFolder, metaFile);
		retriever.retrieve();
	}

	public static void main(String[] args) {
		CommandLine.run(new Main(), args);
	}

}
