using difido_client.Report.Html;
using System;
using System.Collections;
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
        private static List<string> errorsList = new List<string>();

        private ReportManager() {
            reporters = new List<IReporter>();
            reporters.Add(new HtmlTestReporter());//TODO - This should be added dynamically from external file
            //reporters.Add(new RemoteHtmlReporter());              
            //outputFolder = Directory.GetParent(System.IO.Directory.GetCurrentDirectory()).Parent.Parent.FullName + @"/TestResults/Report";
            outputFolder = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile) + @"/Desktop";
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


        public void StartTest(StartTestInfo startTestInfo)
        {
            foreach (IReporter reporter in reporters)
            {
                lock (syncRoot)
                {
                    reporter.StartTest(startTestInfo);
                }
            }
            errorsList.Clear();
            

        }

        public void EndTest(EndTestInfo endTestInfo)
        {
            foreach (IReporter reporter in reporters)
            {
                lock (syncRoot)
                {
                    if (errorsList.Count > 0)
                    {
                        Report("Errors during the test", ConvertListToString(errorsList));
                    }

                    reporter.EndTest(endTestInfo);
                }

            }

        }




        public void StartSuite(StartSuiteInfo startSuiteInfo)
        {
            foreach (IReporter reporter in reporters)
            {
                lock (syncRoot)
                {
                    reporter.StartSuite(startSuiteInfo);
                }
            }

            
        }

        public void EndSuite(EndSuiteInfo endSuiteInfo)
        {
            foreach (IReporter reporter in reporters)
            {
                lock (syncRoot)
                {
                    reporter.EndSuite(endSuiteInfo);

                }

            }            
        }

        public void EndRun()
        {
            foreach (IReporter reporter in reporters)
            {
                lock (syncRoot)
                {
                    reporter.EndRun();

                }

            }
        }


        public void ReportError(params object[] args)
        {
            var info = ConvertStringArgsToFormatterAndValues(args);
            var title = string.Format(info[0].ToString(), (string[])info[1]);

            lock (syncRoot)
            {
                errorsList.Add(title);
            }

            Report(title, "");
        }


        public void Report(string title)
        {
            Report(title, null);
        }

        public void Report(string title, string message)
        {
            Report(title, message, TestStatus.success, ReportElementType.regular);
        }

        public void Report(string title, string message, bool success)
        {
            Report(title, message, success ? TestStatus.success : TestStatus.failure);
            
        }

        public void Report(string title, string message, TestStatus status)
        {
            Report(title, message, status, ReportElementType.regular);
        }

        public void Report(string title, string message, TestStatus status, ReportElementType type)
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
            Report(title, filePath, TestStatus.success, ReportElementType.lnk);
            
        }

        public void Step(string title)
        {
            Report(title, null, TestStatus.success, ReportElementType.step);
        }

        public void ReportImage(string title, string filePath)
        {
            Report(title, filePath, TestStatus.success, ReportElementType.img);
        }
        
        public void StartLevel(string description)
        {
            Report(description, null, TestStatus.success, ReportElementType.startLevel);
        }
        public void EndLevel()
        {
            Report(null, null, TestStatus.success, ReportElementType.stopLevel);
        }

        public void AddTestProperty(string propertyName, string propertyValue)
        {
            foreach (IReporter reporter in reporters)
            {
                lock (syncRoot)
                {
                    reporter.AddTestProperty(propertyName, propertyValue);
                }

            }

        }

        private static string ConvertListToString(IEnumerable SourceList)
        {
            StringBuilder sb = new StringBuilder();
            foreach (var item in SourceList)
            {

                sb.AppendFormat("{0} <br />", item);
            }
            return sb.ToString();
        }

        private static List<object> ConvertStringArgsToFormatterAndValues(params object[] args)
        {
            List<object> FormatterAndArgs = new List<object>();

            try
            {
                FormatterAndArgs.Add(args[0].ToString());
                string[] stringArgs = new string[args.Count() - 1];
                for (int i = 1; i < args.Count(); i++)
                {
                    stringArgs[i - 1] = args[i] != null ? args[i].ToString() : "";
                }
                FormatterAndArgs.Add(stringArgs);
            }
            catch             {

                //ErrorFormat("ConvertStringArgsToFormatterAndValues threw exception :{0}", ex);
            }
            return FormatterAndArgs;
        }

    }


}
