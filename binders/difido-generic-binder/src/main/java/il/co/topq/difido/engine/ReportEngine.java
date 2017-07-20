package il.co.topq.difido.engine;

import il.co.topq.difido.binder.Binder;

public interface ReportEngine {

	void init(Binder binder);

	void run() throws Exception;

}
