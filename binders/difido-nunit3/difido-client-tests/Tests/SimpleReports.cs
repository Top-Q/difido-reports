using NUnit.Framework;
using System;

namespace difido_client_tests.Tests
{
    [TestFixture]
    public class SimpleReports
    {
        [Test]
        public void TestOne()
        {
            Console.WriteLine("In test one");
            Console.WriteLine("Another line in test one");
            
        }

        [Test]
        public void TestTwo()
        {
            Console.WriteLine("In test two");
        }


    }
}
