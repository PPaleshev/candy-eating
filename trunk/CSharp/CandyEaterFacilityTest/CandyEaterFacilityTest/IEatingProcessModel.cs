namespace CandyEaterFacilityTest
{
    public interface IEatingProcessModel
    {
        void StartAsync();
        void ShutdownSync();
        AtomicLong PendingCandies { get; set; }
    }
}