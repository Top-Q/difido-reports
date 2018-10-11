using Difido.Model;
using Difido.Model.Test;
using System;
using System.Web.Script.Serialization;

namespace difidoClient
{


    public class DifidoReporter : IReport
    {
        public void AddTestProperty(string propertyName, string propertyValue)
        {
            
        }

        public void EndLevel()
        {
            Report(null, null, TestStatus.success, ReportElementType.stopLevel);
        }

        public void Report(string title)
        {
            Report(title, null, TestStatus.success, ReportElementType.regular);
        }

        public void Report(string title, string message)
        {
            Report(title, message, TestStatus.success, ReportElementType.regular);
        }

        public void Report(string title, string message, bool status)
        {
            Report(title, message, status ? TestStatus.success : TestStatus.success, ReportElementType.regular);
        }

        public void Report(string title, string message, TestStatus status)
        {
            Report(title, message, status, ReportElementType.regular);
        }

        public void Report(string title, string message, TestStatus status, ReportElementType type)
        {
            ReportElement element = new ReportElement();
            element.time = DateTime.Now.ToString("HH:mm:ss");
            element.title = title;
            element.message = message;
            element.status = status.ToString();
            element.type = type.ToString();           
            
            Console.WriteLine(new JavaScriptSerializer().Serialize(element));
            //Console.WriteLine("title:" + title + ",message:"+message+",status:"+status+",type:"+type);
        }

        public void ReportFile(string title, string filePath)
        {
            Report(title, filePath, TestStatus.success, ReportElementType.lnk);
        }

        public void ReportImage(string title, string filePath)
        {
            Report(title, filePath, TestStatus.success, ReportElementType.img);
        }

        public void StartLevel(string description)
        {
            Report(description, null, TestStatus.success, ReportElementType.startLevel);
        }

        public void Step(string title)
        {
            Report(title, null, TestStatus.success, ReportElementType.step);
        }
    }
}
