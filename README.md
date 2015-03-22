Difido Reports
================

This project aims to provide an HTML report and report server for various test automation frameworks.

The solution is composed of different components

## Difido NUnit
A binding for [NUnit](http://www.nunit.org/), the C# open source test automation framework. The binder allows creating a local HTML reports or/and send the reports to the Difido server 

## Difido TestNG
A binding for [TestNG](http://testng.org/doc/index.html), the popular Java test automation framework. At this stage allows creating of only local reports. 

## JSystem
The JSystem binding exists but it is part of the [JSystem](http://jsystem.org/) project. 

## Difido Report Server
The server allows creation of HTML reports in a central location instead of the local file system of each machine that executes the tests. 
The server allows connection using a simple REST API and receives the JSONS as the data. In addition, the server stores the data in an embedded [Elasticsearch](https://www.elastic.co/). This allow users to create complex queries on the tests results and even use solutions such as [Kibana](https://www.elastic.co/products/kibana) to visualize the data. 

The main features of the server are:
* One central location for all the HTML reports with a web interface that allows searching and sorting the different executions
* Allows creating a single HTML report for number of machines running different executions. 
* Creating easy to use JSONS from the test results and stores in an embedded Elasticsearch database. 
* Based on the extremely fast [Grizzly](https://grizzly.java.net/) framework. 

