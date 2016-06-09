[![Build Status](https://travis-ci.org/Top-Q/difido-reports.svg?branch=master)](https://travis-ci.org/Top-Q/difido-reports)

Difido Reports
================

This project aims to provide a flexible, realtime HTML report for various functional test automation frameworks.

The project includes three parts.

* Binders for various frameworks, like TestNG, NUnit, JSystem and others. The binder generates real time, local HTML report. 
* Server that allows you to see all the reports from one central location. The reports are shown in real time so you can see the status of all your tests in the whole lab without waiting for the executions to end.
* Embedded [Elasticsearch](https://www.elastic.co/) server in the Difido server. There is no need to any additional installation. All the data is stored in the Elasticsearch engine and you can use [Kibana](https://www.elastic.co/products/kibana) or any other tool to get important BI on your executions history. 

## The HTML Report

A live demo of the HTML report can be viewed [here](http://top-q.github.io/difido-reports-demo/)

### Dashboard
In the dashboard you can have a quick view of the status of the test execution. Summary table, graphs and different properties provides you with different aspects of the run.

![dashboard](http://top-q.github.io/difido-reports/images/dashboard.png)


### Execution Tree
In the execution tree you can drill down to each of the tests, according the suite hierarchy. And get all the information you need to understand what really happened during the test run.
You can easily embed in the report different kind of elements like:

* Screenshot
* Files
* Links
* HTML elements like tables.

![execution tree](http://top-q.github.io/difido-reports/images/execution_tree.png)

### Execution Table
Another view of the tests can be achieved from the **Execution Table**. In this view the tests are organized in a table which is allows you to sort the tests and search by different terms. 

![execution table](http://top-q.github.io/difido-reports/images/execution_table.png)


## Difido Report Server

The main features of the server are:
* Real time - No longer long waits for the end of the test execution to view the reports. The reports are generated on the fly.
* One central location for all the HTML reports with a web interface that allows searching and sorting the different executions
* Allows creating a single HTML report for number of machines running different executions. 
* Creating easy to use JSONS from the test results and stores in an embedded Elasticsearch database. 

![server](http://top-q.github.io/difido-reports/images/server.png)

## Getting started
Please refer to the project [Wiki](https://github.com/Top-Q/difido-reports/wiki) for more information and various guides.

## Support
Bugs and feature requests are acceptable via GitHub issues
