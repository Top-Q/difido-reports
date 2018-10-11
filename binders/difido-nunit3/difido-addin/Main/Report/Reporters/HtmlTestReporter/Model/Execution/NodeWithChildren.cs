using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;


namespace difido_client.Report.Html.Model
{
    public abstract class NodeWithChildren : Node
    {
        public List<Node> children { get; set; }

        public NodeWithChildren()
        {
        }

        public NodeWithChildren(string name)
            : base(name)
        {
        }

        public bool IsChildWithNameExists(string name)
        {
            return GetChildWithName(name) != null;
        }


        public Node GetChildWithName(string name)
        {
            if (null == children)
            {
                return null;
            }
            foreach (Node node in children)
            {
                if (node.name.Equals(name))
                {
                    return node;
                }
            }
            return null;
        }

        public void AddChild(Node node)
        {
            if (null == children)
            {
                children = new List<Node>();
            }
            node.parent = this;
            children.Add(node);
        }
    }
}
