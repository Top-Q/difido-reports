package il.co.topq.difido;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FileUtils;

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
	 * Gzips the originalFile and returns a zipped one.
	 * @param originalFile
	 * @return
	 */
	public static File gzip(File originalFile) {
		if (originalFile == null || !originalFile.exists())
			return null;
		
	
		File zippedFile = getZippedFile(originalFile);
		if (zippedFile == null)
			return null;
		
		try (FileInputStream input = new FileInputStream(originalFile); 
			 FileOutputStream output = new FileOutputStream(zippedFile);
			 GZIPOutputStream gzipOS = new GZIPOutputStream(output)) {
		
			byte[] buffer = new byte[1024];
			int len;
			while((len= input.read(buffer)) != -1){
				gzipOS.write(buffer, 0, len);
			}

			gzipOS.close();
			output.close();
			input.close();

			return zippedFile;
		}

		catch(Exception e){
			e.printStackTrace();
		}
		
		return originalFile;
		
		
	}

	/**
	 * Since we must preserve the fileName of the originalFile (or browser auto-unzip 
	 * will not work  later, when the resource is requested), so if a file x.y.gz already exists in temp dir
	 * we will have to create a nester dir and place our file there.
	 * 
	 * @param originalFile
	 * @return
	 */
	private static File getZippedFile(File originalFile){
		String tempDir = System.getProperty("java.io.tmpdir");
		String fileName = originalFile.getName().concat(".gz");
		File f = new File(String.format("%s%s",tempDir,fileName));
		if (!f.exists())
			return f;
		
		File nestedDir = new File(String.format("%s%s",tempDir,System.nanoTime()));
		try {
			if (!nestedDir.mkdirs()){
				return null;
			}
			
			
			return new File(nestedDir,fileName);
			
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	
		
		
	}
}
