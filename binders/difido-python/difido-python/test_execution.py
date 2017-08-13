'''
Created on Aug 3, 2017

@author: itai
'''
import unittest
from execution import Execution, Machine, Test, Scenario


class TestModel(unittest.TestCase):
    

    def testSimpleModel(self):
        expected = {'machines': [{'status': 'success', 'type': 'machine', 'name': "Itai's desktop", 'children': [{'status': 'success', 'children': [{'status': 'success', 'index': 1, 'uid': '1111', 'parameters': {'tparam0': 'tvalue1', 'tparam1': 'tvalue2'}, 'timestamp': '', 'name': 'Test 0', 'className': '', 'duration': 0, 'type': 'test', 'properties': {'tprop1': 'tvalue1', 'tprop0': 'tvalue0'}, 'description': 'This is my test'}, {'status': 'error', 'index': 2, 'uid': '2222', 'parameters': {}, 'timestamp': '', 'name': 'Test 2', 'className': '', 'duration': 0, 'type': 'test', 'properties': {}, 'description': ''}, {'status': 'failure', 'index': 3, 'uid': '3333', 'parameters': {}, 'timestamp': '', 'name': 'Test 3', 'className': '', 'duration': 0, 'type': 'test', 'properties': {}, 'description': ''}], 'type': 'scenario', 'name': 'Scenario 0', 'scenarioProperties': {'foo0': 'bar0', 'foo1': 'bar1'}}]}]}
        t0 = Test(1, "Test 0", "1111")
        t0.description = "This is my test"
        t0.add_parameter("tparam0", "tvalue1")
        t0.add_parameter("tparam1", "tvalue2")
        t0.add_property("tprop0", "tvalue0")
        t0.add_property("tprop1", "tvalue1")
        t1 = Test(2, "Test 2", "2222")
        t1.set_status("error")
        t2 = Test(3, "Test 3", "3333")
        t2.set_status("failure")
        
        s0 = Scenario("Scenario 0")
        s0.add_scenario_property("foo0", "bar0")
        s0.add_scenario_property("foo1", "bar1")
        s0.add_child(t0)
        s0.add_child(t1)
        s0.add_child(t2)
        
        m0 = Machine("Itai's desktop") 
        m0.add_child(s0)
        
        e = Execution()
        e.add_machine(m0)
        print (e.dict())
        self.assertDictEqual(expected, e.dict())


    def testStatusLevels(self):
        t0 = Test(1, "Test 0", "1111")
        t0.set_status("warning")
        self.assertEqual("warning", t0.status)

        t0.set_status("success")
        self.assertEqual("warning", t0.status)
       
        t0.set_status("failure")
        self.assertEqual("failure", t0.status)

        t0.set_status("warning")
        self.assertEqual("failure", t0.status)
        
        t0.set_status("success")
        self.assertEqual("failure", t0.status)

        t0.set_status("error")
        self.assertEqual("error", t0.status)
        
        t0.set_status("failure")
        self.assertEqual("error", t0.status)
        
        t0.set_status("warning")
        self.assertEqual("error", t0.status)

        t0.set_status("success")
        self.assertEqual("error", t0.status)

    def testNodeChildrenCount(self):
        machine = Machine("aaa")
        self.assertEqual(0, machine.count_children())
        self.assertEqual(False, machine.has_children())
        
        scenario = Scenario("s0")
        machine.add_child(scenario)
        self.assertEqual(1, machine.count_children())
        self.assertEqual(True, machine.has_children())
        


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()