


using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using System.Web.Script.Serialization;


using System.Diagnostics;

using System.IO;

using System.IO.Compression;
using difido_client.Report.Html.Model;
using difido_client.Utils;


namespace difido_client.Report.Html
{
    public class HtmlTestReporter : IReporter
    {        
        private const string htmlArchiveFile = @"Resources\difido-reports-common.jar";
        private const string executionModelFileName = "execution.js";
        private const string testModelFileName = "test.js";
        private const string testHtmlFileName = "test.html";
        private Execution execution;
        private Test currentTest;
        private int index;
        private string executionUid;
        private TestDetails testDetails;
        private string testFolder;
        private Machine machine;
        private string currentReportFolder;
        private string outputFolder;
        

        public HtmlTestReporter()
        {
        }

        private void ExecutionToFile()
        {
            string json = "var execution=" + new JavaScriptSerializer().Serialize(execution) + ";";
            System.IO.StreamWriter file = new System.IO.StreamWriter(currentReportFolder + @"\" + executionModelFileName);
            file.WriteLine(json);
            file.Close();

        }

        private void CreateTestFolderIfNotExists()
        {
            testFolder = currentReportFolder + @"\tests\test_" + currentTest.uid;
            if (!Directory.Exists(testFolder))
            {
                Directory.CreateDirectory(testFolder);
                System.IO.File.Copy(currentReportFolder + @"\" + testHtmlFileName, testFolder + @"\" + testHtmlFileName, true);
            }

        }

        private void TestToFile()
        {
            CreateTestFolderIfNotExists();
            string json = "var test=" + new JavaScriptSerializer().Serialize(testDetails) + ";";
            System.IO.StreamWriter file = new System.IO.StreamWriter(testFolder + @"\" + testModelFileName);
            file.WriteLine(json);
            file.Close();

        }


        public void Init(string outputFolder)
        {
            this.outputFolder = outputFolder;
            this.currentReportFolder = outputFolder + @"\current";
            CreateReportsFolder();
            InitHtmls();
            GenerateUid();
            machine = new Machine(System.Environment.MachineName);
            execution = new Execution();
            execution.AddMachine(machine);


        }

        private void GenerateUid()
        {
            executionUid = new Random().Next(0, 1000) + (DateTime.Now.Ticks / TimeSpan.TicksPerMinute).ToString();
        }

        public void StartTest(ReporterTestInfo testInfo)
        {

            currentTest = new Test(index, testInfo.TestName ,executionUid + "-"+ index);
            currentTest.timestamp = DateTime.Now.ToString("HH:mm:ss");            
            string scenarioName = testInfo.FullyQualifiedTestClassName.Split('.')[testInfo.FullyQualifiedTestClassName.Split('.').Length - 2];
            Scenario scenario;
            if (machine.IsChildWithNameExists(scenarioName))
            {
                scenario = (Scenario)machine.GetChildWithName(scenarioName);
            }
            else
            {
                scenario = new Scenario(scenarioName);
                machine.AddChild(scenario);
            }
            scenario.AddChild(currentTest);
            ExecutionToFile();
            testDetails = new TestDetails(testInfo.TestName, currentTest.uid);
            testDetails.description = testInfo.FullyQualifiedTestClassName;
            testDetails.timestamp = DateTime.Now.ToString();
        }

        public void EndTest(ReporterTestInfo testInfo)
        {

            currentTest.status = testInfo.Status.ToString();
            currentTest.duration = testInfo.DurationTime;
            testDetails.duration = testInfo.DurationTime;
            ExecutionToFile();
            TestToFile();
            index++;
        }

        public void StartSuite(string suiteName)
        {
        }

        public void EndSuite(string suiteName)
        {
        }



        public void Report(string title, string message, ReporterTestInfo.TestStatus status, ReportElementType type)
        {
            ReportElement element = new ReportElement();
            if (null == testDetails)
            {
                Console.WriteLine("HTML reporter was not initiliazed propertly. No reports would be created.");
                return;
            }
            testDetails.AddReportElement(element);
            element.title = title;
            element.message = message;
            element.time = DateTime.Now.ToString("HH:mm:ss");
            element.status = status.ToString();
            element.type = type.ToString();

            if (type == ReportElementType.lnk || type == ReportElementType.img)
            {
                if (File.Exists(message))
                {
                    //This is a link to a file. Let's copy it to the report folder
                    CreateTestFolderIfNotExists();
                    try
                    {
                        string fileName = Path.GetFileName(message);
                        string fileDestination = testFolder + @"\" + fileName;
                        System.IO.File.Copy(message, fileDestination, true);
                        //We need that the link would be to the file in the report folder
                        element.message = fileName;
                    }
                    catch (IOException e)
                    {
                        Console.WriteLine("Failed adding file to the report due to " + e.Message);
                    }


                }
            }

            TestToFile();
        }

        private void CreateReportsFolder()
        {
            try
            {
                System.IO.Directory.CreateDirectory(currentReportFolder);
            }
            catch (Exception e)
            {
                throw new Exception("Failed to create reports output folder", e);
            }
        }

        private string GetRootFolder()
        {
            return Directory.GetParent(System.IO.Directory.GetCurrentDirectory()).Parent.Parent.FullName;
        }

        private void InitHtmls()
        {           
            string templateFolder = outputFolder + @"\htmlTemplate";
            if (!Directory.Exists(templateFolder) || !File.Exists(templateFolder + @"\index.html"))
            {
                try
                {
                    //Deleting old reports
                    if (Directory.Exists(templateFolder))
                    {
                        Directory.Delete(templateFolder, true);
                    }
                    

                    //Re creating the reports output folder
                    Directory.CreateDirectory(templateFolder);

                    //Creating temp folder
                    string tempFolder = FileUtils.CreateTempFolder();
                    try
                    {
                        //Extracting the JAR file with the HTML reports to the temp folder
                        File.WriteAllBytes(tempFolder + @"\difido-reports-common.jar" , difido_client.Properties.Resources.difido_reports_common);
                        ZipFile.ExtractToDirectory(tempFolder + @"\difido-reports-common.jar", tempFolder);

                        //Copying only the HTML files to the reports output folder
                        FileUtils.DirectoryCopy(tempFolder + @"\il.co.topq.difido.view", templateFolder, true);
                    }
                    finally
                    {
                        //Deleting the temporary folder
                        Directory.Delete(tempFolder, true);
                    }

                }
                catch (IOException e)
                {
                    //Failed to create or copy files.
                    string str = e.ToString();
                }
            }

            try
            {
                //Deleting old reports
                Directory.Delete(currentReportFolder, true);

                //Re creating the reports output folder
                Directory.CreateDirectory(currentReportFolder);

                FileUtils.DirectoryCopy(templateFolder, currentReportFolder, true);

            }
            catch (IOException e)
            {
                //Failed to create or copy files.
                string str = e.ToString();
            }

        }

 
    }
}
