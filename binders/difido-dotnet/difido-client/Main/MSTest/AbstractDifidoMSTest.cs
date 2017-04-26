using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.Diagnostics;
using static difido_client.ReporterTestInfo;

namespace difido_client.MSTest

{
    [TestClass]
    public abstract class AbstractDifidoMSTest
    {
        protected static IReportDispatcher report = ReportManager.Instance;
        private static TestStatus currentTestStatus;
        private static Stopwatch testStopwatch;
        private static ReporterTestInfo testInfo;

        public TestContext TestContext { get; set; }

        private void TestStarted()
        {
            testInfo = new ReporterTestInfo()
            {
                TestName = TestContext.TestName,
                FullyQualifiedTestClassName = TestContext.FullyQualifiedTestClassName,
                Status = TestStatus.success
            };
            currentTestStatus = TestStatus.success;
            report.StartTest(testInfo);
            testStopwatch = new Stopwatch();
            testStopwatch.Start();
        }

        private void TestFinished()
        {
            testInfo.Status = currentTestStatus;
            testStopwatch.Stop();
            testInfo.DurationTime = testStopwatch.ElapsedMilliseconds;
            report.EndTest(testInfo);

            if (currentTestStatus == TestStatus.failure)
            {
                throw new Exception("The test has failed; See test report for details.");
            }
        }

        protected void RunTest(Action TestBody)
        {
            try
            {
                TestStarted();
                TestBody();
            }
            catch(Exception ex)
            {
                ReportError(ex.Message, ex.StackTrace);
                throw (ex);
            }
            finally
            {
                TestFinished();
            }
        }

        protected static void Report(string message)
        {
            report.Report(message);
        }

        protected static void Report(string title, string message)
        {
            report.Report(title, message);
        }

        protected static void ReportStep(string message)
        {
            report.Step(message);
        }

        protected static void ReportWarning(string message)
        {
            report.Report(message, null, TestStatus.warning);
            UpdateCurrentTestStatus(TestStatus.warning);
        }

        protected static void ReportWarning(string title, string message)
        {
            report.Report(title, message, TestStatus.warning);
            UpdateCurrentTestStatus(TestStatus.warning);
        }

        protected static void ReportFail(string message)
        {
            report.Report(message, null, TestStatus.failure);
            UpdateCurrentTestStatus(TestStatus.failure);
        }

        protected static void ReportFail(string title, string message)
        {
            report.Report(title, message, TestStatus.failure);
            UpdateCurrentTestStatus(TestStatus.failure);
        }

        protected static void ReportError(string title, string message)
        {
            report.Report(title, message, TestStatus.error);
            UpdateCurrentTestStatus(TestStatus.error);
        }

        protected static void ReportStartLevel(string levelTitle)
        {
            report.StartLevel(levelTitle);
        }

        protected static void ReportEndLevel()
        {
            report.EndLevel();
        }

        private static void UpdateCurrentTestStatus(TestStatus testStatus)
        {
            if (testStatus == TestStatus.warning && currentTestStatus != TestStatus.failure && currentTestStatus != TestStatus.error)
            {
                currentTestStatus = TestStatus.warning;
            }
            else if (testStatus == TestStatus.failure && currentTestStatus != TestStatus.error)
            {
                currentTestStatus = TestStatus.failure;
            }
            else if (testStatus == TestStatus.error)
            {
                currentTestStatus = TestStatus.error;
            }
        }
    }
}
