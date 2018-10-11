using Difido.Model;

namespace Difido
{
    public interface IReporter
    {
        void Init(string outputFolder);

        void StartTest(StartTestInfo startTestInfo);

        void EndTest(EndTestInfo endTestInfo);

        void EndRun();

        void StartSuite(StartSuiteInfo startSuiteInfo);

        void EndSuite(EndSuiteInfo endSuiteInfo);

        void Report(string title, string message, TestStatus status, ReportElementType type);

        void AddTestProperty(string propertyName, string propertyValue);

    }

    
    public struct StartTestInfo
    {
        private string id;
        private string parentId;
        private string name;
        private string fullName;
        private string type;

        public string Id
        {
            get
            {
                return id;
            }

            set
            {
                id = value;
            }
        }

        public string ParentId
        {
            get
            {
                return parentId;
            }

            set
            {
                parentId = value;
            }
        }

        public string Name
        {
            get
            {
                return name;
            }

            set
            {
                name = value;
            }
        }

        public string FullName
        {
            get
            {
                return fullName;
            }

            set
            {
                fullName = value;
            }
        }

        public string Type
        {
            get
            {
                return type;
            }

            set
            {
                type = value;
            }
        }
    }



    public struct EndTestInfo
    {

        private string id;
        private string name;
        private string fullName;
        private string methodName;
        private string className;
        private string runState;
        private long seed;
        private TestStatus result;
        private string startTime;
        private string endTime;
        private double duration;
        private int asserts;
        private string parentId;

        public string Id
        {
            get
            {
                return id;
            }

            set
            {
                id = value;
            }
        }

        public string Name
        {
            get
            {
                return name;
            }

            set
            {
                name = value;
            }
        }

        public string FullName
        {
            get
            {
                return fullName;
            }

            set
            {
                fullName = value;
            }
        }

        public string MethodName
        {
            get
            {
                return methodName;
            }

            set
            {
                methodName = value;
            }
        }

        public string ClassName
        {
            get
            {
                return className;
            }

            set
            {
                className = value;
            }
        }

        public string RunState
        {
            get
            {
                return runState;
            }

            set
            {
                runState = value;
            }
        }

        public long Seed
        {
            get
            {
                return seed;
            }

            set
            {
                seed = value;
            }
        }

        public TestStatus Result
        {
            get
            {
                return result;
            }

            set
            {
                result = value;
            }
        }

        public string StartTime
        {
            get
            {
                return startTime;
            }

            set
            {
                startTime = value;
            }
        }

        public string EndTime
        {
            get
            {
                return endTime;
            }

            set
            {
                endTime = value;
            }
        }

        public double Duration
        {
            get
            {
                return duration;
            }

            set
            {
                duration = value;
            }
        }

        public int Asserts
        {
            get
            {
                return asserts;
            }

            set
            {
                asserts = value;
            }
        }

        public string ParentId
        {
            get
            {
                return parentId;
            }

            set
            {
                parentId = value;
            }
        }
    }

    public struct StartSuiteInfo
    {
        private string id;
        private string parentId;
        private string name;
        private string fullName;
        private string type;

        public string Id
        {
            get
            {
                return id;
            }

            set
            {
                id = value;
            }
        }

        public string ParentId
        {
            get
            {
                return parentId;
            }

            set
            {
                parentId = value;
            }
        }

        public string Name
        {
            get
            {
                return name;
            }

            set
            {
                name = value;
            }
        }

        public string FullName
        {
            get
            {
                return fullName;
            }

            set
            {
                fullName = value;
            }
        }

        public string Type
        {
            get
            {
                return type;
            }

            set
            {
                type = value;
            }
        }
    }

    public struct EndSuiteInfo
    {
        private string type;
        private string id;
        private string name;
        private string fullName;
        private string className;
        private string runState;
        private int testCaseCount;
        private TestStatus result;
        private string startTime;
        private string endTime;
        private double duration;
        private int total;
        private int passed;
        private int failed;
        private int warnings;
        private int inconclusive;
        private int skipeed;
        private int asserts;
        private string parentId;

        public string Type
        {
            get
            {
                return type;
            }

            set
            {
                type = value;
            }
        }

        public string Id
        {
            get
            {
                return id;
            }

            set
            {
                id = value;
            }
        }

        public string Name
        {
            get
            {
                return name;
            }

            set
            {
                name = value;
            }
        }

        public string FullName
        {
            get
            {
                return fullName;
            }

            set
            {
                fullName = value;
            }
        }

        public string ClassName
        {
            get
            {
                return className;
            }

            set
            {
                className = value;
            }
        }

        public string RunState
        {
            get
            {
                return runState;
            }

            set
            {
                runState = value;
            }
        }

        public int TestCaseCount
        {
            get
            {
                return testCaseCount;
            }

            set
            {
                testCaseCount = value;
            }
        }

        public TestStatus Result
        {
            get
            {
                return result;
            }

            set
            {
                result = value;
            }
        }

        public string StartTime
        {
            get
            {
                return startTime;
            }

            set
            {
                startTime = value;
            }
        }

        public string EndTime
        {
            get
            {
                return endTime;
            }

            set
            {
                endTime = value;
            }
        }

        public double Duration
        {
            get
            {
                return duration;
            }

            set
            {
                duration = value;
            }
        }

        public int Total
        {
            get
            {
                return total;
            }

            set
            {
                total = value;
            }
        }

        public int Passed
        {
            get
            {
                return passed;
            }

            set
            {
                passed = value;
            }
        }

        public int Failed
        {
            get
            {
                return failed;
            }

            set
            {
                failed = value;
            }
        }

        public int Warnings
        {
            get
            {
                return warnings;
            }

            set
            {
                warnings = value;
            }
        }

        public int Inconclusive
        {
            get
            {
                return inconclusive;
            }

            set
            {
                inconclusive = value;
            }
        }

        public int Skipeed
        {
            get
            {
                return skipeed;
            }

            set
            {
                skipeed = value;
            }
        }

        public int Asserts
        {
            get
            {
                return asserts;
            }

            set
            {
                asserts = value;
            }
        }

        public string ParentId
        {
            get
            {
                return parentId;
            }

            set
            {
                parentId = value;
            }
        }
    }
}
