using NUnit.Engine;
using NUnit.Engine.Extensibility;
using System.Xml;
using System;
using System.IO;
using difido_client;

namespace AddIn
{
    [Extension(Description = "Difido Addin")]
    public class NUnitDifidoAddin : ITestEventListener
    {

        IReportDispatcher dispatcher = ReportManager.Instance;

        public void OnTestEvent(string xmlEventString)
        {
            System.IO.File.WriteAllText(Environment.GetFolderPath(Environment.SpecialFolder.UserProfile) + @"/Desktop/newsuccess.txt", "SUCCESS");
            {
                    var xmlDoc = new XmlDocument();
                    xmlDoc.LoadXml(xmlEventString);
                    if (xmlDoc.SelectSingleNode("/start-run") != null)
                    {
                        StartRun(xmlDoc);
                    }
                    else if (xmlDoc.SelectSingleNode("/test-run") != null)
                    {
                        EndRun(xmlDoc);
                    }
                    else if (xmlDoc.SelectSingleNode("/start-suite") != null)
                    {
                        StartSuite(xmlDoc);
                    }
                    else if (xmlDoc.SelectSingleNode("/test-suite") != null)
                    {
                        EndSuite(xmlDoc);
                    }
                    else if (xmlDoc.SelectSingleNode("/start-test") != null)
                    {
                        StartTest(xmlDoc);
                    }
                    else if (xmlDoc.SelectSingleNode("/test-case") != null)
                    {
                        EndTest(xmlDoc);
                        if (xmlDoc.SelectSingleNode("//output") != null)
                        {
                            TestOutput(xmlDoc);
                        }

                }
            }
        }

        private void EndTest(XmlDocument xmlDoc)
        {
            append(xmlDoc);
            EndTestInfo info = new EndTestInfo();
            info.Id = xmlDoc.SelectSingleNode("/*/@id").Value;
            info.FullName = xmlDoc.SelectSingleNode("/*/@fullname").Value;
            info.MethodName = xmlDoc.SelectSingleNode("/*/@methodname").Value;
            info.ClassName = xmlDoc.SelectSingleNode("/*/@classname").Value;
            info.RunState = xmlDoc.SelectSingleNode("/*/@runstate").Value;
            info.Seed = Convert.ToInt64(xmlDoc.SelectSingleNode("/*/@seed").Value);
            info.Result = ParseResult(xmlDoc.SelectSingleNode("/*/@result").Value);
            info.StartTime = xmlDoc.SelectSingleNode("/*/@start-time").Value;
            info.EndTime = xmlDoc.SelectSingleNode("/*/@end-time").Value;
            info.Duration = Convert.ToDouble(xmlDoc.SelectSingleNode("/*/@duration").Value);
            info.Asserts = Convert.ToInt32(xmlDoc.SelectSingleNode("/*/@asserts").Value);
            info.ParentId = xmlDoc.SelectSingleNode("/*/@parentId").Value;
            dispatcher.EndTest(info);

        }

        private TestStatus ParseResult(string value)
        {
            if ("Passed".Equals(value))
            {
                return TestStatus.success;
            }
            else if ("Failed".Equals(value))
            {
                return TestStatus.failure;
            }
            else if ("Inconclusive".Equals(value))
            {
                return TestStatus.warning;
            }
            else
            {
                return TestStatus.success;
            }
        }

        private void StartTest(XmlDocument xmlDoc)
        {
            append(xmlDoc);
            StartTestInfo info = new StartTestInfo();
            info.Id = xmlDoc.SelectSingleNode("/*/@id").Value;
            info.ParentId = xmlDoc.SelectSingleNode("/*/@parentId").Value;
            info.Name = xmlDoc.SelectSingleNode("/*/@name").Value;
            info.FullName = xmlDoc.SelectSingleNode("/*/@fullname").Value;
            info.Type = xmlDoc.SelectSingleNode("/*/@type").Value;
            dispatcher.StartTest(info);
        }

