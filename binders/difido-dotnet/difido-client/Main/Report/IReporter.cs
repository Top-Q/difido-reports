namespace difido_client
{
    public interface IReporter
    {
        void Init(string outputFolder);

        void StartTest(ReporterTestInfo testInfo);

        void EndTest(ReporterTestInfo testInfo);

        void EndRun();

        void StartSuite(string suiteName, int testCount);

        void EndSuite(string suiteName);

        void Report(string title, string message, DifidoTestStatus status, ReportElementType type);

        void AddTestProperty(string propertyName, string propertyValue);
    }

    public enum DifidoTestStatus
    {
        success, failure, error, warning
    }

    public enum ReportElementType
    {
        regular,step,lnk,img,startLevel,stopLevel
    }

    public struct ReporterTestInfo
    {
        private string testName;
        private string fullyQualifiedTestClassName;
        private DifidoTestStatus status;
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
        public DifidoTestStatus Status
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
