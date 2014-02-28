using System;
using System.Threading;
using Disruptor;

namespace CandyEaterFacilityTest.DisruptorCandyEatingFacility
{
    public class CandyHandler : IEventHandler<RingBufferValueEntry>
    {
        private readonly int ordinal;
        private readonly int eatersCount;
        private readonly ICandyEater eater;
        private readonly Action callback;

        public CandyHandler(int ordinal, int eatersCount, ICandyEater eater, Action callback)
        {
            this.ordinal = ordinal;
            this.eatersCount = eatersCount;
            this.eater = eater;
            this.callback = callback;
        }

        public void OnNext(RingBufferValueEntry cell, long sequence, bool endOfBatch)
        {
            if (cell.HeatingCounter != null)
            {
                cell.HeatingCounter.DecrementAndGet();
                Thread.Yield();
                return;
            }
            var candy = Interlocked.CompareExchange(ref cell.Candy, null, null);
            if (candy == null)
                return;
            var concurrency = candy.GetFlavour().GetConcurrencyLevel();
            var hashCode = Math.Abs(candy.GetFlavour().GetHashCode());
            var ourCandy = false;
            for (int i = 0; i < concurrency; i++)
            {
                if ((hashCode + i)%eatersCount == ordinal)
                {
                    ourCandy = true;
                    break;
                }
            }
            if (!ourCandy)
                return;
            var existingCandy = Interlocked.CompareExchange(ref cell.Candy, null, candy);
            if (existingCandy != null)
            {
                eater.Eat(existingCandy);
                callback();
            }
        }
    }
}
