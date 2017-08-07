'''
Created on Aug 6, 2017

@author: itai
'''
from difidoReport import DifidoReport

class ReportManager(object):
    '''
    classdocs
    '''
    
    def __init__(self):
        self.reporters = []
        self.reporters.append(DifidoReport())


    def start_suite(self,name, attrs):
        for report in self.reporters:
            report.start_suite(name,attrs)
    
    
    def start_keyword(self,name, attrs):
        for report in self.reporters:
            report.start_keyword(name,attrs)
    
    
    def end_keyword(self,name, attrs):
        for report in self.reporters:
            report.end_keyword(name,attrs)

    
    def log_message(self,message):
        for report in self.reporters:
            report.log_message(message)
    
    def message(self, message):
        for report in self.reporters:
            report.message(message)
    
    def end_test(self,name, attrs):
        for report in self.reporters:
            report.end_test(name, attrs)

    def start_test(self,name, attrs):
        for report in self.reporters:
            report.start_test(name, attrs)
    
    def close(self,):
        for report in self.reporters:
            report.close()



    

