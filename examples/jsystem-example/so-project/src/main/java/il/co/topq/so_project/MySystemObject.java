package il.co.topq.so_project;

import jsystem.extensions.analyzers.text.FindText;
import jsystem.framework.system.SystemObjectImpl;

import org.junit.Assert;

import com.aqua.sysobj.conn.CliCommand;
import com.aqua.sysobj.conn.CliConnectionImpl;

public class MySystemObject extends SystemObjectImpl {

	/**
	 * System objects can be inside composite inside other system objects.<br>
	 * Just make sure that the modifier of the system object member is public so
	 * it can exposed to the SUT <br>
	 * Notice that the concrete class of the cliConnection is defined in the SUT
	 * file and the instantiation is done by JSystem.
	 */
	public CliConnectionImpl cliConnection;

	private String path;

	/**
	 * The init() method will be called by JSystem after the instantiation of
	 * the system object. <br>
	 * This can be a good place to assert that all the members that we need were
	 * defined in the SUT file.
	 */
	public void init() throws Exception {
		super.init();
		Assert.assertNotNull("Please define the path member in the SUT file", path);

	}

	/**
	 * The close method is called in the end of the while execution.<br>
	 * This can be a good place to free resources.<br>
	 */
	public void close() {
		super.close();
	}

	/**
	 * Assert that the specified file exists in the path.
	 * 
	 * @param fileName
	 *            file name to assert that exists in the folder.
	 * @throws Exception When file doesn't exist
	 */
	public void assertFileExists(final String fileName) throws Exception {
		CliCommand command = new CliCommand("dir");
		command.addAnalyzers(new FindText(fileName));
		cliConnection.handleCliCommand("List folder", command);

	}

	public String getPath() {
		return path;
	}

	/**
	 * Other members of the system objects can also be exposed to the SUT.
	 * 
	 * @param path Folder to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

}
