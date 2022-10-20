pytest-difido
=============

PyTest plugin for generating HTML test reports

----

This `pytest`_ plugin was generated with `Cookiecutter`_ along with `@hackebrot`_'s `cookiecutter-pytest-plugin`_ template.


Features
--------

* Flexible locally generated HTML report
* Reports are generated at runtime. No need to wait for test execution to end
* Attach files and images to the report
* Works in local and remote mode. You can add `Difido server <https://github.com/Top-Q/difido-reports/releases/>`_ that allows publishing reports to central server
* Console reports - Reports to the console in addition to the HTML output mainly for development phase and CI/CD systems
* Extensible - Allows to implement and add additional reporters


Requirements
------------

* PyTest >= 4.0.0
* Python >= 3


Installation
------------

You can install "pytest-difido" via `pip`_ from `PyPI`_::

    $ pip install pytest-difido



Usage
-----

To use the report, add the report fixture to your test cases. You can report simple message, links, HTML elements and more

..  code-block:: python

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

Files and images can be attached to the HTML report. The files are copied to the local and remote reports. 

..  code-block:: python

    def test_files(report):
        # File will be copied and attached to the report
        report.file(file_path=os.path.join("resources", "some_file.txt"), description="My text file")
        # Image will added with thumbnail
        report.img(img_path=os.path.join("resources", "cat.jpg"), description="Cats are awesome")

Properties can be added to the suite or to each test. The properties are disaplyed in the report and, if ElasticSearch integration is enabled, can be searched.

..  code-block:: python

    def test_add_properties(report):
        # Property will be added to the execution
        report.add_execution_properties("int_Execution", "66")

        # Property will be added to the current running test
        report.add_test_property("double_testProp", "1.56")

If you prefer to use to report without fixtures, or if you want to use it from places other then your test cases, you can intantiate the `Report` class. 
The 'Report' class is implemented as singleton so you will always get the same instance

..  code-block:: python

    def test_report_without_fixture():
        """
        If you don't want to get the reporter as fixture or if you want to use it not from test method
        you can just instantiate the Report class. The Report class is implemented as Singleton
        """
        from difido.report_manager import Report
        report = Report()
        report.that("very important message")

Server Installation
--------------------

This step is not mandatory. Local reports will be generated also without the Difido server. Follow this step only if you wish to 
have central server for publishing test results. If all you need is local reports that will be generated on the running machine, skip this step


* Download the latest Difido server ZIP file from the `Difido releases`_ page
* Extract the content of the file
* Run the '[root]/bin/start.bat' or '[root]/bin/start.sh' file according to your OS. 
* Set the host and port in the client configuration as shown in the next section

You can access the server from your browser (E.G http://localhost:8080/). For more information please refer to the `Difido server Wiki page`_


Configuration
-------------

You can configure the reporter from the `pytest.ini` file or from the command line.
In the case where a parameter is defined using both methods, the value defined from the command line takes precedence

pytest.ini examples

..  code-block:: ini

    [pytest]
    ; Host or ip of Difido server. Default localhost
    df_host = 192.168.0.13
    ; Port of Difido server. Default 8080
    df_port = 8090
    ; Result folder for HTML and console reports. Default is current directory
    df_output_folder = /etc/log/

Command line example

..  code-block:: ini

    $ pytest .\tests\test_report_examples.py --df_host=192.168.10.1 --df_port=8090


**Configuration Properties**

* **df_output_folder** - Result folder for HTML and console reports (default .)
* **df_host**          - Host or ip of Difido server (default localhost)
* **df_port**          - Difido server port (default 8080)
* **df_reporters**     - Comma separated list of reporter classes. The all reporters recieves the event from the report manager
* **df_description**   - Description of test execution as shown in the Difido server


Contributing
------------
Contributions are very welcome. Tests can be run with `tox`_, please ensure
the coverage at least stays the same before you submit a pull request.

License
-------

Distributed under the terms of the `Apache Software License 2.0`_ license, "pytest-difido" is free and open source software


Issues
------

If you encounter any problems, please `file an issue`_ along with a detailed description.

.. _`Cookiecutter`: https://github.com/audreyr/cookiecutter
.. _`@hackebrot`: https://github.com/hackebrot
.. _`MIT`: http://opensource.org/licenses/MIT
.. _`BSD-3`: http://opensource.org/licenses/BSD-3-Clause
.. _`GNU GPL v3.0`: http://www.gnu.org/licenses/gpl-3.0.txt
.. _`Apache Software License 2.0`: http://www.apache.org/licenses/LICENSE-2.0
.. _`cookiecutter-pytest-plugin`: https://github.com/pytest-dev/cookiecutter-pytest-plugin
.. _`file an issue`: https://github.com/Top-Q/difido-reports/issues
.. _`pytest`: https://github.com/pytest-dev/pytest
.. _`tox`: https://tox.readthedocs.io/en/latest/
.. _`pip`: https://pypi.org/project/pip/
.. _`PyPI`: https://pypi.org/project
.. _`Difido releases`: https://github.com/Top-Q/difido-reports/releases/
.. _`Difido server Wiki page`: https://github.com/Top-Q/difido-reports/wiki/The-Difido-Server