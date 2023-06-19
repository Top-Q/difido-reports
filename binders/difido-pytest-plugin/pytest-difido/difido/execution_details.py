'''
Created on Aug 10, 2017

@author: Itai Agmon
'''


class ExecutionDetails:
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
        return {'description': self.description,
                'executionProperties': self.execution_properties,
                'shared': self.shared,
                'forceNew': self.force_new}
