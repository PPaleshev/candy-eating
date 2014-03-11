package impl.producerconsumer;

import contracts.*;
import impl.CandyEatingTask;
import impl.EatingRequest;
import impl.producerconsumer.CandyEatingCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Модель.
 */
public class EatingProcessModel implements CandyEatingFacilityStrategy {
    /**
     * Флаг, равный true, если инициирована остановка выборки конфет из входящей очереди.
     */
    private volatile boolean shutdownFlag;

    /**
     * Очередь запросов на поедание конфет.
     */
    private final BlockingQueue<EatingRequest> requestQueue;

    /**
     * Поток, обслуживающий выборку конфет из входящей очереди.
     */
    private final Thread inputDispatcherThread;

    /**
     * Поток, выполняющий выборку готовых запросов на поедание.
     */
    private final Thread requestDispatcherThread;

    /**
     * Количество выбранных из входящей очереди конфет, ожидающих поедания.
     */
    public static final AtomicInteger pendingRequests = new AtomicInteger();

    /**
     * Механизм выполнения задач.
     */
    private final ExecutorService taskExecutor;

    public EatingProcessModel(BlockingQueue<Candy> inputQueue, Set<CandyEater> eaters, SchedulerFactory factory) {
        inputDispatcherThread = new Thread(new InputQueueDispatcher(inputQueue, factory));
        requestDispatcherThread = new Thread(new RequestQueueDispatcher(eaters));
        requestQueue = new LinkedBlockingQueue<>();
        taskExecutor = new ThreadPoolExecutor(7, 7, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    @Override
    public int getPendingCandies() {
        return pendingRequests.get();
    }

    /**
     * Начинает выполнение процесса поедания конфет из входящей очереди.
     * Метод не реентерабельный, должен вызываться однократно.
     */
    public synchronized void start() {
        inputDispatcherThread.start();
        requestDispatcherThread.start();
    }

    /**
     * Устанавливает флаг прекращения выемки новых конфет из входящей очереди и завершения обработки всех извлечённых конфет.
     * Синхронно ожидает завершения обработки.
     */
    public synchronized void shutdownAndWait() {
        shutdownFlag = true;
        try {
            inputDispatcherThread.join();
            requestDispatcherThread.join();
            taskExecutor.shutdown();
            assert taskExecutor.awaitTermination(0, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Процесс выборки конфет из входящей очереди.
     */
    class InputQueueDispatcher implements Runnable {
        /**
         * Входящая очередь конфет.
         */
        private final BlockingQueue<Candy> inputQueue;

        /**
         * Отображение из вкуса в стратегию планирования поедания конфет этого вкуса.
         */
        private final Map<Flavour, FlavourScheduler> schedulerMap;

        /**
         * Фабрика по производству планировщиков поедания конфет каждого вкуса.
         */
        private final SchedulerFactory factory;

        InputQueueDispatcher(BlockingQueue<Candy> inputQueue, SchedulerFactory factory) {
            this.inputQueue = inputQueue;
            this.schedulerMap = new HashMap<>();
            this.factory = factory;
        }

        @Override
        public void run() {
            try {
                while (!shutdownFlag) {
                    Candy input = inputQueue.poll(100, TimeUnit.MILLISECONDS);
                    if(input != null)
                        enqueue(input);
                }
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Добавляет конфету в очередь поедания конфет, соответствующую её вкусу.
         * @param candy конфета.
         * @throws InterruptedException
         */
        void enqueue(Candy candy) throws InterruptedException {
            Flavour flavour = candy.getFlavour();
            FlavourScheduler scheduler = schedulerMap.get(flavour);
            if (scheduler == null) {
                scheduler = factory.create(flavour, requestQueue);
                schedulerMap.put(flavour, scheduler);
            }
            scheduler.enqueue(candy);
            pendingRequests.incrementAndGet();
        }
    }

    /**
     * Процесс передачи готовых запросов на поедание в обработку.
     */
    class RequestQueueDispatcher implements Runnable, CandyEatingCallback {
        /**
         * Очередь поедателей, готовых к поеданию конфет.
         */
        private final BlockingQueue<CandyEater> readyEaters;

        public RequestQueueDispatcher(Set<CandyEater> eaters) {
            readyEaters = new LinkedBlockingQueue<>(eaters);
        }

        @Override
        public void run() {
            try{
                System.out.println("[RequestDispatcher] Started");

                while (!shutdownFlag)
                    process();

                System.out.println("[RequestDispatcher] Shutdown initiated");

                while (pendingRequests.get() > 0)
                    process();

                System.out.println("[RequestDispatcher] Finished");
            } catch (InterruptedException e) {
                System.out.println("[RequestDispatcher]");
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Ожидает появления готового поедателя и отдаёт ему запрос на поедание.
         */
        private void process() throws InterruptedException {
            CandyEater readyEater = readyEaters.poll(100, TimeUnit.MILLISECONDS);
            if (readyEater == null)
                return;
            EatingRequest request = requestQueue.poll();
            if (request == null)
                readyEaters.put(readyEater);
            else
                taskExecutor.execute(new CandyEatingTask(request, readyEater, this));
        }

        @Override
        public void complete(CandyEatingTask task) throws InterruptedException {
            readyEaters.put(task.getEater());
            task.getRequest().complete();
            pendingRequests.decrementAndGet();
        }
    }
}
