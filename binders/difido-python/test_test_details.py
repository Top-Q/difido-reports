'''
Created on Aug 10, 2017

@author: Itai Agmon
'''

from execution import Execution, Machine, Test, Scenario
from test_details import TestDetails, ReportElement
import unittest
import json



class TestTestDetails(unittest.TestCase):
    
    def testLevelStatuses(self):
        details = TestDetails("111")
        
        element = ReportElement()
        element.title = "Starting level"
        element.set_type("startLevel")
        
        details.add_element(element)
        
        element = ReportElement()
        element.title = "element that fails"
        element.set_status("failure")
        
        details.add_element(element)
        
        element = ReportElement()
        element.set_type("stopLevel")
        
        details.add_element(element)
        
        self.assertEqual("failure",details.report_elements[0].status) 
    
    
    def testBasicMessages(self):
        expected = {'uid': '111', 'reportElements': [{'status': 'success', 'message': '', 'time': '', 'title': 'simple success title'}]}
        details = TestDetails("111")
        element = ReportElement()
        element.title = "simple success title"
        details.add_element(element)
        self.assertDictEqual(expected, details.dict())
        

    def testTestDetailsWithExecution(self):
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
        print(json.dumps(e.dict()))
        


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()