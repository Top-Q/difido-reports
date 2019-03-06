using NUnit.Framework;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace difido_client.Main.Config
{
    public class DifidoConfig
    {
        private static volatile DifidoConfig instance;
        private static object syncRoot = new object();
        private const string relativeConfigurationPathPrefix = "../../../";
        private const string configurationFileName = "difido_config.ini";
        private const string configurationIniSection = "general";

        private IniHandler iniHandler;

        private DifidoConfig()
        {
            string configurationFile = Path.Combine(GetRootFolder(), relativeConfigurationPathPrefix, configurationFileName);
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
            if (string.IsNullOrEmpty(value))
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


        public static string GetRootFolder()
        {
            //Directory.GetCurrentDirectory(); -> this method is problematic when working with NUnit3
            // Instead using: TestContext.CurrentContext.TestDirectory; (to get the path to: bin/Debug)
            string rootFolder = TestContext.CurrentContext.TestDirectory;
            return rootFolder;
        }

        public static DifidoConfig Instance
        {
            get
            {
                if (instance == null)
                {
                    lock (syncRoot)
                    {
                        if (instance == null)
                            instance = new DifidoConfig();
                    }
                }

                return instance;
            }
        }
    }
}
