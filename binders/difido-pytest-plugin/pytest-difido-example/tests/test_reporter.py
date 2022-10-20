import os.path
import time

import pytest

from definitions import root_dir


@pytest.fixture()
def failed_setup_fixture(report):
    report.that("About to test failure in fixture")
    pytest.fail()


@pytest.fixture()
def failed_teardown_fixture(report):
    yield ""
    report.that("About to test failure in teardown")
    pytest.fail()


def test_failure_in_setup(failed_setup_fixture):
    pass


def test_failure_in_teardown(report, failed_teardown_fixture):
    pass


@pytest.fixture()
def messaging_fixture(report):
    report.that("Message from setup phase")
    yield ""
    report.that("Message from teardown phase")


def test_with_messages_in_fixture(report, messaging_fixture):
    report.that("Message from call phase")


def test_pass(report):
    report.that("Successful test")


def test_step(report):
    report.that("Regular message")
    report.step("This is step")
    report.that("Another regular message")


def test_add_properties(report):
    report.add_execution_properties("int_Execution", "66")
    report.add_test_property("double_testProp", "1.56")


def test_levels(report):
    report.start_level("Starting level")
    report.that("Some message")
    report.start_level("Starting another level")
    report.that("Some message")
    report.stop_level()
    report.stop_level()


def test_exception(report):
    report.that("About to raise exception")
    raise Exception("Purposed Exception")


def test_assertion(report):
    report.that("About to fail in assertion")
    assert 1 == 0


def test_message_with_title_and_message(report):
    report.that(title="This is the title", msg='This is the message')


def test_test_duration(report):
    """Test that will display the test duration as test property"""
    report.that("About to go to sleep for 1 second")
    time.sleep(1)
    report.that("Waking up")


@pytest.mark.parametrize("test_input,expected", [("3+5", 8), ("2+4", 6), ("6*9", 42)])
def test_data_driven(report, test_input, expected):
    report.that(f"{test_input} = {expected}")
    assert eval(test_input) == expected


def test_html(report):
    report.html("<b>Bold Title</b>")
    report.html("title", "<b>Bold Message</b>")
    report.html("Table","""
    <table>
  <tr>
    <th>Company</th>
    <th>Contact</th>
    <th>Country</th>
  </tr>
  <tr>
    <td>Alfreds Futterkiste</td>
    <td>Maria Anders</td>
    <td>Germany</td>
  </tr>
  <tr>
    <td>Centro comercial Moctezuma</td>
    <td>Francisco Chang</td>
    <td>Mexico</td>
  </tr>
</table>
    """)
    report.that("The following should not work as HTML")
    report.that("<b>Bold Title</b>")
    report.that("title", "<b>Bold Message</b>")


def test_link(report):
    report.that("The following should appear as link to Google.com")
    report.link("Google", r"http:\\www.google.com")


def test_add_file(report):
    report.file(file_path=os.path.join(root_dir, "resources", "some_file.txt"), description="My text file")
    report.file(file_path=os.path.join(root_dir, "resources", "some_file.txt"))


def test_add_img(report):
    report.img(img_path=os.path.join(root_dir, "resources", "cat.jpg"), description="Cats are awesome")


def test_with_description(report):
    """This is a test with description. The description should be added to the HTML report"""
    report.that("This is test with description")


@pytest.mark.sanity
def test_with_marks(report):
    report.that("Testing test with marks")


def test_that_skipped_with_reason(report):
    pytest.skip("The reason for skipping the test")


def test_that_skipped_without_reason(report):
    pytest.skip()
