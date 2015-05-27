using difido_client.Report.Html.Model;
using difido_client.Utils;
using System.IO.Compression;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Web.Script.Serialization;

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

        public virtual void Init(string outputFolder)
        {
            GenerateUid();
            machine = new Machine(System.Environment.MachineName);
            execution = new Execution();
            execution.AddMachine(machine);
            MachineWasAdded(machine);


        }

        private void GenerateUid()
        {
            executionUid = new Random().Next(0, 1000) + (DateTime.Now.Ticks / TimeSpan.TicksPerMinute).ToString();
        }

        public void StartTest(ReporterTestInfo testInfo)
        {

            currentTest = new Test(index, testInfo.TestName, executionUid + "-" + index);
            currentTest.timestamp = DateTime.Now.ToString("yyyy/MM/dd HH:mm:ss");
            string scenarioName = testInfo.FullyQualifiedTestClassName.Split('.')[testInfo.FullyQualifiedTestClassName.Split('.').Length - 2];
            Scenario scenario;
            if (machine.IsChildWithNameExists(scenarioName))
            {
                scenario = (Scenario)machine.GetChildWithName(scenarioName);
            }
            else
            {
                scenario = new Scenario(scenarioName);
                machine.AddChild(scenario);
            }
            scenario.AddChild(currentTest);
            ExecutionWasAddedOrUpdated(execution);
            testDetails = new TestDetails(testInfo.TestName, currentTest.uid);
            testDetails.description = testInfo.FullyQualifiedTestClassName;
            testDetails.timestamp = DateTime.Now.ToString("yyyy/MM/dd HH:mm:ss");
        }

        public void EndTest(ReporterTestInfo testInfo)
        {

            currentTest.status = testInfo.Status.ToString();
            currentTest.duration = testInfo.DurationTime;
            testDetails.duration = testInfo.DurationTime;
            ExecutionWasAddedOrUpdated(execution);            
            index++;
        }


        public void Report(string title, string message, ReporterTestInfo.TestStatus status, ReportElementType type)
        {
            ReportElement element = new ReportElement();
            if (null == testDetails)
            {
                Console.WriteLine("HTML reporter was not initiliazed propertly. No reports would be created.");
                return;
            }
            testDetails.AddReportElement(element);
            element.title = title;
            element.message = message;
            element.time = DateTime.Now.ToString("HH:mm:ss");
            element.status = status.ToString();
            element.type = type.ToString();

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

            TestDetailsWereAdded(testDetails);
        }

        public void AddTestProperty(string propertyName, string propertyValue)
        {
            if (null == testDetails)
            {
                Console.WriteLine("HTML reporter was not initiliazed propertly. No reports would be created.");
                return;
            }
            testDetails.AddProperty(propertyName, propertyValue);
            TestDetailsWereAdded(testDetails);
        }


        #region abstractMethod

        protected abstract void TestDetailsWereAdded(TestDetails testDetails);

        protected abstract void ExecutionWasAddedOrUpdated(Execution execution);

        protected abstract string FileWasAdded(TestDetails testDetails, string file);

        protected abstract void MachineWasAdded(Machine machine);

        #endregion



        #region unused
        public void StartSuite(string suiteName)
        {
        }

        public void EndSuite(string suiteName)
        {
        }
        #endregion

        #region properties

        protected string ExecutionUid
        {
            get {return executionUid;}

        }

        protected Execution CurrentExecution
        {
            get { return execution; }
        }

        #endregion


    }
}
