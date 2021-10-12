'''
Created on Aug 10, 2017

@author: Itai Agmon
'''

from execution import Execution, Machine, Scenario, Test
from test_details import ReportElement, TestDetails, ReportElementStatus, ReportElementType
from execution_details import ExecutionDetails
from random import randint
import time
import socket
import local_utils, remote_utils
from datetime import datetime
from configuration import Conf
import os
import logging



class AbstractReport(object):
    
    TIME_FORMAT = '%H:%M:%S:'
    DATE_FORMAT = '%Y/%m/%d'
    ROBOT_FORMAT = '%Y%m%d %H:%M:%S'

    def __init__(self):
        self.general_conf = Conf("general")
        self.init_model()
        self.start()
        self.num_of_suites_to_ignore = self.general_conf.get_int("num.of.suites.to.ignore")
    
    def init_model(self):
        self.execution = Execution()
        machine = Machine(socket.gethostname())
        machine.planned_tests = self.general_conf.get_int("planned.tests")

        self.execution.add_machine(machine)
        self.uid = str(randint(1000, 9999) + time.time() / 1000).replace(".", "")
        self.index = 0
        self.scenario_stack = []
        self.buffered_elements = []
        self.testDetails = None
    
    def start_suite(self, name, attr):
        self.execution.get_last_machine().planned_tests = 0
        if self.num_of_suites_to_ignore > 0:
            self.num_of_suites_to_ignore -= 1
            return 
        self.num_of_suites_to_ignore -= 1
        self.scenario = Scenario(name)
        
        self.scenario.scenarioProperties = self.get_additional_execution_properties()
        if len(self.scenario_stack) != 0:
            self.scenario_stack[-1].add_child(self.scenario)
        else:
            self.execution.get_last_machine().add_child(self.scenario)
            
        self.scenario_stack.append(self.scenario)
        self.write_execution()

    def end_suite(self,name,attrs):
        self.num_of_suites_to_ignore += 1
        if self.num_of_suites_to_ignore > 0:
            return
        if len(self.scenario_stack) != 0:
            self.scenario_stack.pop()
        self.testDetails = None
    
    def end_test(self,name, attrs):
        if len(self.scenario_stack) == 0:
            return
        self.test.set_status(attrs["status"])
        self.test.duration = attrs["elapsedtime"]
        self.write_test_details()
        self.write_execution()
            
    def start_test(self, name, attrs):
        if len(self.scenario_stack) == 0:
            return
        
        self.test_start_time = datetime.strptime(attrs['starttime'], AbstractReport.ROBOT_FORMAT) 

        self.test = Test(self.index, name, self.uid + "_" +str(self.index),attrs['className'])
        self.testDetails = TestDetails(self.uid + "_" +str(self.index))

        self.index += 1
        self.test.timstamp = self.test_start_time.strftime(AbstractReport.TIME_FORMAT)
        self.test.date = self.test_start_time.strftime(AbstractReport.DATE_FORMAT)
        self.test.description = attrs['doc']
        self.scenario.add_child(self.test)
        
        if len(self.buffered_elements) > 0:
            for element in self.buffered_elements:
                self.testDetails.add_element(element)
            self.buffered_elements = []
        
        self.write_execution()
        self.write_test_details()

    def log_message(self,message):
        if len(self.scenario_stack) == 0:
            return
        
        element = ReportElement()
        timestamp = datetime.strptime(message['timestamp'], AbstractReport.ROBOT_FORMAT)
        element.time = timestamp.strftime(AbstractReport.TIME_FORMAT)
        element.title = message['message']
        if message["level"] == ReportElementStatus.FAILURE:
            element.set_status("failure")
            if self.test is not None:
                self.test.set_status("failure")

        self.add_report_element(element)

    def message(self, message):
        pass
    
    def start(self):
        pass
    
    def close(self):
        if self.testDetails is not None:
            self.write_test_details()
        self.write_execution()

    def write_test_details(self):
        pass
    
    def write_execution(self):
        pass
    
    def get_additional_execution_properties(self):
        '''
        return dictionary that will be added as properties to the execution
        '''
        return {}
    
    def add_report_element(self, element):
        if self.testDetails is None:
            self.buffered_elements.append(element)
        else:
            self.testDetails.add_element(element)
            self.write_test_details()

    def add_test_property(self,key,value):
        self.test.add_property(key,value)
        self.write_test_details()

    def add_exeution_property(self, key, value):
        pass


