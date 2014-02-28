using System;
using System.Threading;

namespace CandyEaterFacilityTest
{
    public class CandyTest : ICandy 
    {
        private readonly IFlavour flavour;

        private readonly long flavourSequenceNumber;

        private long wasEated;

        public CandyTest(IFlavour flavour, long flavourSequenceNumber) 
        {
            this.flavour = flavour;
            this.flavourSequenceNumber = flavourSequenceNumber;
        }

        public IFlavour GetFlavour() 
        {
            return flavour;
        }

        public long GetFlavourSequenceNumber() 
        {
            return flavourSequenceNumber;
        }

        public void EatMe()
        {
            if (Interlocked.CompareExchange(ref wasEated, 1, 0) != 0)
                Console.Out.WriteLine("Attempt to eat same candy twice!!!!!");
        }
    }
}
