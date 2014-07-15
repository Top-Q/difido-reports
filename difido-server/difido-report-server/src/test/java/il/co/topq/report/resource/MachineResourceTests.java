package il.co.topq.report.resource;

import static org.junit.Assert.assertEquals;
import il.co.topq.difido.model.execution.MachineNode;

import org.junit.Test;

public class MachineResourceTests extends AbstractResourceTestCase {

    @Test
    public void testMachineResource() {
    	
    	String machine1Name = "Machine #1";
    	String machine2Name = "Machine #2";
    	
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
