package il.co.topq.report.plugins;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;

/**
 * 
 * @author Itai Agmon
 *
 */
@Component
public class PluginManager {

	private final Logger log = LoggerFactory.getLogger(PluginManager.class);

	private boolean enabled = true;

	private List<Class<? extends Plugin>> pluginClassList;

	public PluginManager() {
		final String pluginClasses = Configuration.INSTANCE.readString(ConfigProps.PLUGIN_CLASSES);
		if (null == pluginClasses || pluginClasses.isEmpty()) {
			log.info("No Plugins were found, plugin manager will be disabled");
			enabled = false;
			return;
		}
		pluginClassList = new ArrayList<Class<? extends Plugin>>();
		for (String pluginClassName : pluginClasses.split(";")) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends Plugin> pluginClass = (Class<? extends Plugin>) Class.forName(pluginClassName, true,
						PluginManager.class.getClassLoader());
				pluginClassList.add(pluginClass);
			} catch (Exception e) {
				log.error("Failed to load plugin class " + pluginClassName + " due to " + e.getMessage());
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Plugin> List<T> getPlugins(Class<T> requiredPluginClass) {
		log.debug("Requested plugin from type " + requiredPluginClass.getName());
		final List<T> result = new ArrayList<T>();
		if (!enabled) {
			return result;
		}
		for (Class<? extends Plugin> pluginClass : pluginClassList) {
			if (requiredPluginClass.isAssignableFrom(pluginClass)) {
				try {
					final T pluginInstance = (T) pluginClass.newInstance();
					if (null == pluginInstance.getName() || pluginInstance.getName().isEmpty()) {
						log.warn("Plugin '" + pluginClass.getName() + "' name is missing");
					}
					log.debug("Plugin from type " + pluginClass.getName() + " with name " + pluginInstance.getName()
							+ " was instanciated successfully");
					result.add(pluginInstance);

				} catch (Exception e) {
					log.error("Failed to create instance of plugin from type " + pluginClass.getName());
				}
			}

		}
		return result;
	}

}
