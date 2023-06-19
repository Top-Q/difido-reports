'''
Created on Aug 10, 2017

@author: Itai Agmon
'''
import ntpath
import traceback
from abc import ABC

from rich.console import Console
from rich.table import Table
from rich.text import Text

from difido_definitions import root_dir
from .execution import Execution, Machine, Scenario, Test
from . import local_utils, remote_utils, config
from .test_details import ReportElement, TestDetails, ReportElementStatus, ReportElementType
from .execution_details import ExecutionDetails
from random import randint
import time
import socket
import os
from datetime import datetime as dt, datetime


class AbstractReporter(ABC):
    TIME_FORMAT = '%H:%M:%S'
    DATE_FORMAT = '%d/%m/%Y'
    DATE_TIME_FORMAT = '%d%m%Y %H:%M:%S'

    def __init__(self):
        self.execution: Execution = None
        self.uid: int = None
        self.index = 0
        self.scenario_stack = []
        self.buffered_elements = []
        self.testDetails = None
        self.scenario: Scenario = None
        self.test: Test = None
        self.test_start_time: str = None
        self.init_model()
        self.start()
        self.num_of_suites_to_ignore = config.num_of_suites_to_ignore

    def init_model(self):
        self.execution = Execution()
        machine = Machine(socket.gethostname())
        machine.planned_tests = config.planned_tests

        self.execution.add_machine(machine)
        self.uid = str(randint(1000, 9999) + time.time() / 1000).replace(".", "")

    def start_suite(self, name):
        self.execution.get_last_machine().planned_tests = 0
        if self.num_of_suites_to_ignore > 0:
            self.num_of_suites_to_ignore -= 1
            return
        self.num_of_suites_to_ignore -= 1
        self.scenario = Scenario(name)

        self.scenario.scenarioProperties = self.get_additional_execution_properties()
        if len(self.scenario_stack) != 0:
            self.scenario_stack[-1].add_child(self.scenario)
        else:
            self.execution.get_last_machine().add_child(self.scenario)

        self.scenario_stack.append(self.scenario)
        self.write_execution()

    def end_suite(self):
        self.num_of_suites_to_ignore += 1
        if self.num_of_suites_to_ignore > 0:
            return
        if len(self.scenario_stack) != 0:
            self.scenario_stack.pop()
        self.testDetails = None

    def end_test(self, attrs):
        if len(self.scenario_stack) == 0:
            return
        self.test.set_status(attrs["status"])
        self.test.duration = attrs["elapsedtime"]
        self.write_test_details()
        self.write_execution()

    def start_test(self, name, attrs):
        if len(self.scenario_stack) == 0:
            return

        self.test_start_time = datetime.strptime(attrs['starttime'], AbstractReporter.DATE_TIME_FORMAT)

        self.test = Test(self.index, name, self.uid + "_" + str(self.index), attrs['className'])
        self.testDetails = TestDetails(self.uid + "_" + str(self.index))

        self.index += 1
        self.test.timstamp = self.test_start_time.strftime(AbstractReporter.TIME_FORMAT)
        self.test.date = self.test_start_time.strftime(AbstractReporter.DATE_FORMAT)
        self.test.description = attrs['doc']
        self.scenario.add_child(self.test)

        if len(self.buffered_elements) > 0:
            for element in self.buffered_elements:
                self.testDetails.add_element(element)
            self.buffered_elements = []

        self.write_execution()
        self.write_test_details()

    def start(self):
        pass

    def close(self):
        if self.testDetails is not None:
            self.write_test_details()
        self.write_execution()

    def write_test_details(self):
        pass

    def write_execution(self):
        pass

    def get_additional_execution_properties(self):
        '''
        return dictionary that will be added as properties to the execution
        '''
        return {}

    def add_report_element(self, element):
        if self.testDetails is None:
            self.buffered_elements.append(element)
        else:
            self.testDetails.add_element(element)
            self.write_test_details()

    def add_test_property(self, key, value):
        self.test.add_property(key, value)
        self.write_test_details()

    def add_execution_property(self, key, value):
        pass

    def add_file(self, file: str):
        pass


class LocalReporter(AbstractReporter):

    def __init__(self):
        self.report_folder = None
        super().__init__()

    def start(self):
        self.report_folder = config.output_folder
        local_utils.prepare_template(self.report_folder)
        local_utils.prepare_current_log_folder(self.report_folder)

    def write_test_details(self):
        local_utils.prepare_test_folder(self.report_folder, self.testDetails.uid)
        local_utils.write_test_details_to_file(self.report_folder, self.testDetails)

    def write_execution(self):
        local_utils.write_execution_to_file(self.report_folder, self.execution)

    def add_file(self, file: str):
        local_utils.copy_file_to_test_folder(self.report_folder, self.testDetails.uid, file)


