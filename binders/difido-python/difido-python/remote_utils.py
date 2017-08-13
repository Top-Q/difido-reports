'''
Created on Aug 10, 2017

@author: Itai Agmon
'''

import httplib, json
from configuration import Conf

conf = Conf("remote")

def prepare_remote_execution(details):
    conn = get_connection()
    conn.request("POST", "/api/executions", to_content(details), {"Content-Type" : "application/json"})
    res = send_request(conn)
    return res.read()

def add_machine(execution_id, machine):
    conn = get_connection()
    conn.request("POST", "/api/executions/%s/machines/" % execution_id, to_content(machine), {"Content-Type" : "application/json"})
    res = send_request(conn)
    return res.read()

def update_machine(execution_id, machine_id, machine):
    conn = get_connection()
    conn.request("PUT", "/api/executions/{0}/machines/{1}".format(str(execution_id), str(machine_id)), to_content(machine), {"Content-Type" : "application/json"})
    send_request(conn)

def add_test_details(execution_id, test_details):
    conn = get_connection()
    conn.request("POST", "/api/executions/{0}/details".format(str(execution_id)), to_content(test_details), {"Content-Type" : "application/json"})
    send_request(conn)

def end_execution(execution_id):
    conn = get_connection()
    conn.request("PUT", "/api/executions/{0}?active=false".format(execution_id), "", {"Content-Type" : "application/json"})
    send_request(conn)

def to_content(obj):
    content = json.dumps(obj.dict())
    return content.encode('utf-8')

def send_request(conn):
    res = conn.getresponse()
    if res.status != 200 and res.status != 204:
        raise Exception("Error in response. error code " + str(res.status))
    return res
    
def get_connection():
    return httplib.HTTPConnection(conf.get_string("host"), conf.get_int("port"), timeout=10)