using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using difido_client;
using System.Diagnostics;
using System.Reflection;
using static difido_client.ReporterTestInfo;

namespace difido_mstest
{
    [TestClass]
    public abstract class DifidoMSTest : DifidoReporter
    {
        private static Stopwatch testStopwatch;
        private static ReporterTestInfo testInfo;

        public TestContext TestContext { get; set; }

        private void TestStarted()
        {
            testInfo = new ReporterTestInfo();
            testInfo.TestName = TestContext.TestName;
            testInfo.FullyQualifiedTestClassName = TestContext.FullyQualifiedTestClassName;
            testInfo.Status = TestStatus.success;
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
    }
}
