'''
Created on Aug 10, 2017

@author: Itai Agmon
'''

import os
import ConfigParser
import sys
from shutil import copyfile

class Conf(object):
    
    CONFIG_FILE = "difido.cfg"
    
    def __init__(self, section):
        if not os.path.isfile(os.getcwd() + "/" +Conf.CONFIG_FILE):
            self.create_config_file()
        self.section = section
        self.parser = ConfigParser.ConfigParser()
        self.parser.read(os.getcwd() + "/" +Conf.CONFIG_FILE)
    
    def create_config_file(self):
        template = os.path.dirname(sys.modules[__name__].__file__) + "/resources/" + Conf.CONFIG_FILE
        copyfile(template, os.getcwd() + "/" +Conf.CONFIG_FILE)
    
    def get_string(self, option):
        return self.parser.get(self.section, option)
    
    def get_int(self, option):
        return self.parser.getint(self.section, option)

    def get_float(self, option):
        return self.parser.getboolean(self.section, option)


