package il.co.topq.report.business.archiver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FilenameUtils;

public class UnzipUtility {
	/**
	 * Size of the buffer to read/write data
	 */
	private static final int BUFFER_SIZE = 4096;

	/**
	 * Extracts a zip file specified by the zipFilePath to a directory specified by
	 * destDirectory (will be created if does not exists)
	 * 
	 * @param zipFilePath
	 * @param destDirectory
	 * @throws IOException
	 */
	public void unzip(String zipFilePath, String destDirectory) throws IOException {
		File destDir = new File(destDirectory, FilenameUtils.getBaseName(zipFilePath));
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
			ZipEntry entry = zipIn.getNextEntry();
			// iterates over entries in the zip file
			while (entry != null) {
				String filePath = destDirectory + File.separator + FilenameUtils.getBaseName(zipFilePath)
						+ File.separator + entry.getName();
				if (!entry.isDirectory()) {
					// if the entry is a file, extracts it
					extractFile(zipIn, filePath);
				} else {
					// if the entry is a directory, make the directory
					File dir = new File(filePath);
					dir.mkdir();
				}
				zipIn.closeEntry();
				entry = zipIn.getNextEntry();
			}
		}
	}

	/**
	 * Extracts a zip entry (file entry)
	 * 
	 * @param zipIn
	 * @param filePath
	 * @throws IOException
	 */
	private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		byte[] bytesIn = new byte[BUFFER_SIZE];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
	}
}