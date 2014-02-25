package impl;

import contracts.Flavour;

/**
 * Именованный вкус.
 */
public class NamedFlavour implements Flavour {
    /**
     * Название вкуса.
     */
    final String type;

    public NamedFlavour(String type) {
        this.type = type;
    }

    @Override
    public int compareTo(Flavour o) {
        if(o == null || o.getClass() != getClass())
            throw new IllegalArgumentException();
        if(this==o)
            return 0;
        return type.compareTo(((NamedFlavour)o).type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        NamedFlavour flavour = (NamedFlavour) o;
        return type.equals(flavour.type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String toString() {
        return type;
    }
}
