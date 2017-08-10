
'''
Created on Aug 10, 2017

@author: Itai Agmon
'''

from zipfile import ZipFile
import os
import shutil
import json
import sys

LOG_FOLDER = os.getcwd() + "/log/"

JAR_FILE = 'difido.jar'

def prepare_template():
    if os.path.isfile(LOG_FOLDER + "/template/index.html"):
        return
    archive = ZipFile(os.path.dirname(sys.modules[__name__].__file__) + "/resources/" + JAR_FILE, 'r')
    try:
        for zfile in archive.namelist():
            if zfile.startswith('il.co.topq.difido.view/'):
                archive.extract(zfile, LOG_FOLDER)
    finally:
        archive.close()
    os.rename(LOG_FOLDER + "il.co.topq.difido.view/", LOG_FOLDER + "template")


def prepare_current_log_folder():
    if os.path.isdir(LOG_FOLDER + "current/"):
        shutil.rmtree(LOG_FOLDER + "current/")
    shutil.copytree(LOG_FOLDER + "template/", LOG_FOLDER + "current/")

def prepare_test_folder(uid):
    testfolder = LOG_FOLDER + 'current/tests/test_' + uid
    if not os.path.exists(testfolder):
        os.makedirs(testfolder)
        shutil.copyfile(LOG_FOLDER + "current/test.html", testfolder + "/test.html")

def write_test_details_to_file(test_details):
    testfolder = LOG_FOLDER + 'current/tests/test_' + test_details.uid
    try:
        os.remove(testfolder + "/test.js")
    except OSError:
        pass
     
    with open(testfolder + '/test.js', "w+") as ex_file:
        ex_file.write("var test = " + json.dumps(test_details.dict()) + ";")

def write_execution_to_file(execution):
    try:
        os.remove(LOG_FOLDER + 'current/execution.js')
    except OSError:
        pass
    with open(LOG_FOLDER + '/current/execution.js', "w+") as ex_file:
        ex_file.write("var execution = " + json.dumps(execution.dict()) + ";")


