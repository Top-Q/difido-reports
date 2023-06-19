import os

from difido.reporters import config
from difido.test_details import ReportElement, ReportElementStatus, ReportElementType
from time import localtime, strftime, time
import logging


def generate_report_instances(reporters_str: str):
    reporters = []
    if not reporters_str:
        return reporters
    for reporter in reporters_str.split(","):
        splitted_reporter_name = reporter.strip().split(".")
        module_name = '.'.join(splitted_reporter_name[:-1])
        class_name = splitted_reporter_name[-1]
        module = __import__(module_name)
        reporter_class = getattr(module, class_name)
        reporters.append(reporter_class())
    return reporters


class Report:
    class __Report:

        @staticmethod
        def __get_epoc_seconds():
            return int(round(time()))

        def __init__(self):
            self.logger = logging.getLogger()
            self.current_worst_test_status = ReportElementStatus.SUCCESS
            self.test_start_time = 0
            self._reporters = generate_report_instances(config.reporters)

        def that(self, title: str, msg: str = ""):
            if not title:
                return
            self._report(title, msg)

        def html(self, title: str, msg: str = ""):
            if not title:
                return
            self._report(title, msg, element_type=ReportElementType.HTML)

        def file(self, file_path: str, description: str = None):
            if not file_path:
                return
            for report in self._reporters:
                report.add_file(file_path)
            file_name = os.path.basename(file_path)
            if description:
                self.link(description, file_name)
            else:
                self.link(file_name, file_name)

        def img(self, img_path: str, description: str = None):
            if not img_path:
                return
            for report in self._reporters:
                report.add_file(img_path)
            file_name = os.path.basename(img_path)
            if description:
                self._report(description, file_name, element_type=ReportElementType.IMAGE)
            else:
                self._report(file_name, file_name, element_type=ReportElementType.IMAGE)

        def link(self, title: str, link: str):
            if not title or not link:
                return
            self._report(title, link, element_type=ReportElementType.LINK)

        def _report(self, title, msg, element_type=ReportElementType.REGULAR, status=ReportElementStatus.SUCCESS):
            element = ReportElement()
            element.title = title
            element.set_type(element_type)
            element.set_status(status)
            element.time = strftime("%H:%M:%S", localtime())
            self._set_status(status)
            element.message = msg
            if element_type == ReportElementType.LINK:
                element.title = '<a href="' + msg + '" target="_blank">' + title + '</a>'
                element.message = None
                element.set_type(ReportElementType.HTML)
            for reporter in self._reporters:
                reporter.add_report_element(element)

        def step(self, title):
            self._report(title, "", ReportElementType.STEP)

        def start_level(self, title):
            self._report(title, "", ReportElementType.START_LEVEL)

        def stop_level(self):
            self._report("", "", ReportElementType.STOP_LEVEL)

        def add_execution_properties(self, key, value):
            for reporter in self._reporters:
                reporter.add_execution_property(key, value)

        def add_test_property(self, key, value):
            for reporter in self._reporters:
                reporter.add_test_property(key, value)

        def start_test(self, name, attrs):
            self.current_worst_test_status = ReportElementStatus.SUCCESS
            for reporter in self._reporters:
                reporter.start_test(name, attrs)
            self.test_start_time = self.__get_epoc_seconds()

        def end_test(self, attrs):
            self._set_status(attrs['status'])
            attrs['status'] = self.current_worst_test_status
            attrs['elapsedtime'] = (self.__get_epoc_seconds() - self.test_start_time) * 1000
            for reporter in self._reporters:
                reporter.add_test_property("duration", str(attrs['elapsedtime']))
                reporter.end_test(attrs)

        def start_suite(self, name):
            for reporter in self._reporters:
                reporter.start_suite(name)

        def end_suite(self):
            for reporter in self._reporters:
                reporter.end_suite()

        def start(self):
            for reporter in self._reporters:
                reporter.start()

        def close(self):
            for reporter in self._reporters:
                reporter.close()

        def _set_status(self, status):
            if status == ReportElementStatus.ERROR:
                self.current_worst_test_status = status
            elif status == ReportElementStatus.FAILURE:
                if self.current_worst_test_status != ReportElementStatus.ERROR:
                    self.current_worst_test_status = status
            elif status == ReportElementStatus.WARNING:
                if self.current_worst_test_status != ReportElementStatus.ERROR and self.current_worst_test_status != ReportElementStatus.FAILURE:
                    self.current_worst_test_status = status

    instance = None

    def __new__(cls):  # __new__ always a classmethod
        if not Report.instance:
            Report.instance = Report.__Report()
        return Report.instance

    def __getattr__(self, name):
        return getattr(self.instance, name)

    def __setattr__(self, name):
        return setattr(self.instance, name)
