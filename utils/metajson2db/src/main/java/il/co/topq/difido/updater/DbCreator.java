package il.co.topq.difido.updater;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbCreator {

	private static final Logger log = LoggerFactory.getLogger(DbCreator.class);

	private static final String DATABASE_NAME = "database.mv.db";

	public void create() throws Exception {
		final File dbFile = new File(DATABASE_NAME);
		if (dbFile.exists()) {
			log.info("Found old database file. Deleting");
			dbFile.delete();
		}
		if (resourceToBinaryFile("/" + DATABASE_NAME, ".", this)) {
			log.info("Finished creating clean database file");
		} else {
			log.error("Failed creating new database file");
			throw new IOException("Failed creating new database file");
		}
	}

	private static boolean resourceToBinaryFile(String resourceName, String targetDirectory, Object resource)
			throws Exception {
		boolean found = false;
		if (null == resourceName) {
			return found;
		}
		try (InputStream is = resource.getClass().getResourceAsStream(resourceName);
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				FileOutputStream fos = new FileOutputStream(targetDirectory + File.separator
						+ resourceName.substring(resourceName.lastIndexOf('/'), resourceName.length()))) {
			byte[] buffer = new byte[1024];
			int bytesRead;

			while ((bytesRead = is.read(buffer)) != -1) {
				fos.write(buffer, 0, bytesRead);
			}
			fos.flush();
			found = true;
		}
		return found;
	}

	public static void main(String[] args) throws Exception {
		DbCreator c = new DbCreator();
		c.create();
	}

}
