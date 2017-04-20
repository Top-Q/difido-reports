using difido_client.Main.Config;
using difido_client.Main.Report.Reporters.HtmlTestReporter.Model.Execution;
using difido_client.Report.Html.Model;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace difido_client.Main.Report.Reporters.HtmlTestReporter
{
    public class RemoteHtmlReporter : AbstractDifidoReporter
    {
        private readonly int MAX_NUM_OF_ALLOWED_FAILURES = 10;
        private readonly string CONFIGURATION_SECTION = "reportServer";
        private DifidoClient client;
        private Boolean enabled = true;
        private int executionId;
        private int machineId;
        private int numOfFailures;
        // A flag that allows us to execute the init only once.
        private bool first = true;



        public override void Init(string outputFolder)
        {
            base.Init(outputFolder);
        }

        private void Init()
        {
            if (!first)
            {
                return;
            }
            string host = null;
            string port = null;
            try
            {
                enabled = Boolean.Parse(Configuration.Instance.GetProperty(CONFIGURATION_SECTION, "enabled"));
            }
            catch
            {
                enabled = false;
            }

            try
            {
                if (!enabled)
                {
                    return;
                }
                host = Configuration.Instance.GetProperty(CONFIGURATION_SECTION, "host");
                port = Configuration.Instance.GetProperty(CONFIGURATION_SECTION, "port");
                ExecutionDetails executionDetails = new ExecutionDetails();
                if (Configuration.Instance.IsPropertyExists(CONFIGURATION_SECTION, "executionDescription"))
                {
                    executionDetails.description = Configuration.Instance.GetProperty(CONFIGURATION_SECTION, "executionDescription");
                }
                if (Configuration.Instance.IsPropertyExists(CONFIGURATION_SECTION, "executionProperties"))
                {
                    string executionPropertiesValue = Configuration.Instance.GetProperty(CONFIGURATION_SECTION, "executionProperties");

                    // Parsing the properties from the configuration file and adding them as execution properties
                    executionDetails.executionProperties = executionPropertiesValue.Split(';')
                        .Select(value => value.Split('='))
                        .ToDictionary(pair => pair[0], pair => pair[1]);

                    // We are also adding the execution properties as scenario properties so we could see it in the ElasticSearch
                    Scenario scenario = (Scenario)CurrentExecution.GetLastMachine().children[0];
                    scenario.scenarioProperties = executionDetails.executionProperties;
                }

                client = new DifidoClient(host, Int32.Parse(port));
                executionId = client.AddExecution(executionDetails);
                machineId = client.AddMachine(executionId, CurrentExecution.GetLastMachine());
                enabled = true;

            }
            catch
            {
                enabled = false;
            }
            first = false;



        }


        protected override void TestDetailsWereAdded(difido_client.Report.Html.Model.TestDetails testDetails)
        {
            if (!enabled)
            {
                return;
            }
            try
            {
                client.AddTestDetails(executionId, testDetails);
            }
            catch(Exception e)
            {
                Console.WriteLine(e.Message);
                CheckIfNeedsToDisable();
            }

        }

        protected override void ExecutionWasAddedOrUpdated(difido_client.Report.Html.Model.Execution execution)
        {
            // We have to call to the Init here and not in the Init method, 
            // since this method is called too many times, and each time it is called, a new execution is created. 
            Init();
            if (!enabled)
            {
                return;
            }

            try
            {
                client.UpdateMachine(executionId, machineId, execution.GetLastMachine());
            }
            catch
            {                
                CheckIfNeedsToDisable();
            }
        }

        protected override string FileWasAdded(difido_client.Report.Html.Model.TestDetails testDetails, string file)
        {
            if (!enabled)
            {
                return null;
            }
            if (file == null || file.Length == 0 || !File.Exists(file))
            {
                return null;
            }

            try
            {
                client.AddFile(executionId, testDetails.uid, file);
            }
            catch (Exception e)
            {
                Console.WriteLine("Failed sending file " + file + " due to " + e.Message);
            }
            return Path.GetFileName(file);


        }

        protected override void MachineWasAdded(difido_client.Report.Html.Model.Machine machine)
        {
        }

        private void CheckIfNeedsToDisable()
        {
            numOfFailures++;
            if (numOfFailures > MAX_NUM_OF_ALLOWED_FAILURES)
            {
                Console.WriteLine("Communication to server has failed more then " + MAX_NUM_OF_ALLOWED_FAILURES + ". Disabling report reporter");
                enabled = false;
            }
        }

        public override void EndSuite(string suiteName)
        {
            if (!enabled)
            {
                return;
            }
            client.endExecution(executionId);
        }


    }
}
