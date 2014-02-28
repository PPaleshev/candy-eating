namespace CandyEaterFacilityTest.DisruptorCandyEatingFacility
{
    public sealed class RingBufferValueEntry
    {
        public ICandy Candy;
        public AtomicLong HeatingCounter;
    }
}
