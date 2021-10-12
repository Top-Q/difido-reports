'''
Created on Aug 10, 2017

@author: Itai Agmon
'''


class Execution(object):
    '''
    classdocs
    '''

    def __init__(self):
        self._machines = []
        
    def add_machine(self, machine):
        if type(machine) is not Machine:
            raise TypeError("You can add only machines to execution")
        machine.add_parent(self)
        self._machines.append(machine)
        
    def dict(self):
        d = {}
        d["machines"] = []
        for machine in self._machines:
            d["machines"].append(machine.dict())
        return d
    
    def get_last_machine(self):
        if len(self._machines) == 0:
            return None
        return self._machines[-1]
    
    
    
class Node(object):
    
    def __init__(self, name, node_type):
        self.name = name;
        self.status = "success"
        self._parent = None
        self._node_type = node_type
    
    def add_parent(self, parent):
        self._parent = parent    
    
    def dict(self):
        d = {}
        d["name"] = self.name
        d["status"] = self.status
        d["type"] = self._node_type
        return d
    
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
        
        if self._parent is not None and issubclass(type(self._parent), Node):
            self._parent.set_status(status)
                
    
    
    
class NodeWithChildren(Node):    
    
    def __init__(self, name, node_type):
        super(NodeWithChildren, self).__init__(name, node_type)
        self._children = []
        
        
    def dict(self):
        d = {}
        d.update(super(NodeWithChildren, self).dict())
        d["children"] = []
        for child in self._children:
            d["children"].append(child.dict())
        return d
    
    def add_child(self, child):
        if not issubclass(type(child), Node):
            raise TypeError("Can only add children from type Node")
        self._children.append(child)
        child.add_parent(self)
    
    def count_children(self):
        return len(self._children)
    
    def has_children(self):
        return self.count_children() != 0

class Machine(NodeWithChildren):
    
    def __init__(self, name):
        super(Machine, self).__init__(name, "machine")
        self.planned_tests = 0
    
    def dict(self):
        d = {}
        d["plannedTests"] = self.planned_tests
        d.update(super(Machine, self).dict())
        return d
    

class Scenario(NodeWithChildren):
    def __init__(self, name):
        NodeWithChildren.__init__(self, name, "scenario")
        self.scenarioProperties = {}
    
    def add_scenario_property(self, key, value):
        if key is None:
            raise ValueError("Key can not be none")
        self.scenarioProperties[key] = value
    
    def dict(self):
        d = {}
        d["scenarioProperties"] = self.scenarioProperties
        d.update(super(Scenario, self).dict())
        return d
    
        
class Test(Node):
    def __init__(self, index, name, uid, class_name):
        Node.__init__(self, name, "test")
        if index < 0:
            raise TypeError("Index can't be smaller then 0")
        self.index = index
        self.uid = uid
        self.description = ""
        self.duration = 0
        self.timstamp = ""
        self.date = ""
        self.parameters = {}
        self.properties = {}
        self.className = class_name
        
    def add_parameter(self, key, value):
        if key is None:
            raise TypeError("Key can't be none")
        self.parameters[key] = value
        
    def add_property(self, key, value):
        if key is None:
            raise TypeError("Key can't be none")
        self.properties[key] = value

    
    def dict(self):
        d = {}
        d["index"] = self.index
        d["uid"] = self.uid
        d["description"] = self.description
        d["duration"] = self.duration
        d["timestamp"] = self.timstamp
        d["date"] = self.date
        d["className"] = self.className
        d["parameters"] = self.parameters
        d["properties"] = self.properties
        d.update(super(Test, self).dict())
        return d
        
        


