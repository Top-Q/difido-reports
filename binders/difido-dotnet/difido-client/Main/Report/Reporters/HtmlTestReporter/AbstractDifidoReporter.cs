﻿using difido_client.Report.Html.Model;
using System;
using System.IO;
using System.Diagnostics;

namespace difido_client.Main.Report.Reporters.HtmlTestReporter
{
    public abstract class AbstractDifidoReporter : IReporter
    {
        private Execution execution;
        private Test currentTest;
        private int index;
        private string executionUid;
        private TestDetails testDetails;
        private Machine machine;
        private Stopwatch stopwatch;

        public virtual void Init(string outputFolder)
        {
            GenerateUid();
            machine = new Machine(System.Environment.MachineName);
            execution = new Execution();
            execution.AddMachine(machine);
            MachineWasAdded(machine);
            stopwatch = new Stopwatch();


        }

        private void GenerateUid()
        {
            executionUid = new Random().Next(0, 1000) + (DateTime.Now.Ticks / TimeSpan.TicksPerMinute).ToString();
        }

        public void StartTest(ReporterTestInfo testInfo)
        {
            currentTest = new Test(index, testInfo.TestName, executionUid + "-" + index);
            DateTime dateTime = DateTime.Now;
            currentTest.timestamp = dateTime.ToString("HH:mm:ss");
            currentTest.date = dateTime.ToString("yyyy/MM/dd");
            currentTest.className = testInfo.FullyQualifiedTestClassName;
            currentTest.description = testInfo.FullyQualifiedTestClassName;
            string scenarioName = testInfo.FullyQualifiedTestClassName.Split('.')[testInfo.FullyQualifiedTestClassName.Split('.').Length - 2];
            Scenario scenario;
            if (machine.IsChildWithNameExists(scenarioName))
            {
                scenario = (Scenario)machine.GetChildWithName(scenarioName);
            }
            else
            {
                scenario = new Scenario(scenarioName);
                if (machine.children != null)
                {
                    // We need to copy all the properties from the first scenario. 
                    // Failing to do so will cause that the tests in the ElsaticSearch, for example, will not have properties.
                    scenario.scenarioProperties = ((Scenario)machine.children[0]).scenarioProperties;
                }
                machine.AddChild(scenario);
            }
            scenario.AddChild(currentTest);
            ExecutionWasAddedOrUpdated(execution);
            testDetails = new TestDetails(currentTest.uid);

        }

        public void EndTest(ReporterTestInfo testInfo)
        {
            TestDetailsWereAdded(testDetails);
            currentTest.status = testInfo.Status.ToString();
            currentTest.duration = testInfo.DurationTime;
            ExecutionWasAddedOrUpdated(execution);
            index++;
        }


        public void Report(string title, string message, DifidoTestStatus status, ReportElementType type)
        {
            ReportElement element = new ReportElement();
            if (null == testDetails)
            {
                Console.WriteLine("HTML reporter was not initiliazed propertly. No reports would be created.");
                return;
            }

            element.title = title;
            element.message = message;
            element.time = DateTime.Now.ToString("HH:mm:ss");
            element.status = status.ToString();
            element.type = type.ToString();
            testDetails.AddReportElement(element);
            if (type == ReportElementType.lnk || type == ReportElementType.img)
            {
                if (File.Exists(message))
                {
                    string fileName = FileWasAdded(testDetails, message);
                    if (fileName != null)
                    {
                        element.message = fileName;
                    }


                }
            }

            // The stopwatch is an important mechanism that helps when test is creating a large number of message in short time intervals.
            if (!stopwatch.IsRunning)
            {
                stopwatch.Start();
            }
            else
            {
                if (stopwatch.ElapsedMilliseconds <= 100)
                {
                    return;
                }
            }
            stopwatch.Restart();

            TestDetailsWereAdded(testDetails);

        }

        public void AddTestProperty(string propertyName, string propertyValue)
        {
            if (null == testDetails)
            {
                Console.WriteLine("HTML reporter was not initiliazed propertly. No reports would be created.");
                return;
            }
            currentTest.AddProperty(propertyName, propertyValue);
            ExecutionWasAddedOrUpdated(CurrentExecution);
        }


        #region abstractMethod

        protected abstract void TestDetailsWereAdded(TestDetails testDetails);

        protected abstract void ExecutionWasAddedOrUpdated(Execution execution);

        protected abstract string FileWasAdded(TestDetails testDetails, string file);

        protected abstract void MachineWasAdded(Machine machine);

        #endregion




        public virtual void StartSuite(string suiteName, int testCount)
        {
            machine.plannedTests = testCount;
        }

        #region unused
        public virtual void EndSuite(string suiteName)
        {
        }
        #endregion

        #region properties

        protected string ExecutionUid
        {
            get { return executionUid; }

        }

        protected Execution CurrentExecution
        {
            get { return execution; }
        }

        #endregion

        public void EndRun()
        {
            //Not used
        }

    }
}
