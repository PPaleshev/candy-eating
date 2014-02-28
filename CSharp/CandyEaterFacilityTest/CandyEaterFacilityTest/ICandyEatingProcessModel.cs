namespace CandyEaterFacilityTest
{
    public interface ICandyEatingProcessModel
    {
        void PutNextCandy(ICandy candy);
        void ShutdownSync();
        AtomicLong PendingCandies { get; }
    }
}