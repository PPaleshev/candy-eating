package contracts;

/**
 * Стратегия планирования поедания конфет <b>одного</b> вкуса.
 */
public interface FlavourScheduler {

    /**
     * Добавляет конфету определённого вкуса в очередь поедания.
     * @param candy конфета.
     */
    void enqueue(Candy candy);
}
