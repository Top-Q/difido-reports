'''
Created on Aug 10, 2017

@author: Itai Agmon
'''


class ReportElement(object):
    
    def __init__(self):
        self.parent = None
        self.title = ""
        self.message = ""
        self.status = "success"
        self.time = ""
        self.element_type = "regular"
    
    
    def set_status(self, status):
        if status != "error" and status != "failure" and status != "warning" and status != "success":
            raise ValueError("Illegal status %s" % status)
        if status == "error":
            self.status = status
        elif status == "failure":
            if self.status != "error":
                self.status = status
        elif status == "warning":
            if self.status != "error" and self.status != "failure":
                self.status = status
        
    def set_type(self, element_type):
        if element_type != "regular" and element_type != "lnk" and element_type != "img" and element_type != "html" and element_type != "step" and element_type != "startLevel" and element_type != "stopLevel":
            raise ValueError("Illegal element type %s" % element_type)
        self.element_type = element_type 
    
    def dict(self):
        d = {}
        d["title"] = self.title
        d["message"] = self.message
        d["status"] = self.status
        d["time"] = self.time
        d["status"] = self.status
        d["type"] = self.element_type
        return d


class TestDetails(object):
    
    def __init__(self, uid):
        self.uid = uid
        self.report_elements = []
        self.level_elements_stack = []
    
    def add_element(self, element):
        if type(element) is not ReportElement:
            raise TypeError("Can only add report elements")
        element.parent = self
        if element.element_type is None:
            element.element_type = "regular"
        self.report_elements.append(element)
        if element.element_type == "startLevel":
            self.level_elements_stack.append(element)
        elif element.element_type == "stopLevel":
            self.level_elements_stack.pop()
        if element.status != "success":
            for e in self.level_elements_stack:
                e.set_status(element.status)
    
    def dict(self):
        d = {}
        d["uid"] = self.uid
        d["reportElements"] = []        
        for element in self.report_elements:
            d["reportElements"].append(element.dict())
        return d
