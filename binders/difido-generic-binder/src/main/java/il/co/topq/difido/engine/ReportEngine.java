package il.co.topq.difido.engine;

import java.io.File;

import il.co.topq.difido.binder.Binder;

public interface ReportEngine {

	void init(File source, Binder binder);

	void run() throws Exception;

}
