package ustc.sse.yyx.search.thread;

import ch.qos.logback.classic.pattern.ThrowableHandlingConverter;
import org.elasticsearch.threadpool.ThreadPool;
import ustc.sse.yyx.common.utils.R;

import java.util.concurrent.*;

public class Test {
    public static ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("MAIN METHOD STARTS...");
        CompletableFuture<Integer> completableFuture1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("TASK 1---CURRENT THREAD ID: " + Thread.currentThread().getId());
            return 10 / 4;
        }, executorService);
        CompletableFuture<String> completableFuture2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("TASK 2 --- CURRENT THREAD ID: " + Thread.currentThread().getId());
            return "Hello";
        }, executorService);

        CompletableFuture<String> completableFuture = completableFuture1.thenCombineAsync(completableFuture2, (result1, result2) -> {
            System.out.println("TASK 3: " + result1 + " " + result2);
            return "Xuuuuuan";
        }, executorService);
        System.out.println("MAIN METHOD ENDS..." + completableFuture.get());
    }

    public static void main1(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("MAIN METHOD STARTS...");
        // 继承Thread
//        Thread threadExtends = new ThreadExtends();
//        threadExtends.start();

        // 实现Runnable接口
//        new Thread(new RunnableImplements()).start();

        // Callable + FutureTask
//        FutureTask<Integer> futureTask = new FutureTask<>(new CallableImplements());
//        new Thread(futureTask).start();

        // 给线程池直接提交任务 上述三种都有可能导致资源耗尽
        // 可以【将所有的多线程异步任务交给线程池执行】，使得系统的性能稳定，不会由于资源耗尽而导致系统崩溃
        // 确保当前系统中只有少量全局线程池，每个异步任务直接提交给线程池去执行

//        executorService.submit(new RunnableImplements());
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                5,
                200,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(10000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
        Executors.newCachedThreadPool(); // core=0
        Executors.newFixedThreadPool(20); // core=max
        Executors.newScheduledThreadPool(10); // 可以做定时任务
        Executors.newSingleThreadExecutor(); // core=max=1

        System.out.println("MAIN METHOD ENDS...");

    }

    public static class ThreadExtends extends Thread {
        @Override
        public void run() {
            System.out.println("CURRENT THREAD: " + Thread.currentThread().getId());
            System.out.println("RESULT: i=" + 10 / 2);
        }
    }

    public static class RunnableImplements implements Runnable {
        @Override
        public void run() {
            System.out.println("CURRENT THREAD: " + Thread.currentThread().getId());
            System.out.println("RESULT: i=" + 10 / 2);
        }
    }

    public static class CallableImplements implements Callable<Integer> {
        @Override
        public Integer call() {
            System.out.println("CURRENT THREAD: " + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("RESULT: i=" + i);
            return i;
        }
    }
}
