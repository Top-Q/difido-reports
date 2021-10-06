'''
Created on Aug 10, 2017

@author: Itai Agmon
'''

class ReportElementType():
    REGULAR = "regular"
    LINK = "lnk"
    IMAGE = "img"
    HTML = "html"
    STEP = "step"
    START_LEVEL = "startLevel"
    STOP_LEVEL = "stopLevel"

class ReportElementStatus():
    SUCCESS = "success"
    WARNING = "warning"
    FAILURE = "failure"
    ERROR   = "error"

class ReportElement(object):
    
    def __init__(self):
        self.parent = None
        self.title = ""
        self.message = ""
        self.status = ReportElementStatus.SUCCESS
        self.time = ""
        self.element_type = ReportElementType.REGULAR
    
    
    def set_status(self, status):
        if status != ReportElementStatus.ERROR and \
                status != ReportElementStatus.FAILURE and \
                status != ReportElementStatus.WARNING and \
                status != ReportElementStatus.SUCCESS:
            raise ValueError("Illegal status %s" % status)
        if status == ReportElementStatus.ERROR:
            self.status = status
        elif status == ReportElementStatus.FAILURE:
            if self.status != ReportElementStatus.ERROR:
                self.status = status
        elif status == ReportElementStatus.WARNING:
            if self.status != ReportElementStatus.ERROR and self.status != ReportElementStatus.FAILURE:
                self.status = status
        
    def set_type(self, element_type):
        if element_type != ReportElementType.REGULAR and \
                element_type != ReportElementType.LINK and \
                element_type != ReportElementType.IMAGE and \
                element_type != ReportElementType.HTML and \
                element_type != ReportElementType.STEP and \
                element_type != ReportElementType.START_LEVEL and \
                element_type != ReportElementType.STOP_LEVEL:
            raise ValueError("Illegal element type %s" % element_type)
        self.element_type = element_type 
    
    def dict(self):
        d = {}
        d["title"] = self.title
        d["message"] = self.message
        d["status"] = self.status
        d["time"] = self.time
        d["status"] = str(self.status)
        d["type"] = str(self.element_type)
        return d


class TestDetails(object):
    
    def __init__(self, uid):
        self.uid = uid
        self.report_elements = []
        self.level_elements_stack = []
        self.execution_properties = {}

    
    def add_element(self, element):
        if type(element) is not ReportElement:
            raise TypeError("Can only add report elements")
        element.parent = self
        if element.element_type is None:
            element.element_type = ReportElementType.REGULAR
        self.report_elements.append(element)
        if element.element_type == ReportElementType.START_LEVEL:
            self.level_elements_stack.append(element)
        elif element.element_type == ReportElementType.STOP_LEVEL:
            self.level_elements_stack.pop()
        if element.status != ReportElementStatus.SUCCESS:
            for e in self.level_elements_stack:
                e.set_status(element.status)
    
    def dict(self):
        d = {}
        d["uid"] = self.uid
        d["reportElements"] = []        
        for element in self.report_elements:
            d["reportElements"].append(element.dict())
        return d
