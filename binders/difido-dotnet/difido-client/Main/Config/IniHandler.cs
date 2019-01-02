using IniParser;
using IniParser.Model;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace difido_client.Main.Config
{
    public class IniHandler
    {
        private IniData parsedData;

        public IniHandler(string fileName)
        {
            if (!File.Exists(fileName))
            {
                throw new IOException("File " + fileName + " is not exist");
            }
            FileIniDataParser parser = new FileIniDataParser();
            parsedData = parser.ReadFile(fileName);
        }

        public string GetProperty(string section, string key)
        {
            KeyDataCollection keyDataCollection = parsedData[section];
            return null == keyDataCollection ? null : keyDataCollection[key];
        }

        public KeyDataCollection GetSectionProperties(string section)
        {
            KeyDataCollection keyDataCollection = parsedData[section];
            return keyDataCollection;
        }
    }
}
