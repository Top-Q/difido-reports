'''
Created on Aug 10, 2017

@author: Itai Agmon
'''

from zipfile import ZipFile
import os
import shutil
import json

from difido_definitions import root_dir

JAR_FILE = 'difido.jar'


def prepare_template(log_folder):
    if os.path.isfile(os.path.join(log_folder, "template", "index.html")):
        return
    archive = ZipFile(os.path.join(root_dir, "difido", JAR_FILE), 'r')
    try:
        for zfile in archive.namelist():
            if zfile.startswith('il.co.topq.difido.view/'):
                archive.extract(zfile, log_folder)
    finally:
        archive.close()
    original_name_folder = os.path.join(log_folder, "il.co.topq.difido.view")
    new_name_folder = os.path.join(log_folder, "template")
    os.rename(original_name_folder, new_name_folder)


def prepare_current_log_folder(log_folder):
    current_folder = os.path.join(log_folder, "current")
    if os.path.isdir(current_folder):
        shutil.rmtree(current_folder)
    shutil.copytree(os.path.join(log_folder, "template"), current_folder)


def prepare_test_folder(log_folder, uid):
    test_folder = os.path.join(log_folder, "current", "tests", "test_" + uid)
    if not os.path.exists(test_folder):
        os.makedirs(test_folder)
        test_html_from_root = os.path.join(log_folder, "current", "test.html")
        test_html_in_test = os.path.join(test_folder, "test.html")
        if not os.path.isfile(test_html_from_root):
            raise Exception(f"Failed to copy '{os.path.abspath(test_html_from_root)}' to '{os.path.abspath(test_html_in_test)}'. File not exists")
        shutil.copyfile(test_html_from_root, test_html_in_test)


def copy_file_to_test_folder(log_folder, uid, file_path):
    dst = os.path.join(log_folder, "current", "tests", "test_" + uid, os.path.basename(file_path))
    shutil.copyfile(file_path, dst)


def write_test_details_to_file(log_folder, test_details):
    test_folder = os.path.join(log_folder, "current", "tests", "test_" + test_details.uid)
    try:
        os.remove(os.path.join(test_folder, "test.js"))
    except OSError:
        pass

    with open(os.path.join(test_folder, 'test.js'), "w+") as ex_file:
        ex_file.write("var test = " + json.dumps(test_details.dict()) + ";")


def write_execution_to_file(log_folder, execution):
    if log_folder:
        try:
            os.remove(os.path.join(log_folder, 'current/execution.js'))
        except OSError:
            pass
        with open(os.path.join(log_folder, 'current/execution.js'), "w+") as ex_file:
            ex_file.write("var execution = " + json.dumps(execution.dict()) + ";")
