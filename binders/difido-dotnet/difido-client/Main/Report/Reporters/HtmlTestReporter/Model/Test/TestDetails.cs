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

        private List<ReportElement> levelElementBuffer;

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
            UpdateLevelElementsBuffer(reportElement);
            UpdateLevelElementStatuses(reportElement);

        }

        private void UpdateLevelElementStatuses(ReportElement element)
        {
            if (null == levelElementBuffer)
            {
                // No level has been started
                return;
            }
            if (null == element || null == element.type)
            {
                return;
            }
            if (element.status.Equals(TestStatus.success.ToString()))
            {
                // Nothing to do
            }

            TestStatus elementStatus = (TestStatus)Enum.Parse(typeof(TestStatus), element.status);
            foreach (ReportElement currElement in levelElementBuffer)
            {
                TestStatus currElementStatus = (TestStatus)Enum.Parse(typeof(TestStatus), currElement.status);
                if (elementStatus > currElementStatus)
                {
                    currElement.status = elementStatus.ToString();
                }
            }
        }

        private void UpdateLevelElementsBuffer(ReportElement element)
        {
            if (element == null || element.type == null)
            {
                return;
            }
            if (element.type.Equals(ReportElementType.startLevel.ToString()))
            {
                if (null == levelElementBuffer)
                {
                    levelElementBuffer = new List<ReportElement>();
                }
                levelElementBuffer.Add(element);

            }
            else if (element.type.Equals(ReportElementType.stopLevel.ToString()))
            {
                if (levelElementBuffer == null || levelElementBuffer.Count == 0)
                {
                    // Never should happen
                    return;
                }
                levelElementBuffer.RemoveAt(levelElementBuffer.Count - 1);
            }
        }
    }
}
