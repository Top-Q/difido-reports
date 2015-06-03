using difido_client.Report.Html.Model;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using System.Web.Script.Serialization;

namespace difido_client.Main.Report.Reporters.HtmlTestReporter
{
    internal class DifidoClient
    {
        private readonly string BASE_URI_TEMPLATE = "http://{0}:{1}/api/";
        private readonly string baseUri;


        public DifidoClient(String host, int port)
        {
            baseUri = String.Format(BASE_URI_TEMPLATE, host, port);
        }

        public int AddExecution()
        {
            WebRequest request = WebRequest.Create(baseUri + "executions/");
            request.Method = "POST";
            request.ContentType = "text/plain";
            return Int32.Parse(Send(request));
        }


        public int GetLastExecutionId()
        {
            WebRequest request = WebRequest.Create(baseUri + "executions/lastId");
            request.Method = "GET";
            request.ContentType = "text/plain";
            return Int32.Parse(Send(request));
        }

        public int AddMachine(int executionId, Machine machine)
        {
            WebRequest request = WebRequest.Create(baseUri + "executions/" + executionId + "/machines/");
            request.Method = "POST";
            return Int32.Parse(SendContent(request, machine));
        }

        private string SendContent(WebRequest request, Object obj)
        {

            request.ContentType = "application/json";
            string data = new JavaScriptSerializer().Serialize(obj);       
            byte[] byteArray = Encoding.UTF8.GetBytes(data);
            request.ContentLength = byteArray.Length;
            using (Stream dataStream = request.GetRequestStream())
            {                
                dataStream.Write(byteArray, 0, byteArray.Length);

            }
            
            return Send(request);

        }

        public void UpdateMachine(int executionId, int machineId, Machine machine)
        {
            WebRequest request = WebRequest.Create(baseUri + "executions/" + executionId + "/machines/" + machineId);
            request.Method = "PUT";
            SendContent(request, machine);
        }

        public void AddTestDetails(int executionId, TestDetails testDetails)
        {
            WebRequest request = WebRequest.Create(baseUri + "executions/" + executionId + "/details");
            request.Method = "POST";
            SendContent(request, testDetails);
        }

        public void AddFile(int executionId, string uid, string file)
        {

            using (WebClient client = new WebClient())
            {
                client.UploadFile(baseUri + "executions/" + executionId + "/details/" + uid + "/file/", file);
            }
        }

        private string Send(WebRequest request)
        {
            HttpWebResponse response = null;
            try
            {

                response = (HttpWebResponse)request.GetResponse();
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
            }

            if (response.StatusCode != HttpStatusCode.OK && response.StatusCode != HttpStatusCode.Accepted && response.StatusCode != HttpStatusCode.NoContent)
            {
                throw new Exception("Request was not successful. Response status is: " + response.StatusCode);
            }
            Stream dataStream = response.GetResponseStream();
            // Open the stream using a StreamReader for easy access.
            StreamReader reader = new StreamReader(dataStream);
            // Read the content.
            return reader.ReadToEnd();
        }


    }
}
