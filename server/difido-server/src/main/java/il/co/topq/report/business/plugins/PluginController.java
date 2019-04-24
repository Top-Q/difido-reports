package il.co.topq.report.business.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.events.ExecutionEndedEvent;
import il.co.topq.report.events.MachineCreatedEvent;
import il.co.topq.report.persistence.MetadataRepository;
import il.co.topq.report.plugins.ExecutionPlugin;
import il.co.topq.report.plugins.InteractivePlugin;
import il.co.topq.report.plugins.MachineUpdatePlugin;
import il.co.topq.report.plugins.Plugin;
import il.co.topq.report.plugins.PluginManager;

@Component
public class PluginController implements InfoContributor {

	private final Logger log = LoggerFactory.getLogger(PluginController.class);

	private final PluginManager pluginManager;
	
	private final MetadataRepository metadataRepository;
	
	public PluginController(PluginManager pluginManager, MetadataRepository metadataRepository) {
		this.pluginManager = pluginManager;
		this.metadataRepository = metadataRepository;
	}

	@EventListener
	public void onExecutionEndedEvent(ExecutionEndedEvent executionEndedEvent) {
		log.debug("Plugin controller was called at the end of execution " + executionEndedEvent.getExecutionId());
		List<ExecutionPlugin> executionPlugins = pluginManager.getPlugins(ExecutionPlugin.class);
		for (ExecutionPlugin plugin : executionPlugins) {
			try {
				log.debug("Calling plugin " + plugin.getName());
				plugin.onExecutionEnded(metadataRepository.findById(executionEndedEvent.getExecutionId()));
			} catch (Throwable e) {
				log.error("Failed calling plugin from type " + plugin.getClass().getName() + " with name "
						+ plugin.getName(), e);
			}

		}
	}

	@EventListener
	public void onMachineCreatedEvent(MachineCreatedEvent machineCreatedEvent) {
		log.debug("Plugin controller was called at machine created event for execution:"
				+ machineCreatedEvent.getExecutionId());
		List<MachineUpdatePlugin> machineUpdatePlugins = pluginManager.getPlugins(MachineUpdatePlugin.class);
		for (MachineUpdatePlugin plugin : machineUpdatePlugins) {
			try {
				log.debug("Calling plugin " + plugin.getName());
				plugin.onMachineCreated(machineCreatedEvent);
			} catch (Throwable e) {
				log.error("Failed calling plugin from type " + plugin.getClass().getName() + " with name "
						+ plugin.getName(), e);
			}

		}
	}

	/**
	 * Get the names of all the plugins that are configured
	 * 
	 * @return list of plugin names
	 */
	public List<String> getPluginNames() {
		List<String> pluginNames = new ArrayList<String>();
		for (Plugin plugin : pluginManager.getPlugins(Plugin.class)) {
			if (StringUtils.isEmpty(plugin.getName())) {
				continue;
			}
			pluginNames.add(plugin.getName().trim());
		}
		return pluginNames;
	}

	/**
	 * Execute plugin
	 * 
	 * @param pluginName
	 *            The name of the plugin to execute
	 * @param params
	 *            Free parameter for the plugin
	 */
	public void executePlugin(final String pluginName, final List<ExecutionMetadata> metaDataList,
			final String params) {
		if (StringUtils.isEmpty(pluginName)) {
			log.warn("Trying to call plugin with empty name");
			return;
		}
		for (Plugin plugin : pluginManager.getPlugins(Plugin.class)) {
			try {
				if (pluginName.trim().equals(plugin.getName().trim())) {
					log.debug("Calling plugin " + plugin.getName());
					plugin.execute(metaDataList, params);
				}
			} catch (Throwable e) {
				log.error("Failed calling plugin from type " + plugin.getClass().getName() + " with name "
						+ plugin.getName() + " and params " + params, e);
			}

		}

	}

	public String executeInteractivePlugin(final String pluginName, final List<ExecutionMetadata> metaDataList,
			final String params) {
		if (StringUtils.isEmpty(pluginName)) {
			log.warn("Trying to call plugin with empty name");
			return "";
		}
		for (InteractivePlugin plugin : pluginManager.getPlugins(InteractivePlugin.class)) {
			try {
				if (pluginName.trim().equals(plugin.getName().trim())) {
					log.debug("Calling plugin " + plugin.getName());
					return plugin.executeInteractively(metaDataList, params);
				}
			} catch (Throwable e) {
				log.error("Failed calling plugin from type " + plugin.getClass().getName() + " with name "
						+ plugin.getName() + " and params " + params, e);
			}

		}
		return "";

	}

	/**
	 * Info about the server that can be retrieved using the
	 * http://<host>:<port>/info request
	 */
	@Override
	public void contribute(Builder builder) {
		Map<String, String> pluginDetails = new HashMap<>();
		List<Plugin> plugins = pluginManager.getPlugins(Plugin.class);
		pluginDetails.put("number of plugins", plugins.size() + "");
		pluginDetails.put("plugins", plugins.stream().map(p -> p.getName()).collect(Collectors.toList()).toString());
		builder.withDetail("plugin controller", pluginDetails).build();

	}
}
