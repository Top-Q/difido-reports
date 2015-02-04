using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;


namespace difido_client.Report.Html.Model
{
    public class Test : Node
    {
        public int index { get; set; }
        public string uid { get; set; }
        public long duration { get; set; }
        public string timestamp { get; set; }

        public Test()
        {
            type = "test";
            status = "success";
        }

        public Test(int index, string name, string uid) : base(name)
        {
            this.index = index;
            this.uid = uid;
            type = "test";
            status = "success";
            
        }


    }
}
