package impl;

import contracts.Flavour;

/**
 * Уникальный вкус.
 */
public class IdentityFlavour implements Flavour {
    /**
     * Идентификатор вкус.
     */
    private final int number;

    public IdentityFlavour(int number) {
        this.number = number;
    }

    @Override
    public int getId() {
        return number;
    }

    @Override
    public int compareTo(Flavour o) {
        if(o == null || o.getClass() != getClass())
            throw new IllegalArgumentException();
        if(this==o)
            return 0;
        return Integer.compare(number, ((IdentityFlavour)o).number);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        IdentityFlavour flavour = (IdentityFlavour) o;
        return number ==  flavour.number;
    }

    @Override
    public int hashCode() {
        return number;
    }

    @Override
    public String toString() {
        return String.valueOf(number);
    }
}
