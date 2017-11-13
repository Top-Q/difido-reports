'''
Created on Aug 10, 2017

@author: itai
'''

class ExecutionDetails(object):
    '''
    classdocs
    '''


    def __init__(self):
        '''
        Constructor
        '''
        self.description = ""
        self.execution_properties = {}
        self.shared = False
        self.force_new = False
        
    def dict(self):
        d = {}
        d['description'] = self.description
        d['executionProperties'] = self.execution_properties
        d['shared'] = self.shared
        d['forceNew'] = self.force_new   
        return d
        