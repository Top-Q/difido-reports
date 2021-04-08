using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace difido_client.Main.Report.Reporters.HtmlTestReporter.Model.Execution
{
    public class ExecutionDetails
    {
        public string description { get; set; }

        public Dictionary<string, string> executionProperties { get; set; }

        public bool shared { get; set; }

        public bool forceNew { get; set; }


        public ExecutionDetails()
        {
        }

        public ExecutionDetails(string description, bool shared = false, bool forceNew = false)
        {
            this.description = description;
            this.shared = shared;
            this.forceNew = forceNew;
        }

  



    }
}
