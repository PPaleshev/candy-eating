package impl;

import contracts.Candy;
import contracts.CandyEater;
import contracts.Flavour;

import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Модель.
 */
public class EatingProcessModel {
    /**
     * Флаг, равный true, если инициирована остановка выборки конфет из входящей очереди.
     */
    private volatile boolean shutdownFlag;

    /**
     * Диспетчер очереди входящих конфет.
     */
    private final InputQueueDispatcher inputDispatcher;

    /**
     * Диспетчер очереди запросов на поедание.
     */
    private final RequestQueueDispatcher requestDispatcher;

    /**
     * Очередь запросов на поедание конфет.
     */
    private final BlockingQueue<EatingRequest> requestQueue;

    /**
     * Потоки, в которых работают inputDispatcher и requestDispatcher.
     */
    private volatile Thread thread1, thread2;

    /**
     * Количество выбранных из входящей очереди конфет, ожидающих поедания.
     */
    public static final AtomicInteger pendingRequests = new AtomicInteger();

    /**
     * Механизм выполнения задач.
     */
    private final ExecutorService taskExecutor;

    public EatingProcessModel(BlockingQueue<Candy> inputQueue, Set<CandyEater> eaters) {
        inputDispatcher = new InputQueueDispatcher(inputQueue);
        requestDispatcher = new RequestQueueDispatcher(eaters);
        requestQueue = new LinkedBlockingQueue<EatingRequest>();
        taskExecutor = Executors.newCachedThreadPool();
    }

    /**
     * Начинает выполнение процесса поедания конфет из входящей очереди.
     * Метод не реентерабельный, должен вызываться однократно.
     */
    public void startAsync() {
        Thread t1 = new Thread(inputDispatcher);
        Thread t2 = new Thread(requestDispatcher);
        t1.start();
        t2.start();
        thread1 = t1;
        thread2 = t2;
    }

    /**
     * Устанавливает флаг прекращения выемки новых конфет из входящей очереди.
     */
    public void shutdownSync() {
        shutdownFlag = true;
        try {
            thread1.join();
            thread2.join();
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
         * Отображение из вкуса в модель процесса поедания конфет данного вкуса.
         */
        private final ConcurrentHashMap<Flavour, FlavourProducerConsumerModel> modelMap;

        InputQueueDispatcher(BlockingQueue<Candy> inputQueue) {
            this.inputQueue = inputQueue;
            this.modelMap = new ConcurrentHashMap<Flavour, FlavourProducerConsumerModel>();
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
            FlavourProducerConsumerModel targetModel = modelMap.get(flavour);
            if(targetModel == null) {
                FlavourProducerConsumerModel temp = new FlavourProducerConsumerModel(requestQueue, 2);
                FlavourProducerConsumerModel existing = modelMap.putIfAbsent(flavour, temp);
                targetModel = existing == null ? temp : existing;
            }
            targetModel.enqueue(candy);
            pendingRequests.incrementAndGet();
        }
    }

    /**
     * Процесс передачи готовых запросов на поедание в обработку.
     */
    class RequestQueueDispatcher implements Runnable, ICandyEatingCallback {
        /**
         * Очередь поедателей, готовых к поеданию конфет.
         */
        private final BlockingQueue<CandyEater> readyEaters;

        public RequestQueueDispatcher(Set<CandyEater> eaters) {
            readyEaters = new LinkedBlockingQueue<CandyEater>(eaters);
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
         * Выбирает первого свободного поедателя.
         * @return
         */
        private void process() throws InterruptedException {
            CandyEater readyEater = readyEaters.poll(100, TimeUnit.MILLISECONDS);
            if (readyEater == null)
                return;
            EatingRequest request = requestQueue.poll(10, TimeUnit.MILLISECONDS);
            if (request == null)
                readyEaters.put(readyEater);
            else
                taskExecutor.execute(new CandyEatingTask(request, readyEater, this));
        }

        @Override
        public void complete(CandyEatingTask task) throws InterruptedException {
            pendingRequests.decrementAndGet();
            readyEaters.put(task.getEater());
            task.getRequest().complete();
        }
    }
}
