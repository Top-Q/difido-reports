# Created on Aug 10, 2017
#
# @author: Itai Agmon


import os
import configparser
import sys
from shutil import copyfile
from configparser import NoOptionError


class Conf(object):
    
    CONFIG_FILE = "config.cfg"
    
    def __init__(self, section):

        if "--config-file" in sys.argv:
            Conf.CONFIG_FILE = sys.argv[sys.argv.index("--config-file")+1]
        if not os.path.isfile(os.getcwd() + "/" + Conf.CONFIG_FILE):
            self.create_config_file()
        self.section = section
        self.parser = configparser.ConfigParser()
        self.parser.read(os.getcwd() + "/" + Conf.CONFIG_FILE)
    
    def create_config_file(self):
        template = os.path.dirname(sys.modules[__name__].__file__) + "/resources/" + Conf.CONFIG_FILE
        copyfile(template, os.getcwd() + "/" + Conf.CONFIG_FILE)
    
    def get_string(self, option):
        try:
            return self.parser.get(self.section, option)
        except NoOptionError:
            return ""
    
    def get_int(self, option):
        try:
            return self.parser.getint(self.section, option)
        except NoOptionError:
            return 0

    def get_float(self, option):
        try:
            return self.parser.getboolean(self.section, option)
        except NoOptionError:
            return 0.0
    
    def get_list(self, option):
        try:
            return self.get_string(option).split(';')
        except Exception:
            return []

    def get_dict(self, option):
        d = {}
        try:
            value = self.get_string(option)
        except NoOptionError:
            return {} 
        if value is None:
            return d
        if len(value.split(";")) == 0:
            return d
        for keyval in value.split(";"):
            if len(keyval.split("=")) < 2:
                continue
            d[keyval.split('=')[0]] = keyval.split('=')[1]
        return d