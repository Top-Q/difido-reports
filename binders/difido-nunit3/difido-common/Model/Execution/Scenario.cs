using System.Collections.Generic;


namespace Difido.Model.Execution
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
