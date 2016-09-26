/*
 * Copyright (c) 2016, University of Ljubljana, Slovenia
 *
 * Gasper Urh, gu7668@student.uni-lj.si
 *
 * This library was developed as part of the paper submitted for the UbitTention workshop paper (in conjunction with UbiComp'16) and my master thesis. For more information, please visit http://projects.hcilab.org/ubittention/
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

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
