package il.co.topq.report.front.rest;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.stereotype.Component;

import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;

@Component
public class JerseyConfig extends ResourceConfig {
	public JerseyConfig() {
		registerEndpoints();
	}
	

	private void registerEndpoints() {
		if (!Configuration.INSTANCE.readBoolean(ConfigProps.ARCHIVER_ENABLED)){
			register(ExecutionResource.class);
			register(MachineResource.class);
			register(TestDetailsResource.class);
			
		}
		register(ReportsResource.class);
		register(PluginResource.class);
		register(SettingsResource.class);

		// This is important if we want the server to serve also static content
		property(ServletProperties.FILTER_FORWARD_ON_404, true);
	}
}