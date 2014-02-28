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

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void Exchange(long newValue)
        {
            Interlocked.Exchange(ref value, newValue);
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public long Get()
        {
            return Interlocked.Read(ref value);
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public long DecrementAndGet()
        {
            return Interlocked.Decrement(ref value);
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void Add(long valueToAdd)
        {
            Interlocked.Add(ref value, valueToAdd);
        }
    }
}