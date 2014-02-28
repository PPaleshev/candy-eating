namespace CandyEaterFacilityTest.LockFreeProducerConsumerCandyEatingFacility
{
    public class EatingRequest
    {
        public ICandy Candy { get; private set; }
        public ICallback Callback { get; private set; }

        public EatingRequest(ICandy candy, ICallback callback)
        {
            Candy = candy;
            Callback = callback;
        }
    }
}