using difido_client.Main.Report.Reporters.HtmlTestReporter.Model.Execution;
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
        private DifidoClient client;
        private Boolean enabled = true;
        private int executionId;
        private int machineId;
        private int numOfFailures;


        public override void Init(string outputFolder)
        {
            base.Init(outputFolder);

            String host = null;
            int port = 0;
            try
            {
                if (!enabled)
                {
                    return;
                }
                host = "localhost";
                port = 8080;

                ExecutionDetails executionDetails = new ExecutionDetails();
                //executionDetails.description = "Some description";
                client = new DifidoClient(host, port);
                executionId = client.AddExecution(executionDetails);
                machineId = client.AddMachine(executionId, CurrentExecution.GetLastMachine());
                enabled = true;
                
            }
            catch
            {
                enabled = false;
                //log.warning("Failed to init " + RemoteHtmlReporter.class.getName() + "connection with host '" + host + ":"
                //        + port + "' due to " + t.getMessage());
            }

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
            catch
            {
                //log.warning("Failed updating test details to remote server due to " + e.getMessage());
                CheckIfNeedsToDisable();
            }

        }

        protected override void ExecutionWasAddedOrUpdated(difido_client.Report.Html.Model.Execution execution)
        {
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
                //log.warning("Failed updating test details to remote server due to " + e.getMessage());
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
            catch
            {
                //log.warning("Failed uploading file " + file.getName() + " to remote server due to " + e.getMessage());
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
                //log.warning("Communication to server has failed more then " + MAX_NUM_OF_ALLOWED_FAILURES
                //        + ". Disabling report reporter");
                enabled = false;
            }
        }

    }
}
