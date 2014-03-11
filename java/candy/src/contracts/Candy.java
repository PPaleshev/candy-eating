package contracts;

/**
 * Конфета со вкусом.
 */
public interface Candy {
    /**
     * @return Возвращает вкус конфеты.
     */
    Flavour getFlavour();

    void eatMe();

    long getSequenceNo();
}
