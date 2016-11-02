using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using System.Web.Script.Serialization;

namespace difido_client.Report.Html.Model
{
    public class TestDetails
    {
        public string uid { get; set; }
        public List<ReportElement> reportElements;


        public TestDetails()
        {
        }

        public TestDetails(string uid)
        {
            this.uid = uid;
        }

        
        public void AddReportElement(ReportElement reportElement)
        {
            if (null == reportElements)
            {
                reportElements = new List<ReportElement>();
            }
            reportElements.Add(reportElement);

        }

    }
}
