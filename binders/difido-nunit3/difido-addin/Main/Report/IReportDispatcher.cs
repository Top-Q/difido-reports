
using Difido.Model;
using Difido.Model.Test;

namespace Difido
{
    public interface IReportDispatcher
    {
        void StartTest(StartTestInfo startTestInfo);

        void EndTest(EndTestInfo endTestInfo);

        void StartSuite(StartSuiteInfo startSuiteInfo);

        void EndSuite(EndSuiteInfo endSuiteInfo);

        void Report(ReportElement element);

        void AddTestProperty(string propertyName, string propertyValue);

        void EndRun();
    }
}