class LocalReport(AbstractReport):
    
    ROBOT_LISTENER_API_VERSION = 2
    
    def start(self):
        local_conf = Conf("local")
        self.log_folder = local_conf.get_string("report.folder")
        if not self.log_folder:
            self.log_folder = os.getcwd() + "/log/"
        if not self.log_folder.endswith("/"):
            self.log_folder += "/"
        local_utils.prepare_template(self.log_folder)
        local_utils.prepare_current_log_folder(self.log_folder)

    def write_test_details(self):
        local_utils.prepare_test_folder(self.log_folder, self.testDetails.uid)
        local_utils.write_test_details_to_file(self.log_folder, self.testDetails)

    def write_execution(self):
        local_utils.write_execution_to_file(self.log_folder, self.execution)


class RemoteReport(AbstractReport):

    ROBOT_LISTENER_API_VERSION = 2
    
    def start(self):
        conf = Conf("remote")
        self.execution_properties = conf.get_dict("execution.properties")
        details = ExecutionDetails()
        details.description = conf.get_string("description")
        details.execution_properties = self.execution_properties
        try: 
            self.execution_id = remote_utils.prepare_remote_execution(details)
            self.enabled = True
        except Exception as e:
            print(e)
            self.enabled = False
            return
        
        machine = self.execution.get_last_machine()
        try: 
            self.machine_id = remote_utils.add_machine(self.execution_id, machine)
        except:
            self.enabled = False
            return
        self.retries = 10

    def get_additional_execution_properties(self):
        return self.execution_properties
        
    def write_execution(self):
        if not self.enabled:
            return
        try:
            remote_utils.update_machine(self.execution_id, self.machine_id, self.execution.get_last_machine())
        except Exception as e:
            print("Exception occurred when trying to update machine: " + str(e))
            self.check_if_disable()

    def write_test_details(self):
        if not self.enabled:
            return
        try:
            remote_utils.add_test_details(self.execution_id, self.testDetails)
        except Exception as e:
            print ("Exception occurred when trying to add test details: " + str(e))
            self.check_if_disable()

    def close(self):
        super(RemoteReport, self).close()
        try:
            remote_utils.end_execution(self.execution_id)
        except:
            pass
    
    def check_if_disable(self):
        self.retries -= 1
        if self.retries <= 0:
            self.enabled = False
            print ("Number of failed request exceeded the maximum allowed. Disabling remote Difido")
        pass

    def add_exeution_property(self, key, value):
        super(RemoteReport, self).add_exeution_property(key, value)
        self.execution_properties[key] = value

    def add_report_element(self, element):
        super(RemoteReport, self).add_report_element(element)
        if element.element_type == ReportElementType.IMAGE:
            remote_utils.add_file(self.execution_id, element.parent.uid, element.message)
            # print("finished add_report_element")


class Console(AbstractReport):
    logging.basicConfig(level=logging.INFO, format='%(message)s')
    log = logging.getLogger()
    logging.getLogger('googleapicliet.discovery_cache').setLevel(logging.CRITICAL)


    def start_suite(self, name, attr):
        super(Console, self).start_suite(name, attr)
        self.log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        self.log.info(time.strftime("%H:%M:%S", time.localtime()) + " :: Start Suite :: " + name)

    def end_suite(self, name, attrs):
        super(Console, self).end_suite(name, attrs)
        self.log.info(time.strftime("%H:%M:%S", time.localtime()) + " :: End Suite ::")

    def end_test(self, name, attrs):
        super(Console, self).end_test(name, attrs)
        self.log.info(time.strftime("%H:%M:%S", time.localtime()) + " :: End Test :: ")
        self.log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")


    def start_test(self, name, attrs):
        super(Console, self).start_test(name, attrs)
        self.log.info( time.strftime("%H:%M:%S", time.localtime()) + " :: Start Test :: " + name)

    def add_report_element(self, element):
        super(Console, self).add_report_element(element)
        self.log.info(element.time + " :: " + element.status + " :: " + element.title + " :: " + element.message)

