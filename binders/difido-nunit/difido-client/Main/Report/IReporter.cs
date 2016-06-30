using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;


namespace difido_client
{
    public interface IReporter
    {
      

        void Init(string outputFolder);

        void StartTest(ReporterTestInfo testInfo);

        void EndTest(ReporterTestInfo testInfo);

        void StartSuite(string suiteName);

        void EndSuite(string suiteName);

        void Report(string title, string message, ReporterTestInfo.TestStatus status, ReportElementType type);

        void AddTestProperty(string propertyName, string propertyValue);

    }

    public enum ReportElementType
    {
        regular,step,lnk,img,startLevel,stopLevel
    }

    public struct ReporterTestInfo
    {
        public enum TestStatus
        {
            success, failure, error, warning
        }
        private string testName;
        private string fullyQualifiedTestClassName;
        private TestStatus status;
        private string suiteName;
        private long durationTime;



        public string TestName
        {
            get { return testName; }
            set { testName = value; }
        }

        public string FullyQualifiedTestClassName
        {
            get { return fullyQualifiedTestClassName; }
            set { fullyQualifiedTestClassName = value; }
        }
        public TestStatus Status
        {
            get { return status; }
            set { status = value; }
        }

        public string SuiteName
        {
            get { return suiteName; }
            set { suiteName = value; }
        }

        public long DurationTime
        {
            get { return durationTime; }
            set { durationTime = value; }
        }
    }
}
