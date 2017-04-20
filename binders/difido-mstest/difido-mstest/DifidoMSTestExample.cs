using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Threading;

namespace difido_mstest
{
    [TestClass]
    public class DifidoMSTestExample : DifidoMSTest
    {
        [TestMethod]
        public void TestPass()
        {
            RunTest(delegate ()
            {
                Report("Pass........");

                ReportStartLevel("Level 1");
                Report("Inside level 1");
                ReportEndLevel();

                Thread.Sleep(1000);

                ReportStartLevel("Level 2");

                Report("Inside level 2");

                ReportStartLevel("Level 3");
                Report("Inside level 3");
                ReportEndLevel();

                ReportEndLevel();

                Thread.Sleep(200);
                Report("Outside levels");
            });
        }

        [TestMethod]
        public void TestWarning()
        {
            RunTest(delegate ()
            {
                Report("Not a warning");
                ReportWarning("Warning.........");
                Report("Not a warning");
            });
        }

        [TestMethod]
        public void TestFail()
        {
            RunTest(delegate ()
            {
                ReportStartLevel("Level 1");
                ReportWarning("Warning.........");
                ReportEndLevel();

                ReportStartLevel("Level 2");
                ReportFail("Failure.........");
                ReportEndLevel();

                ReportStartLevel("Level 3");
                Report("Continue after the fail...");
                ReportEndLevel();
            });
        }

        [TestMethod]
        public void TestException()
        {
            RunTest(delegate ()
            {
                Report("Before the exception...");
                Report("Before the exception...");
                throw new Exception("Exception..........!");
            });
        }

        [TestMethod]
        public void TestAssertFail()
        {
            RunTest(delegate ()
            {
                Report("Before the exception...");
                Report("Before the exception...");
                Assert.AreEqual(3, 4);
            });
        }
    }
}
