using Difido.Model.Execution;
using Difido.Model.Test;
using Difido.Model;
using System;
using System.IO;
using System.Diagnostics;
using System.Collections.Generic;

namespace Difido.Main.Report.Reporters.HtmlTestReporter
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

        public void StartTest(StartTestInfo startTestInfo)
        {
            currentTest = new Test(index, startTestInfo.Name, executionUid + "-" + index);

            DateTime dateTime = DateTime.Now;
            currentTest.timestamp = dateTime.ToString("HH:mm:ss");
            currentTest.date = dateTime.ToString("yyyy/MM/dd");
            currentTest.className = startTestInfo.FullName;
            currentTest.description = startTestInfo.FullName;
            currentTest.AddProperty("Id",startTestInfo.Id);
            currentTest.AddProperty("ParentId", startTestInfo.ParentId);
            Scenario parentScenario = machine.GetScenarioChildWithId(startTestInfo.ParentId);
            parentScenario.AddChild(currentTest);
            ExecutionWasAddedOrUpdated(execution);
            testDetails = new TestDetails(currentTest.uid);
        }

        public void EndTest(EndTestInfo endTestInfo)
        {
            TestDetailsWereAdded(testDetails);
            currentTest.AddProperty("StartTime", endTestInfo.StartTime);
            currentTest.AddProperty("EndTime", endTestInfo.EndTime);
            currentTest.AddProperty("Asserts", ""+endTestInfo.Asserts);
            currentTest.AddProperty("RunState",  endTestInfo.RunState);
            currentTest.AddProperty("Seed", "" + endTestInfo.Seed);
            currentTest.status = endTestInfo.Result.ToString();
            currentTest.duration = (long)(endTestInfo.Duration * 100000);
            ExecutionWasAddedOrUpdated(execution);
            index++;
        }


        public void Report(ReportElement element)
        {
            
            if (null == testDetails)
            {
                Console.WriteLine("HTML reporter was not initiliazed propertly. No reports would be created.");
                return;
            }

            testDetails.AddReportElement(element);
            if (element.type  == "lnk" || element.type == "img")
            {
                if (File.Exists(element.message))
                {                    
                    string fileName = FileWasAdded(testDetails, element.message);
                    if (fileName != null)
                    {
                        element.message = fileName;
                    }


                }
            }
            /*
             * Since NUnit is sending all the output messages together, there is no use for this mehchanism
             * 
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
            */
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




        public virtual void StartSuite(StartSuiteInfo startSuiteInfo)
        {
            //TODO Find a way to get the planned tests from the suites. 
            if (!"TestFixture".Equals(startSuiteInfo.Type))
            {
                return;
            }

            Scenario scenario = new Scenario(startSuiteInfo.Name);
            if (null == scenario.scenarioProperties)
            {
                scenario.scenarioProperties = new Dictionary<string, string>();
            }
            scenario.scenarioProperties["Id"] = startSuiteInfo.Id;
            scenario.scenarioProperties["ParentId"] = startSuiteInfo.ParentId;
            scenario.scenarioProperties["FullName"] = startSuiteInfo.FullName;
            machine.AddChild(scenario);
        }

        #region unused
        public virtual void EndSuite(EndSuiteInfo endSuiteInfo)
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

        public void EndTest(StartTestInfo endTestInfo)
        {
            throw new NotImplementedException();
        }
    }
}
