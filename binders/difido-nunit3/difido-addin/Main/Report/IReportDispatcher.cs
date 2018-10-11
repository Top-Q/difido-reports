
namespace difido_client
{
    public interface IReportDispatcher
    {
        void StartTest(StartTestInfo startTestInfo);

        void EndTest(EndTestInfo endTestInfo);

        void StartSuite(StartSuiteInfo startSuiteInfo);

        void EndSuite(EndSuiteInfo endSuiteInfo);

        void Report(string title);

        void Report(string title, string message);

        void Report(string title, string message, bool status);

        void Report(string title, string message, TestStatus status);

        void Report(string title, string message, TestStatus status, ReportElementType type);

        void ReportFile(string title, string filePath);

        void ReportImage(string title, string filePath);

        void StartLevel(string description);

        void EndLevel();

        void Step(string title);

        void AddTestProperty(string propertyName, string propertyValue);

        void EndRun();
    }
}
