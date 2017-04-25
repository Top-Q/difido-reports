using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;


namespace difido_client.Report.Html.Model
{
    public class Machine : NodeWithChildren
    {
        public int plannedTests { get; set; }

        public Machine()
        {
            type = "machine";
        }

        public Machine(string name) : base(name)
        {
            type = "machine";
        }
        
    }
}
