package il.co.topq.report.plugins;

import il.co.topq.report.events.MachineCreatedEvent;

public interface MachineUpdatePlugin extends Plugin{

	void onMachineCreated(MachineCreatedEvent machineCreatedEvent);

}
