package il.co.topq.difido;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import il.co.topq.difido.binder.Binder;
import il.co.topq.difido.config.GenericBinderConfig;
import il.co.topq.difido.config.GenericBinderConfig.BinderOptions;
import il.co.topq.difido.engine.LocalReportEngine;
import il.co.topq.difido.engine.RemoteReportEngine;
import il.co.topq.difido.engine.ReportEngine;

public class Main {

	private static final String PLUGIN_FOLDER_NAME = "plugin";

	private Logger log = LoggerFactory.getLogger(Main.class);

	private GenericBinderConfig config = new GenericBinderConfig();

	public void run() {
		addPluginsFromPluginFolder();
		Binder binder = null;
		try {
			binder = createBinderInstance(config.getPropertyAsString(BinderOptions.BINDER_CLASS));

		} catch (Exception e) {
			log.error("Failed to create binder for class " + config.getPropertyAsString(BinderOptions.BINDER_CLASS)
					+ " due to " + e.getMessage());
			return;
		}
		final File source = new File(config.getPropertyAsString(BinderOptions.SOURCE));
		if (!source.exists()) {
			log.error("Source is not exists " + source.getAbsolutePath());
			return;
		}
		if (config.getPropertyAsBoolean(BinderOptions.LOCAL_DIFIDO_ENABLED)) {
			log.info("Running local engine");
			final File destinationFolder = new File(config.getPropertyAsString(BinderOptions.DESTINATION_FOLDER));
			runLocalEngine(binder, source, destinationFolder);
		}
		if (config.getPropertyAsBoolean(BinderOptions.REMOTE_DIFIDO_ENABLED)) {
			log.info("Running remote engine");
			final String host = config.getPropertyAsString(BinderOptions.REMOTE_DIFIDO_HOST);
			final int port = config.getPropertyAsInt(BinderOptions.REMOTE_DIFIDO_PORT);
			final String description = config.getPropertyAsString(BinderOptions.REMOTE_EXECUTION_DESCRIPTION);
			runRemoteRngine(binder,source,host,port, description);
		}

	}

	private void addPluginsFromPluginFolder() {
		File pluginFolder = new File(PLUGIN_FOLDER_NAME);
		if (!pluginFolder.exists()) {
			if (!pluginFolder.mkdirs()) {
				log.error("Failed to create pluging folder " + pluginFolder.getAbsolutePath());
			}
			return;
		}
		try {
			File[] jarFiles = pluginFolder.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(".jar")){
						return true;
					}
					return false;
				}
			});
			for (File jar : jarFiles){
				addPath(jar.getAbsolutePath());
			}
		} catch (Exception e) {
			log.error("Failed to add plugin folder " + PLUGIN_FOLDER_NAME + " to the classpath");
		}
	}

	private void runRemoteRngine(Binder binder, File source, String host, int port, String description) {
		ReportEngine engine = new RemoteReportEngine(host,port,description);
		engine.init(source, binder);
		try {
			engine.run();
		} catch (Exception e) {
			log.error("Failed to run local remote engine due to " + e.getMessage());
		}
	}

	private void runLocalEngine(Binder binder, final File source, final File destinationFolder) {
		ReportEngine engine = new LocalReportEngine(destinationFolder);
		engine.init(source, binder);
		try {
			engine.run();
		} catch (Exception e) {
			log.error("Failed to run local report engine due to " + e.getMessage());
		}
	}

	private Binder createBinderInstance(String className) throws Exception {
		Class<?> clazz = Class.forName(className);
		Constructor<?> constructor = clazz.getConstructor();
		Binder binder = (Binder) constructor.newInstance();
		log.debug("Successfully create binder for class " + className);
		return binder;
	}

	// need to do add path to Classpath with reflection since the
	// URLClassLoader.addURL(URL url) method is protected:
	public static void addPath(String s) throws Exception {
		File f = new File(s);
		URI u = f.toURI();
		URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class<URLClassLoader> urlClass = URLClassLoader.class;
		Method method = urlClass.getDeclaredMethod("addURL", new Class[] { URL.class });
		method.setAccessible(true);
		method.invoke(urlClassLoader, new Object[] { u.toURL() });
	}

	public static void main(String[] args) throws Exception {
		Main main = new Main();
		main.run();
	}
}
