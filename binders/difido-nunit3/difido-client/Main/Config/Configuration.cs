using System;
using System.IO;

namespace difido_client.Main.Config
{
    public class Configuration
    {
        private static volatile Configuration instance;
        private static object syncRoot = new Object();
        private const string configurationFileName = "configuration.ini";
        private const string configurationIniSection = "general";

        private IniHandler iniHandler;

        private Configuration()
        {
            string configurationFile = Path.Combine(GetRootFolder(), configurationFileName);
            iniHandler = new IniHandler(configurationFile);
        }

        public bool IsPropertyExists(string section, string option)
        {
            string value = null;
            try
            {
                value = GetProperty(section, option);
            }
            catch
            {
                return false;
            }
            if (String.IsNullOrEmpty(value))
            {
                return false;
            }
            return true;
        }

        public string GetProperty(FrameworkOptions option)
        {
            return iniHandler.GetProperty(configurationIniSection, option.ToString());
        }

        public string GetProperty(string section, FrameworkOptions option)
        {
            return iniHandler.GetProperty(section, option.ToString());
        }

        public string GetProperty(string section, string option)
        {
            return iniHandler.GetProperty(section, option);
        }


        public string GetRootFolder()
        {
            return Directory.GetParent(System.IO.Directory.GetCurrentDirectory()).Parent.Parent.FullName;
        }

        public static Configuration Instance
        {
            get
            {
                if (instance == null)
                {
                    lock (syncRoot)
                    {
                        if (instance == null)
                            instance = new Configuration();
                    }
                }

                return instance;
            }
        }
    }
}
