package contracts;

/**
 * Вкус.
 */
public interface Flavour extends Comparable<Flavour> {
    /**
     * Уникальный идентификатор вкуса.
     * Необходим для поддержки сценариев, основанных на disruptor.
     * @return возвращает идентификатор вкуса.
     */
    int getId();
}
