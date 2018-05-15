package il.co.topq.difido;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.GZIPOutputStream;

public class ZipUtils {

	/**
	 * uncompress all the files in the specified zipFile to the specified
	 * outputFolder. If filter is specified, will include only the file name
	 * that starts with the filter
	 * 
	 * @param zipFile
	 * @param outputFolder
	 * @param filter
	 * @return List of all the extracted files
	 * @throws Exception
	 */
	static List<File> decopmerss(String zipFile, String outputFolder, String filter) throws Exception {
		final List<File> extractedFiles = new ArrayList<File>();
		try (JarFile jar = new JarFile(zipFile)) {
			Enumeration<JarEntry> e = jar.entries();
			while (e.hasMoreElements()) {
				java.util.jar.JarEntry file = (JarEntry) e.nextElement();
				if (!file.getName().startsWith(filter)) {
					continue;
				}
				java.io.File f = new java.io.File(
						outputFolder + java.io.File.separator + file.getName().replaceFirst(filter, ""));
				extractedFiles.add(f);
				if (file.isDirectory()) {
					f.mkdirs();
					continue;
				}
				try (InputStream is = jar.getInputStream(file);
						FileOutputStream fos = new java.io.FileOutputStream(f)) {
					while (is.available() > 0) {
						fos.write(is.read());
					}

				}
			}
		}
		return extractedFiles;

	}

	/**
	 * Gzips the given file and returns the zipped one.
	 * @param originalFile
	 * @return
	 */
	public static File gzip(File originalFile) {
		if (originalFile == null || !originalFile.exists())
			return null;
		
		//if file already zipped, return it;
		if (originalFile.getPath().toLowerCase().endsWith(".gz"))
			return originalFile;
		
		File zippedFile = new File(originalFile.getAbsolutePath().concat(".gz"));
		
		try (FileInputStream input = new FileInputStream(originalFile)){
			try (FileOutputStream output = new FileOutputStream(zippedFile)){
					try (GZIPOutputStream gzipOS = new GZIPOutputStream(output)){
						byte[] buffer = new byte[1024];
			            int len;
			            while((len= input.read(buffer)) != -1){
			                gzipOS.write(buffer, 0, len);
			            }
						
			            gzipOS.close();
			            output.close();
			            input.close();
					}
			}

			return zippedFile;
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return originalFile;
		
		
	}
	
}
