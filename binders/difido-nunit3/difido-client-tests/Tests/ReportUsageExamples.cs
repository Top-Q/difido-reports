
using difidoClient;
using NUnit.Framework;
using System;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Windows.Forms;

namespace difido_client_tests
{
    [TestFixture]
    public class ReportUsageExamples
    {

        protected IReport report = new DifidoReporter();

        [SetUp]
        public void SetUp()
        {
            string testId= TestContext.CurrentContext.Test.ID;
        }
        
        [Test]
        public void SimpleReport()
        {
            Console.WriteLine("Something");
            //report.Report("My very simple report");
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
        public void TestWithSoftFailure()
        {
            report.Report("Message before failure");
            report.Report("Soft failure", "Soft Failure Message", false);
            report.Report("Message after failure");
        }

        [Test]
        public void TestMultipleAssertions()
        {
            

            Assert.Multiple(() =>
            {
                Assert.AreEqual(5.2, 6, "First Assertion");
                Assert.AreEqual(3.9, 4, "Second assertion");
            });
        }


        [Test]
        public void TestWithScreenshot0()
        {            
            CaptureScreenshot("TestWithScreenshot0");
        }

        [Test]
        public void TestWithScreenshot1()
        {            
            CaptureScreenshot("TestWithScreenshot1");
        }

        private void CaptureScreenshot(string fileName)
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
            fileName = System.IO.Path.GetTempPath() + fileName + ".png";
            File.Delete(fileName);
            bmpScreenshot.Save(fileName, ImageFormat.Png);
            report.ReportImage("screenshot", fileName);
            
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
