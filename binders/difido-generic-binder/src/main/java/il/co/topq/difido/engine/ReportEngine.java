package il.co.topq.difido.engine;

import il.co.topq.difido.binders.Binder;

public interface ReportEngine {

	void init(Binder binder);

	void run() throws Exception;

}
