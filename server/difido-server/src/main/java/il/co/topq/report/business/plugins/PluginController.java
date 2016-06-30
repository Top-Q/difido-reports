package il.co.topq.report.business.plugins;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import il.co.topq.report.events.ExecutionEndedEvent;
import il.co.topq.report.plugins.ExecutionPlugin;
import il.co.topq.report.plugins.PluginManager;

@Component
public class PluginController {

	private final Logger log = LoggerFactory.getLogger(PluginController.class);

	@Autowired
	private PluginManager pluginManager;

	@EventListener
	public void onExecutionEndedEvent(ExecutionEndedEvent executionEndedEvent) {
		if (executionEndedEvent == null || executionEndedEvent.getMetadata() == null) {
			log.error("Execution ended event was called with null argument");
			return;
		}
		log.debug("Plugin controller was called at the end of execution " + executionEndedEvent.getExecutionId());
		List<ExecutionPlugin> executionPlugins = pluginManager.getPlugins(ExecutionPlugin.class);
		for (ExecutionPlugin plugin : executionPlugins) {
			try {
				log.debug("Calling plugin " + plugin.getName());
				plugin.onExecutionEnded(executionEndedEvent.getMetadata());
			} catch (Exception e) {
				log.error("Failed calling plugin from type " + plugin.getClass().getName() + " with name "
						+ plugin.getName());
			}

		}
	}
}
