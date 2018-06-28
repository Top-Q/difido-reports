package il.co.topq.report.front;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.PathResourceResolver;

import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.front.resolvers.GzipArchivedResourceResolver;

@Configuration
public class StaticResourceConfiguration extends WebMvcConfigurerAdapter {
	private final Logger log = LoggerFactory.getLogger(StaticResourceConfiguration.class);

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String docRoot = il.co.topq.report.Configuration.INSTANCE.readString(ConfigProps.DOC_ROOT_FOLDER);
		if (null == docRoot || docRoot.isEmpty()) {
			docRoot = "docRoot/";
		}
		if (!docRoot.endsWith("/")) {
			docRoot += "/";
		}
		log.debug("docRoot folder is set to " + docRoot);

		ResourceHandlerRegistration resourceHandlerRegistration = registry.addResourceHandler("/**")
				.addResourceLocations("file:" + docRoot);

		boolean enableArchivedResources = il.co.topq.report.Configuration.INSTANCE
				.readBoolean(ConfigProps.ENABLE_ARCHIVED_RESOURCES);
		log.debug("enableArchivedResources={}", enableArchivedResources);
		if (enableArchivedResources) {
			// many of our resources are dynamic
			// so we probably don't want to cache them.
			resourceHandlerRegistration.resourceChain(false).addResolver(new GzipArchivedResourceResolver())
					.addResolver(new PathResourceResolver());
		}

	}

	/**
	 * This is important if we want to forward the 'index.html' file by default
	 * when user is browsing to '/'
	 */
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("forward:/index.html");
	}
}