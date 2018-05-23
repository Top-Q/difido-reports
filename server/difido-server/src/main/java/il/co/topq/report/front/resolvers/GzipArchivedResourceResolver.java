package il.co.topq.report.front.resolvers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.EncodedResource;
import org.springframework.web.servlet.resource.GzipResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

/**
 * This class extends Spring's GzipResourceResolver adding the functionality to 
 * retrieve a gzipped resource even if an original (unzipped) resource does not exist.
 * e.g. if a request came for myResource.js but we only have myResource.js.gz we will 
 * fetch it. 
 *   
 * @author alik.gershon
 *
 */
public class GzipArchivedResourceResolver extends GzipResourceResolver {
	private final static Logger log = LoggerFactory.getLogger(GzipArchivedResourceResolver.class);
	
	private final static String GZ = ".gz";
	@Override
	protected Resource resolveResourceInternal(HttpServletRequest request, String requestPath,
			List<? extends Resource> locations, ResourceResolverChain chain) {

		//Use the parent's logic for retrieving the resource first (this will include fetching the 
		//Gzip version of the resource if exists); 
		//if we managed to retrieve a resource the usual way (either original or gzipped)
		//- just return it 
		Resource originalResource = super.resolveResourceInternal(request, requestPath, locations, chain);
		try {	
			if (originalResource != null){ 
				return originalResource;
			}


			//a resource was not found, add .gz and try to search down the chain for 
			//the archived resource. 
			String newPath = String.format("%s%s", requestPath,GZ);	
			Resource gzippedResource = chain.resolveResource(request, newPath, locations);

			//No archived resource found, return the original resource (before our intervention). 
			if (gzippedResource == null){
				return originalResource;
			}


			GzipArchivedResource archived = new GzipArchivedResource(gzippedResource); 
			if (archived.exists()){
				return archived;
			}


		}catch (Exception e){
			log.error("Unexpected exception for [" +originalResource.getFilename() + "]",e);
		}
		
		return originalResource;

		
		
	}
	
	/**
	 * This resource wrapper will return the originally requested fileName
	 * stripping off the .gz part. 
	 */
	private static final class GzipArchivedResource extends AbstractResource implements EncodedResource {
		private final Resource archived;
		private String originalFileName;
		
		public GzipArchivedResource(Resource resource) {
			this.archived = resource;
			int fileLength = resource.getFilename().length();
			originalFileName = resource.getFilename().substring(0,fileLength - GZ.length());
			
		}
		
		
		@Override
		public boolean isReadable() {
			return this.archived.isReadable();
		}
		
		@Override
		public boolean isOpen() {
			return this.archived.isOpen();
		}
		
		@Override
		public boolean exists() {
			return this.archived.exists();
		}
		@Override
		public String getDescription() {
			return archived.getDescription();
		}
		
		@Override
		public File getFile() throws IOException {
			return this.archived.getFile();
		}

		@Override
		public URI getURI() throws IOException {
			return this.archived.getURI();
		}
		
		@Override
		public URL getURL() throws IOException {
			return this.archived.getURL();
		}
		
		@Override
		public InputStream getInputStream() throws IOException {
			return archived.getInputStream();
		}

		@Override
		public long lastModified() throws IOException {
			return this.archived.lastModified();
		}		
		
		@Override
		public long contentLength() throws IOException {
			return this.archived.contentLength();
		}
		
		@Override
		public Resource createRelative(String relativePath) throws IOException {
			return this.archived.createRelative(relativePath);
		}
		@Override
		public String getContentEncoding() {
			return "gzip";
		}
		
		@Override
		public String getFilename() {
			return originalFileName;
		}
	}
}
