from Test import Test
from test_details import ReportElementStatus
import time


class TestReporter(Test):

    def test_pass(self):
        self.report.log("reporter HEYY")
        self.report.add_execution_properties("int_Execution", "66")
        self.report.add_test_property("double_testProp","1.56")

    def test_fail(self):
        self.report.log("reporter HEYY", ReportElementStatus.FAILURE)


    def test_warning(self):
        self.report.log("reporter HEYY", ReportElementStatus.WARNING)
        time.sleep(12)


    def test_error(self):
        self.report.log("reporter HEYY", ReportElementStatus.ERROR)
        time.sleep(61)


    def test_exception(self):
        anc.aaa()

    def test_assetion(self):
        time.sleep(1)
        assert 1 == 0

    def test_levels(self):
        self.report.start_level("Level")
        self.report.log("Level Test")
        self.report.stop_level()
