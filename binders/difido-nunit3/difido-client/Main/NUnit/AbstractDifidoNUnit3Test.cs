using difido_client;
using difido_client.Main.Config;
using NUnit.Framework;
using NUnit.Framework.Interfaces;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace difido_client.Main.NUnit
{
    [TestFixture]
    public class AbstractDifidoNUnit3Test
    {
        protected static IReportDispatcher report = null;
        ReporterTestInfo testInfo;
        protected Stopwatch testStopwatch;

        [SetUp]
        public void BeforeAll()
        {
            report = ReportManager.Instance;

            TestContext.TestAdapter testAdapter = TestContext.CurrentContext.Test;

            testInfo = new ReporterTestInfo();

            testInfo.TestName = testAdapter.Name;
            testInfo.FullyQualifiedTestClassName = testAdapter.FullName;

            testInfo.Status = DifidoTestStatus.success;

            testStopwatch = new Stopwatch();
            testStopwatch.Start();

            report.StartTest(testInfo);

            //report.Report("This is from: AbstractDifidoNUnit3Test.BeforeAll");
        }

        [TearDown]
        public void AfterAll()
        {
            //report.Report("This is from: AbstractDifidoNUnit3Test.AfterAll");

            TestContext.ResultAdapter resultAdapter = TestContext.CurrentContext.Result;

            if (resultAdapter.Outcome == ResultState.Error)
            {
                testInfo.Status = DifidoTestStatus.error;
                report.Report(resultAdapter.Message, resultAdapter.StackTrace, DifidoTestStatus.error);
            }
            else if (resultAdapter.Outcome == ResultState.Success)
            {
                testInfo.Status = DifidoTestStatus.success;
            }
            else if (resultAdapter.Outcome == ResultState.Failure)
            {
                testInfo.Status = DifidoTestStatus.failure;
                report.Report(resultAdapter.Message, resultAdapter.StackTrace, DifidoTestStatus.failure);
            }
            else
            {
                testInfo.Status = DifidoTestStatus.warning;
            }

            testStopwatch.Stop();
            testInfo.DurationTime = testStopwatch.ElapsedMilliseconds;
            report.EndTest(testInfo);
        }

        public static void Report(string message)
        {
            report.Report(message);
        }

        public static void Report(string title, string message)
        {
            report.Report(title, message);
        }

        public static void ReportStep(string message)
        {
            report.Step(message);
        }

        public static void ReportWarning(string message)
        {
            report.Report(message, null, DifidoTestStatus.warning);
        }

        public static void ReportWarning(string title, string message)
        {
            report.Report(title, message, DifidoTestStatus.warning);
        }

        public static void ReportFail(string message)
        {
            report.Report(message, null, DifidoTestStatus.failure);
        }

        public static void ReportFail(string title, string message)
        {
            report.Report(title, message, DifidoTestStatus.failure);
        }

        public static void ReportError(string title, string message)
        {
            report.Report(title, message, DifidoTestStatus.error);
        }

        public static void ReportStartLevel(string levelTitle)
        {
            report.StartLevel(levelTitle);
        }

        public static void ReportEndLevel()
        {
            report.EndLevel();
        }
    }
}
