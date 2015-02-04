using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;


namespace difido_client
{
    public interface IReportDispatcher
    {
        void StartTest(ReporterTestInfo testInfo);

        void EndTest(ReporterTestInfo testInfo);

        void StartSuite(string suiteName);

        void EndSuite(string suiteName);

        void Report(string title);

        void Report(string title, string message);

        void Report(string title, string message, bool status);

        void Report(string title, string message, ReporterTestInfo.TestStatus status);

        void Report(string title, string message, ReporterTestInfo.TestStatus status, ReportElementType type);

        void ReportFile(string title, string filePath);

        void ReportImage(string title, string filePath);

        void StartLevel(string description);

        void EndLevel();

        void Step(string title);
    }
}
