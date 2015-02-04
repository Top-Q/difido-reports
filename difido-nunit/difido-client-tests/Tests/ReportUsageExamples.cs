using difido_client;
using difido_client_tests.Infra.difido_client_tests;
using NUnit.Framework;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace difido_client_tests
{
    [TestFixture]
    public class ReportUsageExamples
    {

        protected IReportDispatcher report = ReportManager.Instance;

        [Test]
        public void SimpleReport()
        {
            report.Report("My very simple report");
        }

        [Test]
        public void MoreInteretingReports()
        {
            report.Step("This is the first test step");
            report.Report("Click on me", "More interesting details");            
            report.Step("This is the second test step");
        }

        [Test]
        public void ReportLevels()
        {
            report.Step("Let's play with levels");
            report.StartLevel("Click on me to see more reports");
            report.Report("Something");
            report.Report("Something else");
            report.Report("Additional something");
            report.EndLevel();
        }

        [Test]
        public void TestWithError()
        {
            report.Report("About to fail the test with exception");
            throw new Exception("This is my error message");

        }

        [Test]
        public void TestWithFailure()
        {
            report.Report("About to fail the test with assertion");
            Assert.IsNotNull(null, "Failing the test with assertion");

        }

        [Test]
        public void TestWithScreenshot()
        {
            report.Step("About to add screenshot to the report");
            new ScreenCapture().CaptureScreenToFile("C:\\temp2.gif", ImageFormat.Png);
            report.ReportImage("My Image", "c:\\temp2.gif");
        }


    }
}
