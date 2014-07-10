package il.co.topq.report;

import static org.junit.Assert.assertEquals;
import il.co.topq.difido.model.execution.MachineNode;

import org.junit.Test;

public class EndToEndTests extends AbstractResourceTestCase {
	
	@Test
	public void createSimpleReport(){
		String machine2Name = "Machine #2";
		String machine1Name = "Machine #1";
    	
    	int executionId = addExecution();
    	assertEquals(0, executionId);
    	
    	int machineId = addMachine(executionId, machine1Name);
    	assertEquals(0, machineId);
    	
    	machineId = addMachine(executionId, machine2Name);
    	assertEquals(1, machineId);
    	
    	MachineNode machine = getMachine(executionId, machineId);
    	assertEquals(machine2Name, machine.getName());
	}
	
}
