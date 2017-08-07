"""Listener that stops execution if a test fails."""
from ReportManager import ReportManager

ROBOT_LISTENER_API_VERSION = 2

def start_suite(name, attrs):
    manager.start_suite(name, attrs)


def start_keyword(name, attrs):
    manager.start_keyword(name, attrs)
    pass

def end_keyword(name, attrs):
    manager.end_keyword(name, attrs)
    pass

def log_message(message):
    manager.log_message(message)

def message(message):
    manager.message(message)

def end_test(name, attrs):
    manager.end_test(name, attrs)

def start_test(name, attrs):
    manager.start_test(name, attrs)

def close():
    manager.close()

manager = ReportManager()