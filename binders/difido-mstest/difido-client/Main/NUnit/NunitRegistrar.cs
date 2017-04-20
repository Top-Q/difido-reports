using NUnit.Core.Extensibility;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;


namespace difido_client.Nunit
{
    [NUnitAddin(Name = "EventListnerForReport", Description = "Event listener that listens to the tests and dispatches the events to the report manager")]
    public class NunitRegistrar : IAddin
    {
        private bool registeredListeners;

        public bool Install(IExtensionHost host)
        {
            if (!registeredListeners)
            {
                if (host == null)
                    throw new ArgumentNullException("host");

                IExtensionPoint listeners = host.GetExtensionPoint("EventListeners");
                if (listeners == null)
                    return false;

                listeners.Install(new TestEventListener());
                registeredListeners = true;
                return true;

            }
            return true;
        }
    }
}
