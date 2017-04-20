


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
using difido_client.Main.Report.Reporters.HtmlTestReporter;


namespace difido_client.Report.Html
{
    public class HtmlTestReporter : AbstractDifidoReporter
    {
        private const string htmlArchiveFile = @"Resources\difido-reports-common.jar";
        private const string executionModelFileName = "execution.js";
        private const string testModelFileName = "test.js";
        private const string testHtmlFileName = "test.html";        
        private string testFolder;
        private string currentReportFolder;
        private string outputFolder;
        

        public HtmlTestReporter()
        {
        }

     

        private void CreateTestFolderIfNotExists(string uid)
        {
            testFolder = currentReportFolder + @"\tests\test_" + uid;
            if (!Directory.Exists(testFolder))
            {
                Directory.CreateDirectory(testFolder);
                System.IO.File.Copy(currentReportFolder + @"\" + testHtmlFileName, testFolder + @"\" + testHtmlFileName, true);
            }

        }



        public override void Init(string outputFolder) 
        {
            base.Init(outputFolder);
            this.outputFolder = outputFolder;
            this.currentReportFolder = outputFolder + @"\current";
            CreateReportsFolder();
            InitHtmls();
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



        protected override void TestDetailsWereAdded(TestDetails details)
        {
            try
            {
                CreateTestFolderIfNotExists(details.uid);
                JavaScriptSerializer serializer = new JavaScriptSerializer();
                serializer.MaxJsonLength = int.MaxValue;
                string json = "var test=" + serializer.Serialize(details) + ";";
                System.IO.StreamWriter file = new System.IO.StreamWriter(testFolder + @"\" + testModelFileName);
                try
                {
                    file.WriteLine(json);
                }
                finally
                {
                    file.Close();
                }

            }
            catch (Exception e)
            {
                Console.WriteLine("Failed writing test details due to " + e.Message);
            }
        }

        protected override void ExecutionWasAddedOrUpdated(Execution execution)
        {
            try
            {
                JavaScriptSerializer serailizer = new JavaScriptSerializer();
                string json = "var execution=" + serailizer.Serialize(execution) + ";";
                System.IO.StreamWriter file = new System.IO.StreamWriter(currentReportFolder + @"\" + executionModelFileName);

                try
                {
                    file.WriteLine(json);
                }
                finally
                {
                    file.Close();
                }
            }
            catch (Exception e)
            {

                Console.WriteLine("Failed to write execution due to " + e.Message);
            }
        }

        protected override string FileWasAdded(TestDetails testDetails, string file)
        {
            CreateTestFolderIfNotExists(testDetails.uid);
            try
            {
                string fileName = Path.GetFileName(file);
                string fileDestination = testFolder + @"\" + fileName;
                System.IO.File.Copy(file, fileDestination, true);
                //We need that the link would be to the file in the report folder
                return fileName;
            }
            catch (IOException e)
            {
                Console.WriteLine("Failed adding file to the report due to " + e.Message);
                return null;
            }
        }

        #region unused

        protected override void MachineWasAdded(Machine machine)
        {
        }
        #endregion
    }
}