class RemoteReporter(AbstractReporter):

    def __init__(self):
        self.__execution_properties = {}
        self.__execution_id: int = None
        self.__machine_id: int = None
        self.__enabled: bool = True
        self.__retries = 10
        super().__init__()

    def start(self):
        self.__execution_properties = config.execution_properties
        details = ExecutionDetails()
        details.description = config.execution_description
        details.execution_properties = self.__execution_properties
        try:
            self.__execution_id = remote_utils.prepare_remote_execution(details)
            self.__enabled = True
        except Exception as e:
            print(e)
            self.__enabled = False
            return

        machine = self.execution.get_last_machine()
        try:
            self.__machine_id = remote_utils.add_machine(self.__execution_id, machine)
        except:
            self.__enabled = False
            return

    def get_additional_execution_properties(self):
        if not self.__enabled:
            return
        return self.__execution_properties

    def write_execution(self):
        if not self.__enabled:
            return
        try:
            remote_utils.update_machine(self.__execution_id, self.__machine_id, self.execution.get_last_machine())
        except Exception as e:
            print("Exception occurred when trying to update machine: " + str(e))
            self.check_if_disable()

    def write_test_details(self):
        if not self.__enabled:
            return
        try:
            remote_utils.add_test_details(self.__execution_id, self.testDetails)
        except Exception as e:
            print("Exception occurred when trying to add test details: " + str(e))
            self.check_if_disable()

    def close(self):
        if not self.__enabled:
            return
        super(RemoteReporter, self).close()
        try:
            remote_utils.end_execution(self.__execution_id)
        except Exception:
            pass

    def check_if_disable(self):
        self.__retries -= 1
        if self.__retries <= 0:
            self.__enabled = False
            print("Number of failed request exceeded the maximum allowed. Disabling remote Difido")
        pass

    def add_execution_property(self, key, value):
        if not self.__enabled:
            return
        super(RemoteReporter, self).add_execution_property(key, value)
        self.__execution_properties[key] = value

    def add_file(self, file: str):
        if not self.__enabled:
            return
        remote_utils.add_file(self.__execution_id, self.test.uid, file)


class ConsoleReporter:
    def __init__(self):
        self.screen_width = 200
        self.console = Console(record=True, width=self.screen_width)
        if not self.console.color_system:
            self.console = Console(color_system="windows", record=True, width=self.screen_width)
        self.output_folder = os.path.join(config.output_folder, 'console')
        if not os.path.isdir(self.output_folder):
            os.mkdir(self.output_folder)
        self.filename = f'html-{dt.now().strftime("%d_%m_%y-%H_%M_%S")}.html'
        ''' Since pytest is reporting the progress and status of tests we want avoid this to push our report
         line so we have to keep track if this is the first log message and if so we will create an empty
         log'''
        self.first_log_in_test = None

    def start_test(self, name: str, attrs):
        self.first_log_in_test = True
        self.console.print(' ')
        self.console.rule(Text.styled(f'{name}', style='bold red'))
        if attrs['doc']:
            self.console.print(attrs['doc'], style='grey74')

    def end_test(self, attrs):
        element = ReportElement()
        element.element_type = ReportElementType.REGULAR
        if attrs["status"] == ReportElementStatus.SUCCESS:
            element.status = ReportElementStatus.SUCCESS
            element.title = "Test ended successfully"
        elif attrs["status"] == ReportElementStatus.WARNING:
            element.status = ReportElementStatus.WARNING
            element.title = "Test skipped"
        else:
            element.status = attrs["status"]
            element.title = "Test failed"
        self.add_report_element(element)

    def add_file(self, file: str):
        pass

    def add_report_element(self, element: ReportElement):
        if element.element_type == ReportElementType.REGULAR:
            if element.status == ReportElementStatus.SUCCESS:
                self._log('REGULAR', 'white', element.title)
            elif element.status == ReportElementStatus.WARNING:
                self._log('WARNING', 'yellow', element.title)
            elif element.status == ReportElementStatus.FAILURE:
                self._log('FAILURE', 'blue', element.title)
            elif element.status == ReportElementStatus.ERROR:
                self._log('ERROR', 'red', element.title)
        elif element.element_type == ReportElementType.IMAGE:
            self._log('IMAGE', 'white', f"File: '{element.message}' with description '{element.title}'")
        elif element.element_type == ReportElementType.HTML:
            self._log('HTML', 'cyan', element.title)
        elif element.element_type == ReportElementType.STEP:
            self._log(f'STEP', 'cyan', element.title)
        elif element.element_type == ReportElementType.START_LEVEL:
            self._log(f'LEVEL', 'cyan', element.title)
        elif element.element_type == ReportElementType.STOP_LEVEL:
            self._log(f'STOP', 'cyan', "")

    def add_execution_property(self, key, value):
        self._log('REGULAR', 'white', f"Execution property: {key}={value}")

    def add_test_property(self, key, value):
        self._log('REGULAR', 'white', f"Test property: {key}={value}")

    def start_suite(self, name):
        self._log('REGULAR', 'white', f"Start suite: {name}")

    def end_suite(self):
        self._log('REGULAR', 'white', f"End suite")

    def start(self):
        pass

    def close(self):
        full_file_name = os.path.join(self.output_folder, self.filename)
        if os.path.exists(full_file_name):
            os.remove(full_file_name)
        try:
            self.console.save_html(full_file_name)
        except Exception as e:
            print(f"Exception '{e}' while calling end run from ConsoleReporter")

    def _log(self, level, color, message):
        if self.first_log_in_test:
            ''' We want to avoid the first line interruption of pytest'''
            self.console.print(' ')
            self.first_log_in_test = False
        grid = Table().grid()
        log_time_w = 11
        log_type_w = 9
        log_meta_w = 40
        grid.add_column('Log Time', width=log_time_w)
        grid.add_column("Log Type", width=log_type_w)
        grid.add_column("Log Message", width=self.screen_width - (log_meta_w + log_type_w + log_time_w),
                        overflow="fold")

        call_frame = None
        for frame in traceback.extract_stack():
            if not call_frame:
                call_frame = frame
                continue
            if 'report_manager.py' in frame.filename:
                break
            call_frame = frame

        grid.add_column("Log Meta", width=log_meta_w, justify='right')
        if ntpath.basename(call_frame.filename) != "plugin.py":
            message_source = f'{ntpath.basename(call_frame.filename).replace(".py", "")}:{call_frame.lineno}'
        else:
            message_source = ""
        grid.add_row(f'[cyan][{datetime.now().strftime("%H:%M:%S")}][/]',
                     f'[{color}]{level}[/]',
                     self.console.render_str(message, highlight=True),
                     message_source)
        self.console.print(grid)
