using System.Collections.Generic;


namespace difido_client.Report.Html.Model
{
    public class Scenario : NodeWithChildren
    {

        public Dictionary<string, string> scenarioProperties { get; set; }

        public Scenario()
        {
            type = "scenario";
            status = "success";
        }

        public Scenario(string name)
            : base(name)
        {
            type = "scenario";
            status = "success";
        }

        
    }
}
