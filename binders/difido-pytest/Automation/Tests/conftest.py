from configuration import Conf
from test_details import ReportElementStatus, ReportElementType
from time import localtime, strftime
import time
import Reporter as Difido

difido_suite_name = None  # type: str
difido_test_name = None  # type: str
testAttr = {}
reporter = Difido.Reporter()
config_file = "config.cfg"
automation_ver = "Some version Here"

def pytest_runtest_setup(item):
    global current_test_number
    global testAttr
    testAttr = {'starttime': strftime("%Y%m%d %H:%M:%S", localtime()), 'doc': "",
                'className': item.nodeid.split("::")[1].replace("Test", "")}
    testname = item.name.replace("test_", "")
    reporter.start_test(testname, testAttr)

def pytest_runtest_logreport(report):
    if report.when == 'call':
        if report.outcome == 'failed':
            testAttr['status'] = ReportElementStatus.ERROR
            reporter.report("", "<pre>" + str(report.longreprtext) + "</pre>", element_type=ReportElementType.HTML,
                            status=ReportElementStatus.ERROR)
        else:
            testAttr['status'] = ReportElementStatus.SUCCESS

        time.sleep(1)
        from test_details import ReportElementType as Type
        reporter.report("", "Test End", Type.STEP)
        reporter.end_test(difido_test_name, testAttr)

def pytest_sessionfinish(session):
    reporter.debug("Clean up")
    reporter.debug("Wait 10 seconds before execution End")
    time.sleep(10)
    reporter.end_suite(difido_suite_name, testAttr)
    reporter.close()


def pytest_collection_finish(session):
    """ called after collection has been performed and modified.
    :param _pytest.main.Session session: the pytest session object
    """

    print("~~~~~~~~~~~~~~~~~~~~~~~~~`Automation Ver :: "+automation_ver+"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
    conf = Conf("remote")
    description = conf.get_string("description")
    reporter.start_suite(description, testAttr)
    reporter.add_execution_properties("automation_version", automation_ver)