
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

        public Scenario GetScenarioChildWithId(string id)
        {
            if (null == children)
            {
                return null;
            }
            foreach (Node node in children)
            {
                if (!(node is Scenario))
                {
                    continue;
                }
                Scenario scenario = (Scenario)node;
                if (scenario.scenarioProperties.ContainsKey("Id"))
                {
                    if (scenario.scenarioProperties["Id"].Equals(id))
                    {
                        return scenario;
                    }
                }

            }
            return null;
        }
    }
}
