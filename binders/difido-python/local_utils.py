
'''
Created on Aug 10, 2017

@author: Itai Agmon
'''

from zipfile import ZipFile
import os
import shutil
import json
import sys

JAR_FILE = 'difido.jar'

def prepare_template(log_folder):
    if os.path.isfile(log_folder + "/template/index.html"):
        return
    archive = ZipFile(os.path.dirname(sys.modules[__name__].__file__) + "/resources/" + JAR_FILE, 'r')
    try:
        for zfile in archive.namelist():
            if zfile.startswith('il.co.topq.difido.view/'):
                archive.extract(zfile, log_folder)
    finally:
        archive.close()
    os.rename(log_folder + "il.co.topq.difido.view/", log_folder + "template")


def prepare_current_log_folder(log_folder):
    if os.path.isdir(log_folder + "current/"):
        shutil.rmtree(log_folder + "current/")
    shutil.copytree(log_folder + "template/", log_folder + "current/")

def prepare_test_folder(log_folder, uid):
    testfolder = log_folder + 'current/tests/test_' + uid
    if not os.path.exists(testfolder):
        os.makedirs(testfolder)
        shutil.copyfile(log_folder + "current/test.html", testfolder + "/test.html")

def write_test_details_to_file(log_folder, test_details):
    testfolder = log_folder + 'current/tests/test_' + test_details.uid
    try:
        os.remove(testfolder + "/test.js")
    except OSError:
        pass
     
    with open(testfolder + '/test.js', "w+") as ex_file:
        ex_file.write("var test = " + json.dumps(test_details.dict()) + ";")

def write_execution_to_file(log_folder, execution):
    try:
        os.remove(log_folder + 'current/execution.js')
    except OSError:
        pass
    with open(log_folder + '/current/execution.js', "w+") as ex_file:
        ex_file.write("var execution = " + json.dumps(execution.dict()) + ";")


