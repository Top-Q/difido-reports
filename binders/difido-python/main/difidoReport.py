from execution import Execution, Machine, Scenario, Test
from test_details import ReportElement, TestDetails
from random import randint
import time
import socket
import json
import shutil
import os
from datetime import datetime

class DifidoReport(object):
    
    LOG_FOLDER = '/home/itai/workspaceEricsson/robot_example/log/current'
    
    TIME_FORMAT = '%H:%M:%S:'
    
    DATE_FORMAT = '%Y/%m/%d'
    
    ROBOT_FORMAT = '%Y%m%d %H:%M:%S.%f'
    
    def __init__(self):
        self.init_model()
        self.init_files()
    
    
    def init_model(self):
        self.execution = Execution()
        machine = Machine(socket.gethostname())

        self.execution.add_machine(machine)
        self.uid = str(randint(1000,9999) + time.time() / 1000).replace(".","")
        self.index = 0
        self.scenario_stack = []
    
    def init_files(self):
        shutil.rmtree(DifidoReport.LOG_FOLDER + '/tests',True)
        os.makedirs(DifidoReport.LOG_FOLDER + '/tests')
        try:
            os.remove(DifidoReport.LOG_FOLDER + "/execution.js")
        except OSError:
            pass
        pass
    
    def start_suite(self, name, attr):
        self.scenario = Scenario(name)
        if not self.execution.get_last_machine().has_children():
            self.execution.get_last_machine().add_child(self.scenario)
        
        if len(self.scenario_stack) is not 0:
            self.scenario_stack[-1].add_child(self.scenario)
            
        self.scenario_stack.append(self.scenario)
        
    def start_keyword(self, name, attrs):
        element = ReportElement()
        starttime = datetime.strptime(attrs['starttime'], DifidoReport.ROBOT_FORMAT)
        element.time = starttime.strftime(DifidoReport.TIME_FORMAT)
        element.title = attrs['kwname']
        if len(attrs['args']) is not 0:
            for att in attrs['args']:
                element.title += " " + str(att) 
        element.set_type("startLevel")
        self.testDetails.add_element(element)
        self.write_test_details()
        pass
    
    def end_keyword(self, name, attrs):
        element = ReportElement()
        element.set_type("stopLevel")
        self.testDetails.add_element(element)
        self.write_execution()
        self.write_test_details()
        pass

    
    def end_suite(self,name,attrs):
        if len(self.scenario_stack) is not 0:
            self.scenario_stack.pop()
    
    def end_test(self,name, attrs):
        if attrs["status"] != "PASS":
            self.test.set_status("failure")
        self.test.duration = attrs['elapsedtime']
        self.write_execution()
            
    def start_test(self, name, attrs):
        self.test_start_time = datetime.strptime(attrs['starttime'], DifidoReport.ROBOT_FORMAT) 

        self.test = Test(self.index, name, self.uid + "_" +str(self.index))
        self.testDetails = TestDetails(self.uid + "_" +str(self.index))

        self.index += 1
        self.test.timstamp = self.test_start_time.strftime(DifidoReport.TIME_FORMAT)
        self.test.date = self.test_start_time.strftime(DifidoReport.DATE_FORMAT)
        self.test.description = attrs['doc']
        self.test.add_property("Long name", attrs['longname'])
        self.test.add_property("Robot id", attrs['id'])
        self.test.add_property("critical", str(attrs['critical']))
        tagIndex = 0
        for tag in attrs['tags']:
            self.test.add_property("tag" + str(tagIndex), tag)
            tagIndex += 1
        self.scenario.add_child(self.test)
        
        self.write_execution()
        self.write_test_details()
        
    
    def log_message(self,message):
        element = ReportElement()
        timestamp = datetime.strptime(message['timestamp'], DifidoReport.ROBOT_FORMAT)
        element.time = timestamp.strftime(DifidoReport.TIME_FORMAT)
        element.title = message['message']
        if message["level"] == "FAIL":
            element.set_status("failure")
            self.test.set_status("failure")
        self.testDetails.add_element(element)
        self.write_test_details()

    def message(self, message):
        pass
    
    def close(self):
        self.write_test_details()
        self.write_execution()
    
    def write_test_details(self):
        if self.testDetails is None:
            return
        testfolder = DifidoReport.LOG_FOLDER + '/tests/test_' + self.testDetails.uid
        if not os.path.exists(testfolder):
            os.makedirs(testfolder)
            shutil.copyfile(DifidoReport.LOG_FOLDER + "/test.html", testfolder + "/test.html")

        try:
            os.remove(testfolder + "/test.js")
        except OSError:
            pass
         
        with open(testfolder + '/test.js', "w+") as ex_file:
            ex_file.write("var test = " + json.dumps(self.testDetails.dict()) + ";")

    
    def write_execution(self):
        try:
            os.remove(DifidoReport.LOG_FOLDER + '/execution.js')
        except OSError:
            pass
        with open(DifidoReport.LOG_FOLDER + '/execution.js', "w+") as ex_file:
            ex_file.write("var execution = " + json.dumps(self.execution.dict()) + ";")
