package contracts;

/**
 * Конфета со вкусом.
 */
public interface Candy {
    /**
     * @return Возвращает вкус конфеты.
     */
    Flavour getFlavour();

    long getSequenceNo();
}
