from Utils.Singleton import Singleton
import difido
from test_details import ReportElement, ReportElementStatus, ReportElementType
from time import localtime, strftime, time
from configuration import Conf
import threading
import logging


class Reporter(object, metaclass=Singleton):
    logger = logging.getLogger()
    reporters = []  # type: List[difido.AbstractReport]
    worstStatus = ReportElementStatus.SUCCESS
    testStartTime = 0
    report_lock = threading.Lock()

    @staticmethod
    def __get_epoc_seconds():
        return int(round(time()))

    def __init__(self):
        conf = Conf("remote")
        remote_enable = conf.get_string("enable")
        if remote_enable.lower() == "true":
            print("Enable remote Reporter")
            self.reporters.append(difido.RemoteReport())
        else:
            print("remote reporter disabled")
        self.reporters.append(difido.Console())

    def log(self, msg, status=ReportElementStatus.SUCCESS):
        self.report("", msg, status=status)

    def report(self, msg, title, element_type=ReportElementType.REGULAR, status=ReportElementStatus.SUCCESS):
        # self.debug("report lock aquire")
        self.report_lock.acquire()
        element = ReportElement()
        element.title = title
        element.set_type(element_type)
        element.set_status(status)
        element.time = strftime("%H:%M:%S", localtime())
        self.set_status(status)
        element.message = msg
        if element_type == ReportElementType.HTML:
            element.title = '<pre style="white-space: pre-wrap;">' + element.title + '</pre>'
        if element_type == ReportElementType.LINK:
            element.title = '<a href="' + element.title + '" target="_blank">' + element.title + '</a>'
            element.set_type(ReportElementType.HTML)
        for reporter in self.reporters:
            reporter.add_report_element(element)
            # self.debug("finshed reporting to " + str(reporter))
        self.report_lock.release()
        # self.debug("report lock release")

    def start_level(self, title):
        self.report("", title, ReportElementType.START_LEVEL)

    def stop_level(self):
        self.report("", "", ReportElementType.STOP_LEVEL)

    def add_execution_properties(self, key, value):
        self.report_lock.acquire()
        for reporter in self.reporters:
            reporter.add_exeution_property(key, value)
        self.report_lock.release()

    def add_test_property(self, key, value):
        self.report_lock.acquire()
        for reporter in self.reporters:
            reporter.add_test_property(key, value)
        self.report_lock.release()

    def start_test(self, name, attrs):
        self.report_lock.acquire()
        self.worstStatus = ReportElementStatus.SUCCESS
        for reporter in self.reporters:
            reporter.start_test(name, attrs)
        self.report_lock.release()
        self.testStartTime = self.__get_epoc_seconds()

    def end_test(self, name, attrs):
        self.debug("end test lock aquire")
        self.report_lock.acquire()
        self.set_status(attrs['status'])
        attrs['status'] = self.worstStatus
        attrs['elapsedtime'] = (self.__get_epoc_seconds() - self.testStartTime) * 1000
        self.report_lock.release()
        self.report("", "Test Time: " + str(attrs['elapsedtime']/1000) + " seconds", ReportElementType.STEP)
        self.report("", "Test Status: " + str(attrs['status']), ReportElementType.REGULAR, status=attrs['status'])
        self.report_lock.acquire()
        for reporter in self.reporters:
            reporter.end_test(name, attrs)
        self.report_lock.release()
        self.debug("end test lock release")

    def start_suite(self, name, attrs):
        self.report_lock.acquire()
        for reporter in self.reporters:
            reporter.start_suite(name, attrs)
        self.report_lock.release()

    def end_suite(self, name, attrs):
        self.debug("end suite lock aquire")
        self.report_lock.acquire()
        for reporter in self.reporters:
            reporter.end_suite(name, attrs)
        self.report_lock.release()
        self.debug("end suite lock release")

    def start(self):
        self.report_lock.acquire()
        for reporter in self.reporters:
            reporter.start()
        self.report_lock.release()

    def close(self):
        self.report_lock.acquire()
        for reporter in self.reporters:
            reporter.close()
        self.report_lock.release()

    def debug(self, msg, module=None):
        addition_string = ""
        if module is not None:
            addition_string = module + " :: "
        if isinstance(msg, bytes):
            msg = msg.decode()
        self.logger.info(strftime("%H:%M:%S", localtime()) + " :: DEBUG :: " + addition_string + msg)

    def set_status(self, status):
        if status == ReportElementStatus.ERROR:
            self.worstStatus = status
        elif status == ReportElementStatus.FAILURE:
            if self.worstStatus != ReportElementStatus.ERROR:
                self.worstStatus = status
        elif status == ReportElementStatus.WARNING:
            if self.worstStatus != ReportElementStatus.ERROR and self.worstStatus != ReportElementStatus.FAILURE:
                self.worstStatus = status
