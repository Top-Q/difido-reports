using difido_client.Report.Excel;
using difido_client.Report.Html;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;


namespace difido_client
{
    public sealed class ReportManager : IReportDispatcher
    {
        private static volatile ReportManager instance;
        private static object syncRoot = new Object();
        private List<IReporter> reporters;
        private string outputFolder;

        private ReportManager() {
            reporters = new List<IReporter>();
            reporters.Add(new HtmlTestReporter());//TODO - This should be added dynamically from external file
            reporters.Add(new ExcelReporter());
            outputFolder = Directory.GetParent(System.IO.Directory.GetCurrentDirectory()).Parent.Parent.FullName + @"/TestResults/Report";
            try
            {
                System.IO.Directory.CreateDirectory(outputFolder);
            }
            catch (Exception e)
            {
                throw new Exception("Failed to create reports output folder", e);
            }

            
            Init(outputFolder);

        }

        public static ReportManager Instance
        {
            get
            {
                if (instance == null)
                {
                    lock (syncRoot)
                    {
                        if (instance == null)
                            instance = new ReportManager();
                    }
                }

                return instance;
            }
        }

  

  
        public void Init(string outputFolder)
        {
            foreach (IReporter reporter in reporters)
            {
                lock (syncRoot)
                {
                    reporter.Init(outputFolder);
                }

            }

        }


        public void StartTest(ReporterTestInfo testInfo)
        {
            foreach (IReporter reporter in reporters)
            {
                lock (syncRoot)
                {
                    reporter.StartTest(testInfo);
                }

            }

        }

        public void EndTest(ReporterTestInfo testInfo)
        {
            foreach (IReporter reporter in reporters)
            {
                lock (syncRoot)
                {
                    reporter.EndTest(testInfo);
                }

            }

        }

        public void StartSuite(string suiteName)
        {
            
        }

        public void EndSuite(string suiteName)
        {
            
        }


        public void Report(string title)
        {
            Report(title, null);
        }

        public void Report(string title, string message)
        {
            Report(title, message, ReporterTestInfo.TestStatus.success, ReportElementType.regular);
        }

        public void Report(string title, string message, bool success)
        {
            Report(title, message, success ? ReporterTestInfo.TestStatus.success : ReporterTestInfo.TestStatus.failure);
            
        }

        public void Report(string title, string message, ReporterTestInfo.TestStatus status)
        {
            Report(title, message, status, ReportElementType.regular);
        }

        public void Report(string title, string message, ReporterTestInfo.TestStatus status, ReportElementType type)
        {
            foreach (IReporter reporter in reporters)
            {
                lock (syncRoot)
                {
                    reporter.Report(title, message, status, type);
                }

            }
        }

        public void ReportFile(string title, string filePath)
        {
            Report(title, filePath, ReporterTestInfo.TestStatus.success, ReportElementType.lnk);
            
        }

        public void Step(string title)
        {
            Report(title, null, ReporterTestInfo.TestStatus.success, ReportElementType.step);
        }

        public void ReportImage(string title, string filePath)
        {
            Report(title, filePath, ReporterTestInfo.TestStatus.success, ReportElementType.img);
        }
        
        public void StartLevel(string description)
        {
            Report(description, null, ReporterTestInfo.TestStatus.success, ReportElementType.startLevel);
        }
        public void EndLevel()
        {
            Report(null, null, ReporterTestInfo.TestStatus.success, ReportElementType.stopLevel);
        }
    }


}
