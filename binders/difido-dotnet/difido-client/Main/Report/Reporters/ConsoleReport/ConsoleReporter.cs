using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace difido_client.Main.Report.Reporters.ConsoleReport
{
    public class ConsoleReporter : IReporter
    {
        public void AddTestProperty(string propertyName, string propertyValue)
        {
        }

        public void EndRun()
        {
        }

        public void EndSuite(string suiteName)
        {
        }

        public void EndTest(ReporterTestInfo testInfo)
        {
            Print("------------------------------------------------------------------------");
            Print("[TEST END]: " + testInfo.TestName);
            Print("Test duration: " + testInfo.DurationTime + " milliseconds");
            Print("Test status: " + testInfo.Status);
            Print("------------------------------------------------------------------------");
        }

        public void Init(string outputFolder)
        {
        }

        public void Report(string title, string message, ReporterTestInfo.TestStatus status, ReportElementType type)
        {
            StringBuilder sb = new StringBuilder();

            switch (type)
            {
                case ReportElementType.regular:
                    break;
                case ReportElementType.startLevel:
                    sb.Append("[START LEVEL]: ");
                    break;
                case ReportElementType.step:
                    sb.Append("[STEP]: ");
                    break;
                case ReportElementType.stopLevel:
                    sb.Append("[STOP LEVEL]");
                    break;
                case ReportElementType.img:
                    sb.Append("[IMAGE]: ");
                    break;
                case ReportElementType.lnk:
                    sb.Append("[LINK]: ");
                    break;
            }

            switch (status)
            {
                case ReporterTestInfo.TestStatus.success:
                    break;
                case ReporterTestInfo.TestStatus.warning:
                    sb.Append("[WARNING]: ");
                    break;
                case ReporterTestInfo.TestStatus.error:
                    sb.Append("[ERROR]: ");
                    break;
                case ReporterTestInfo.TestStatus.failure:
                    sb.Append("[FAILURE]: ");
                    break;
            }

            if (title != null && !title.Equals(""))
            {

                if (message != null && !message.Equals(""))
                {
                    sb.Append(title + " - " + message);
                }
                else
                {
                    sb.Append(title);
                }
            }

            Print(sb.ToString());
        }

        public void StartSuite(string suiteName, int testCount)
        {
        }

        public void StartTest(ReporterTestInfo testInfo)
        {
            Print("------------------------------------------------------------------------");
            Print("[TEST START]: " + testInfo.TestName);
            Print("------------------------------------------------------------------------");
        }

        private void Print(String message)
        {
            Console.WriteLine(DateTime.Now.ToString("HH:mm:ss: ") + message);
        }
    }
}
