import os



from definitions import root_dir


def test_messaging(report):
    # Simple message will be added to the report
    report.that("Simple report message")
    # link to Google
    report.link(title='Google', link=r'http:\\www.google.com')

    # Message will be hidden inside toggle element
    report.start_level("start toggle")
    report.that("In toggle")
    report.stop_level()

    # Message will appear in bold font
    report.step("bold message")

    # HTML element will be added to the report
    report.html("Table", """
        <table>
      <tr>
        <th>Company</th>
        <th>Contact</th>
      </tr>
      <tr>
        <td>ACME Corporation</td>
        <td>Road Bumper</td>
      </tr>
      <tr>
        <td>DOLO Inc</td>
        <td>Wired Coyote</td>
      </tr>
    </table>
        """)


def test_files(report):
    # File will be copied and attached to the report
    report.file(file_path=os.path.join(root_dir, "resources", "some_file.txt"), description="My text file")
    # Image will added with thumbnail
    report.img(img_path=os.path.join(root_dir, "resources", "cat.jpg"), description="Cats are awesome")


def test_add_properties(report):
    # Property will be added to the execution
    report.add_execution_properties("int_Execution", "66")

    # Property will be added to the current running test
    report.add_test_property("double_testProp", "1.56")


def test_report_without_fixture():
    """
    If you don't want to get the reporter as fixture or if you want to use it not from test method
    you can just instantiate the Report class. The Report class is implemented as singleton
    """
    from difido.report_manager import Report
    report = Report()
    report.that("very important message")
