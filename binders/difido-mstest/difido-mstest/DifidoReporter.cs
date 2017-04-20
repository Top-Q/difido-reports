using difido_client;
using System;
using static difido_client.ReporterTestInfo;

namespace difido_mstest
{
    public abstract class DifidoReporter
    {
        protected static IReportDispatcher report = ReportManager.Instance;

        protected static TestStatus currentTestStatus;

        protected static void Report(string message)
        {
            report.Report(message);
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
