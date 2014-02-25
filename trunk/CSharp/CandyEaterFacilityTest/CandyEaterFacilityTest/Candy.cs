namespace CandyEaterFacilityTest
{
    public class CandyTest : ICandy 
    {
        private readonly IFlavour flavour;

        private readonly long flavourSequenceNumber;

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
    }
}
