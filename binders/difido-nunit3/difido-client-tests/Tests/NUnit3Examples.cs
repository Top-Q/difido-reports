using difido_client;
using difido_client.Main.NUnit;
using difido_client_tests.Infra.difido_client_tests;
using NUnit.Framework;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace difido_client_tests
{
    [TestFixture]
    public class NUnit3Examples : AbstractDifidoNUnit3Test
    {

        protected IReportDispatcher report = ReportManager.Instance;

        [SetUp]
        public void SetUp()
        {
            report.Report("This is from: NUnit3Examples.SetUp");
        }

        [TearDown]
        public void TearDown()
        {
            report.Report("This is from: NUnit3Examples.TearDown");
        }

        [Test]
        public void SimpleReport()
        {            
            report.Report("Sleeping 5 seconds");
            Thread.Sleep(5000);
            report.Report("And this is the end");
        }

        [Test]
        public void FailTest()
        {
            report.Report("OK...");
            Assert.Fail("This is not good!");
            report.Report("OK...");
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
        public void ReportLevelsWithException()
        {
            report.Step("Let's play with levels");
            report.StartLevel("Click on me to see more reports");
            report.Report("Something");
            report.Report("Something else");
            report.Report("Additional something");
            report.EndLevel();
            report.StartLevel("Click on me to see more reports");
            report.Report("Something");
            report.Report("Something else");
            throw new Exception("An exception");
        }

        [Test]
        public void TestWithError()
        {
            report.Report("About to fail the test with exception");
            throw new Exception("This is my error message");
        }

        [Test]
        public void TestWithManyReportElements()
        {
            for (int i = 0; i < 100; i++)
            {
                report.Report("Report element with index "+i);
            }
        }

        [Test]
        public void TestWithFailure()
        {
            report.Report("About to fail the test with assertion");
            Assert.IsNotNull(null, "Failing the test with assertion");
        }

        [Test]
        public void TestWithScreenshot0()
        {            
            CaptureScreenshot();
        }

        [Test]
        public void TestWithScreenshot1()
        {            
            CaptureScreenshot();
        }

        private void CaptureScreenshot()
        {
            var bmpScreenshot = new Bitmap(Screen.PrimaryScreen.Bounds.Width,
                               Screen.PrimaryScreen.Bounds.Height,
                               PixelFormat.Format32bppArgb);

            // Create a graphics object from the bitmap.
            var gfxScreenshot = Graphics.FromImage(bmpScreenshot);

            // Take the screenshot from the upper left corner to the right bottom corner.
            gfxScreenshot.CopyFromScreen(Screen.PrimaryScreen.Bounds.X,
                                        Screen.PrimaryScreen.Bounds.Y,
                                        0,
                                        0,
                                        Screen.PrimaryScreen.Bounds.Size,
                                        CopyPixelOperation.SourceCopy);

            // Save the screenshot to the specified path that the user has chosen.
            string fileName = System.IO.Path.GetTempPath() + Guid.NewGuid().ToString() + ".png";
            bmpScreenshot.Save(fileName, ImageFormat.Png);
            report.ReportImage("screenshot", fileName);
            File.Delete(fileName);
        }

        [Test]
        public void TestTestProperties()
        {
            report.Step("About to add test properties to the report");
            for (int i = 0; i < 10; i++)
            {
                report.AddTestProperty("prop" + i, "some kind of value" + i);
            }
        }


    }
}
