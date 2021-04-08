using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Windows.Forms;

namespace difido_client.MSTest

{
    [TestClass]
    public abstract class AbstractDifidoMSTest
    {
        protected static IReportDispatcher report = ReportManager.Instance;
        private static Stopwatch testStopwatch;
        public static ReporterTestInfo testInfo;
        protected Dictionary<string, string> TestParameters;
        protected string NiceTestName = null;

        public TestContext TestContext { get; set; }

        private void TestStarted(Dictionary<string, string> paramsForTestName = null)
        {
            testInfo = new ReporterTestInfo();
            if (NiceTestName != null)
            {
                testInfo.TestName = NiceTestName;
            }
            else
            {
                testInfo.TestName = TestContext.TestName;
            }

            if (paramsForTestName != null)
            {
                testInfo.TestName += " ( ";
                foreach (KeyValuePair<string, string> paramKeyValue in paramsForTestName)
                {
                    testInfo.TestName += paramKeyValue.Key + "=" + paramKeyValue.Value + "; ";
                }
                testInfo.TestName += " )";
            }

            testInfo.FullyQualifiedTestClassName = TestContext.FullyQualifiedTestClassName;
            testInfo.Status = DifidoTestStatus.success;
            report.StartTest(testInfo);
            testStopwatch = new Stopwatch();
            testStopwatch.Start();

            AddTestProperties(TestParameters);
        }

        private void TestFinished()
        {
            testStopwatch.Stop();
            testInfo.DurationTime = testStopwatch.ElapsedMilliseconds;
            report.EndTest(testInfo);

            if (testInfo.Status == DifidoTestStatus.failure)
            {
                throw new Exception("The test has failed; See test report for details.");
            }
        }

        protected void RunTest(Action TestBody, Dictionary<string, string> paramsForTestName = null)
        {
            try
            {
                TestStarted(paramsForTestName);
                TestBody();
            }
            catch (Exception ex)
            {
                TakeScreenshot(ex.Message);
                ReportError(ex.Message, ex.StackTrace);
                throw (ex);
            }
            finally
            {
                TestFinished();
            }
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

        public static void AddTestProperty(string propertyName, string propertyValue)
        {
            report.AddTestProperty(propertyName, propertyValue);
        }

        public static void AddTestProperties(Dictionary<string, string> properties)
        {
            if (properties != null)
            {
                foreach (var keyValue in properties)
                {
                    report.AddTestProperty(keyValue.Key, keyValue.Value);
                }
            }
        }

        public Dictionary<string, string> GetParamsFromDataSource(params string[] paramNames)
        {
            Dictionary<string, string> paramsFromDataSource = new Dictionary<string, string>();

            foreach (string paramName in paramNames)
            {
                paramsFromDataSource[paramName] = TestContext.DataRow[paramName].ToString();
            }

            return paramsFromDataSource;
        }

        protected void TakeScreenshot()
        {
            TakeScreenshot("screenshot_" + DateTime.Now.ToString("yyyy-MM-dd_HH-mm-ss"), "screenshot");
        }

        protected void TakeScreenshot(string message)
        {
            TakeScreenshot("screenshot_" + DateTime.Now.ToString("yyyy-MM-dd_HH-mm-ss"), message);
        }

        protected void TakeScreenshot(string fileName, string message)
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
            bmpScreenshot.Save(fileName, ImageFormat.Png);
            report.ReportImage(message, fileName);
            File.Delete(fileName);
        }
    }
}
