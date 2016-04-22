package resources;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Singular managed threadpool for the application. allows for greater control over threadpool resources.
 * Created by chris on 4/11/16.
 */
public class ApplicationThreadPool {
    private static ApplicationThreadPool ourInstance = new ApplicationThreadPool();
    private ThreadPoolExecutor pool;

    public static ApplicationThreadPool getInstance() {
        return ourInstance;
    }

    private ApplicationThreadPool() {
        int numThreads = Runtime.getRuntime().availableProcessors();
        pool = new ThreadPoolExecutor(numThreads, numThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    public ThreadPoolExecutor getPool(){
        return pool;
    }
}
