using System;
using System.Collections.Generic;
using System.Text;

namespace difidoClient
{

    public enum ReportElementType
    {
        regular, step, lnk, img, startLevel, stopLevel
    }

    public enum TestStatus
    {
        success, failure, error, warning
    }



    public interface IReport
{
    void Report(string title);

    void Report(string title, string message);

    void Report(string title, string message, bool status);

    void Report(string title, string message, TestStatus status);

    void Report(string title, string message, TestStatus status, ReportElementType type);

    void ReportFile(string title, string filePath);

    void ReportImage(string title, string filePath);

    void StartLevel(string description);

    void EndLevel();

    void Step(string title);

    void AddTestProperty(string propertyName, string propertyValue);


}
}
