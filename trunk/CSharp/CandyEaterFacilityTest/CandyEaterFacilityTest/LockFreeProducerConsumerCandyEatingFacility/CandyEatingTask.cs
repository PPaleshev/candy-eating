namespace CandyEaterFacilityTest.LockFreeProducerConsumerCandyEatingFacility
{
    public class CandyEatingTask
    {
        public EatingRequest Request { get; private set; }
        public ICandyEater CandyEater { get; private set; }

        public CandyEatingTask(EatingRequest request, ICandyEater candyEater)
        {
            Request = request;
            CandyEater = candyEater;
        }
    }
}