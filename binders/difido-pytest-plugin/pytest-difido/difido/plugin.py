# -*- coding: utf-8 -*-
import _pytest
import pytest

from time import localtime, strftime

from difido import config, ReportElementStatus, ReportElementType
from difido.report_manager import Report

test_attr = {}


def pytest_addoption(parser):
    group = parser.getgroup('difido')

    def add_option(key, description):
        group.addoption(
            '--' + key,
            action='store',
            dest='dest_' + key,
            default=None,
            help=description
        )
        parser.addini(key, description)

    add_option(key='df_host', description='Difido server host or ip')
    add_option(key='df_port', description='Difido server port address')
    add_option(key='df_reporters', description='Comma separated list of report classes')
    add_option(key='df_description', description='Difido execution description')
    add_option(key='df_output', description='Reports output folder')


def set_config(pytest_config: _pytest.config.Config):
    def read_value(key: str, default: str) -> str:
        if getattr(pytest_config.option, 'dest_' + key):
            return getattr(pytest_config.option, 'dest_' + key)
        if key in pytest_config.inicfg:
            return pytest_config.inicfg[key]
        return default

    config.host = read_value("df_host", config.host)
    config.port = int(read_value("df_port", config.port))
    config.reporters = read_value("df_reporters", config.reporters)
    config.execution_description = read_value("df_description", config.execution_description)
    config.output_folder = read_value("df_output", config.output_folder)


@pytest.fixture(scope="session")
def report():
    return Report()


def pytest_runtest_setup(item):
    global test_attr
    report = Report()
    doc = getattr(item.obj, '__doc__')
    test_attr = {'starttime': strftime("%d%m%Y %H:%M:%S", localtime()), 'doc': doc,
                 'className': item.nodeid}
    test_name = item.name.capitalize().replace('_', ' ')
    report.start_test(test_name, test_attr)
    if item.fixturenames:
        report.add_test_property("fixtures", ", ".join(item.fixturenames))
    if item.own_markers:
        markers = ", ".join([m.name for m in item.own_markers])
        report.add_test_property("markers", markers)
    report.add_test_property("original_name", item.originalname)
    report.add_test_property("path", str(item.path))

    Report().stop_level()
    Report().start_level("Setup")


def pytest_runtest_logreport(report):
    if report.skipped:
        Report()._report(f"{report.longrepr[2]}",
                         None, element_type=ReportElementType.REGULAR,
                         status=ReportElementStatus.WARNING)
        test_attr['status'] = ReportElementStatus.WARNING

    elif (report.when == 'setup' or report.when == 'teardown') and report.outcome == 'failed':
        test_attr['status'] = ReportElementStatus.ERROR
        Report()._report(f"Error in '{report.when}' phase", "<pre>" + str(report.longreprtext) + "</pre>",
                         element_type=ReportElementType.HTML,
                         status=ReportElementStatus.ERROR)
    elif report.when == 'call':
        if report.outcome == 'failed':
            test_attr['status'] = ReportElementStatus.FAILURE
            Report()._report("Failure in test", "<pre>" + str(report.longreprtext) + "</pre>",
                             element_type=ReportElementType.HTML,
                             status=ReportElementStatus.FAILURE)
    else:
        test_attr['status'] = ReportElementStatus.SUCCESS

    if report.when == "setup":
        Report().stop_level()
    elif report.when == "call":
        Report().start_level("Teardown")
    if report.when == "teardown":
        Report().stop_level()

    if report.skipped or report.when == "teardown":
        Report().end_test(test_attr)


def pytest_sessionstart(session):
    set_config(session.config)
    Report().start_suite(config.execution_description)


def pytest_sessionfinish(session):
    reporter = Report()
    reporter.end_suite()
    reporter.close()
