package si.uni_lj.fri.taskyapp.sensor;

import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by urgas9 on 22. 01. 2016.
 */
public class SensorThreadsManager {

    private static final String TAG = "SensorThreadsManager";
    private static final int DEFAULT_NUM_OF_THREADS = 10;
    private ExecutorService mThreadPool;
    private CompletionService mCompletionService;
    private int numOfSubmittedThreads;

    public SensorThreadsManager() {
        this(DEFAULT_NUM_OF_THREADS);
    }

    public SensorThreadsManager(int nThreads) {
        super();
        this.numOfSubmittedThreads = 0;
        this.mThreadPool = Executors.newFixedThreadPool(nThreads);
        this.mCompletionService = new ExecutorCompletionService(mThreadPool);
    }

    public Future submit(Callable callable) {
        numOfSubmittedThreads++;
        return mCompletionService.submit(callable);
    }

    public Future take() {
        try {
            Future f = mCompletionService.take();
            numOfSubmittedThreads--;
            return f;
        } catch (InterruptedException e) {
            //e.printStackTrace();
            Log.e(TAG, "Could not take Future object from completion service: " + e.getMessage());
        }
        return null;
    }

    public void dispose() {
        mThreadPool.shutdownNow();
        mThreadPool = null;
        mCompletionService = null;
    }

    public boolean moreResultsAvailable() {
        return numOfSubmittedThreads > 0;
    }

}
