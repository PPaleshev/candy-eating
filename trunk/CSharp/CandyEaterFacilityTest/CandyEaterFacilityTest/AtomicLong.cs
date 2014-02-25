using System.Runtime.CompilerServices;
using System.Threading;

namespace CandyEaterFacilityTest
{
    public sealed class AtomicLong
    {
        private long value;

        public AtomicLong(long value)
        {
            this.value = value;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public long IncrementAndGet()
        {
            return Interlocked.Increment(ref value);
        }

        public void Exchange(long newValue)
        {
            Interlocked.Exchange(ref value, newValue);
        }

        public long Get()
        {
            return Interlocked.Read(ref value);
        }

        public long DecrementAndGet()
        {
            return Interlocked.Decrement(ref value);
        }
    }
}