using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.Diagnostics;
using static difido_client.ReporterTestInfo;
using System.Collections.Generic;

namespace difido_client.MSTest

{
    [TestClass]
    public abstract class AbstractDifidoMSTest
    {
        protected static IReportDispatcher report = ReportManager.Instance;
        private static TestStatus currentTestStatus;
        private static Stopwatch testStopwatch;
        private static ReporterTestInfo testInfo;

        protected Dictionary<string, string> TestParameters;

        public TestContext TestContext { get; set; }

        private void TestStarted(Dictionary<string, string> paramsForTestName = null)
        {
            testInfo = new ReporterTestInfo();
            testInfo.TestName = TestContext.TestName;

            if (paramsForTestName != null)
            {
                testInfo.TestName += " ( ";
                foreach (KeyValuePair<string, string> paramKeyValue in paramsForTestName)
                {
                    testInfo.TestName += paramKeyValue.Key + "=" + paramKeyValue.Value + "; ";
                }
                testInfo.TestName += " )";
            }

            testInfo.FullyQualifiedTestClassName = TestContext.FullyQualifiedTestClassName;
            testInfo.Status = TestStatus.success;
            currentTestStatus = TestStatus.success;
            report.StartTest(testInfo);
            testStopwatch = new Stopwatch();
            testStopwatch.Start();

            if (TestParameters != null)
                AddTestProperties(TestParameters);
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

        protected void RunTest(Action TestBody, Dictionary<string, string> paramsForTestName = null)
        {
            try
            {
                TestStarted(paramsForTestName);
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

        public static void Report(string message)
        {
            report.Report(message);
        }

        public static void Report(string title, string message)
        {
            report.Report(title, message);
        }

        public static void ReportStep(string message)
        {
            report.Step(message);
        }

        public static void ReportWarning(string message)
        {
            report.Report(message, null, TestStatus.warning);
            UpdateCurrentTestStatus(TestStatus.warning);
        }

        public static void ReportWarning(string title, string message)
        {
            report.Report(title, message, TestStatus.warning);
            UpdateCurrentTestStatus(TestStatus.warning);
        }

        public static void ReportFail(string message)
        {
            report.Report(message, null, TestStatus.failure);
            UpdateCurrentTestStatus(TestStatus.failure);
        }

        public static void ReportFail(string title, string message)
        {
            report.Report(title, message, TestStatus.failure);
            UpdateCurrentTestStatus(TestStatus.failure);
        }

        public static void ReportError(string title, string message)
        {
            report.Report(title, message, TestStatus.error);
            UpdateCurrentTestStatus(TestStatus.error);
        }

        public static void ReportStartLevel(string levelTitle)
        {
            report.StartLevel(levelTitle);
        }

        public static void ReportEndLevel()
        {
            report.EndLevel();
        }

        public static void AddTestProperty(string propertyName, string propertyValue)
        {
            report.AddTestProperty(propertyName, propertyValue);
        }

        public static void AddTestProperties(Dictionary<string, string> properties)
        {
            foreach (var keyValue in properties)
            {
                report.AddTestProperty(keyValue.Key, keyValue.Value);
            }
        }

        public static void UpdateCurrentTestStatus(TestStatus testStatus)
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

        public Dictionary<string, string> GetParamsFromDataSource(params string[] paramNames)
        {
            Dictionary<string, string> paramsFromDataSource = new Dictionary<string, string>();

            foreach(string paramName in paramNames)
            {
                paramsFromDataSource[paramName] = TestContext.DataRow[paramName].ToString();
            }

            return paramsFromDataSource;
        }
    }
}
