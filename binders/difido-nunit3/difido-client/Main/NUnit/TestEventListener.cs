
/**
using NUnit.Core;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;


namespace difido_client.Nunit
{

    public class TestEventListener : EventListener
    {
        protected static IReportDispatcher report = ReportManager.Instance;
        private Stopwatch testStopwatch;
        private ReporterTestInfo testInfo;
        private int testCount;
        
        public void RunStarted(string name, int testCount)
        {
            this.testCount = testCount;
        }
        
        public void RunFinished(TestResult result) { }
        public void RunFinished(Exception exception) { }
        public void TestStarted(TestName testName)
        {
            testInfo = new ReporterTestInfo();
            testInfo.TestName = testName.Name;
            testInfo.FullyQualifiedTestClassName = testName.FullName;
            testInfo.Status = ReporterTestInfo.TestStatus.success;
            report.StartTest(testInfo);
            testStopwatch = new Stopwatch();
            testStopwatch.Start();
            

        }
        public void TestFinished(TestResult result)
        {
            switch (result.ResultState)
            {
                case ResultState.Error:
                    {
                        testInfo.Status = ReporterTestInfo.TestStatus.error;
                        report.Report(result.Message, result.StackTrace, ReporterTestInfo.TestStatus.error);
                        break;
                    }
                case ResultState.Success:
                    {
                        testInfo.Status = ReporterTestInfo.TestStatus.success;
                        break;
                    }
                case ResultState.Failure:
                    {
                        testInfo.Status = ReporterTestInfo.TestStatus.failure;
                        report.Report(result.Message, result.StackTrace, ReporterTestInfo.TestStatus.failure);
                        break;
                    }
                default:
                    {
                        testInfo.Status = ReporterTestInfo.TestStatus.warning;
                        break;
                    }
            }

            testStopwatch.Stop();
            testInfo.DurationTime = testStopwatch.ElapsedMilliseconds;
            report.EndTest(testInfo);
        }
        public void SuiteStarted(TestName testName)
        {
            report.StartSuite(null, testCount);
        }
        public void SuiteFinished(TestResult result)
        {
            report.EndSuite(null);
        }
        public void UnhandledException(Exception exception)
        {
         
        }
        public void TestOutput(TestOutput testOutput)
        {

        }
    }
}
*/
