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

        public string description { get; set; }
        
        public long duration { get; set; }

        /**
	    * yyyy/MM/dd
	    */
        public string date { get; set; }

        /**
        * HH:mm:ss
        */
        public string timestamp { get; set; }

        public string className { get; set; }

        public Dictionary<string, string> parameters;

        public Dictionary<string, string> properties;



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

        public void AddProperty(string key, string value)
        {
            if (null == properties)
            {
                properties = new Dictionary<string, string>();
            }
            properties.Add(key, value);
        }


        public void AddParameter(string key, string value)
        {
            if (null == parameters)
            {
                parameters = new Dictionary<string, string>();
            }
            parameters.Add(key, value);
        }



    }
}