        private void EndSuite(XmlDocument xmlDoc)
        {
            append(xmlDoc);
            if (null == xmlDoc.SelectSingleNode("/*/@parentId"))
            {
                // From some reason NUnit is sending all the data twice, in two different 
                // formats. In in a single xml element for each event and the other one is 
                // a larger XML element that holds all the data.
                // Since we already collected the data in the first time, we don't need it
                // again and we can just ignore it from now on.
                return;
            }
            EndSuiteInfo info = new EndSuiteInfo();
            info.Type = xmlDoc.SelectSingleNode("/*/@type").Value;
            info.Id = xmlDoc.SelectSingleNode("/*/@id").Value;
            info.Name = xmlDoc.SelectSingleNode("/*/@name").Value;
            info.FullName= xmlDoc.SelectSingleNode("/*/@fullname").Value;
            XmlNode node = xmlDoc.SelectSingleNode("/*/@classname");
            if (node != null)
            {
                info.ClassName = node.Value;
            }
            
            info.RunState = xmlDoc.SelectSingleNode("/*/@runstate").Value;
            info.TestCaseCount = Convert.ToInt32(xmlDoc.SelectSingleNode("/*/@testcasecount").Value);
            info.Result = ParseResult(xmlDoc.SelectSingleNode("/*/@result").Value);
            info.StartTime = xmlDoc.SelectSingleNode("/*/@start-time").Value;
            info.EndTime = xmlDoc.SelectSingleNode("/*/@end-time").Value;
            info.Duration = Convert.ToDouble(xmlDoc.SelectSingleNode("/*/@duration").Value);
            info.Total = Convert.ToInt32(xmlDoc.SelectSingleNode("/*/@total").Value);
            info.Passed= Convert.ToInt32(xmlDoc.SelectSingleNode("/*/@passed").Value);
            info.Failed = Convert.ToInt32(xmlDoc.SelectSingleNode("/*/@failed").Value);
            info.Warnings = Convert.ToInt32(xmlDoc.SelectSingleNode("/*/@warnings").Value);
            info.Inconclusive = Convert.ToInt32(xmlDoc.SelectSingleNode("/*/@inconclusive").Value);
            info.Skipeed = Convert.ToInt32(xmlDoc.SelectSingleNode("/*/@skipped").Value);
            info.Asserts= Convert.ToInt32(xmlDoc.SelectSingleNode("/*/@asserts").Value);
            info.ParentId = xmlDoc.SelectSingleNode("/*/@parentId").Value;
            dispatcher.EndSuite(info);
        }

        private void StartSuite(XmlDocument xmlDoc)
        {
            append(xmlDoc);
            StartSuiteInfo info = new StartSuiteInfo(); 
            info.Id = xmlDoc.SelectSingleNode("/*/@id").Value;
            info.ParentId = xmlDoc.SelectSingleNode("/*/@parentId").Value;
            info.Name = xmlDoc.SelectSingleNode("/*/@name").Value;
            info.FullName = xmlDoc.SelectSingleNode("/*/@fullname").Value;
            info.Type = xmlDoc.SelectSingleNode("/*/@type").Value;
            dispatcher.StartSuite(info);
            
        }

        private void EndRun(XmlDocument xmlDoc)
        {
            append(xmlDoc);
            dispatcher.EndRun();
        }

        private void StartRun(XmlDocument xmlDoc)
        {            
            append(xmlDoc);
            
        }

        private void TestOutput(XmlDocument xmlDoc)
        {
            append(xmlDoc);
            using (StringReader sr = new StringReader(xmlDoc.InnerText))
            {
                string line;
                while ((line = sr.ReadLine()) != null)
                {
                    dispatcher.Report(line);
                }
            }
        }

        private void append(XmlDocument xmlDoc)
        {
            string text = null;
            using (var stringWriter = new StringWriter())
            using (var xmlTextWriter = XmlWriter.Create(stringWriter))
            {
                xmlDoc.WriteTo(xmlTextWriter);
                xmlTextWriter.Flush();
                text =  stringWriter.GetStringBuilder().ToString();
            }
            File.AppendAllText(Environment.GetFolderPath(Environment.SpecialFolder.UserProfile) + @"/Desktop/addin.xml", text + Environment.NewLine);
        }
    }
}
