using System;

namespace CandyEaterFacilityTest
{
    public class NamedFlavour : IFlavour, IEquatable<NamedFlavour>
    {
        private readonly string name;
        private readonly int concurrency;
        private readonly int code;

        public NamedFlavour(string name, int concurrency)
        {
            this.name = name;
            this.concurrency = concurrency;
            code = name.GetHashCode();
        }

        public bool Equals(NamedFlavour other)
        {
            return other != null && string.Equals(name, other.name);
        }

        public override bool Equals(object obj)
        {
            return Equals(obj as NamedFlavour);
        }

        public override int GetHashCode()
        {
            return code;
        }

        public int GetConcurrencyLevel()
        {
            return concurrency;
        }

        public override string ToString()
        {
            return name;
        }
    }
}
