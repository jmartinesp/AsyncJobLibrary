package com.arasthel.asyncjob;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

/**
 * Created by Arasthel on 08/07/14.
 */
public class AsyncJob<JobResult> {

    private static Handler uiHandler = new Handler(Looper.getMainLooper());

    // Action to do in background
    private AsyncAction actionInBackground;
    // Action to do when the background action ends
    private AsyncResultAction actionOnMainThread;

    // An optional ExecutorService to enqueue the actions
    private ExecutorService executorService;

    // The thread created for the action
    private Thread asyncThread;
    // The FutureTask created for the action
    private FutureTask asyncFutureTask;

    // The result of the background action
    private JobResult result;

    /**
     * Instantiates a new AsyncJob
     */
    public AsyncJob() {
    }

    /**
     * Executes the provided code immediately on the UI Thread
     * @param onMainThreadJob Interface that wraps the code to execute
     */
    public static void doOnMainThread(final OnMainThreadJob onMainThreadJob) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                onMainThreadJob.doInUIThread();
            }
        });
    }

    /**
     * Executes the provided code immediately on a background thread
     * @param onBackgroundJob Interface that wraps the code to execute
     */
    public static void doInBackground(final OnBackgroundJob onBackgroundJob) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                onBackgroundJob.doOnBackground();
            }
        }).start();
    }

    /**
     * Executes the provided code immediately on a background thread that will be submitted to the
     * provided ExecutorService
     * @param onBackgroundJob Interface that wraps the code to execute
     * @param executor Will queue the provided code
     */
    public static FutureTask doInBackground(final OnBackgroundJob onBackgroundJob, ExecutorService executor) {
        FutureTask task = (FutureTask) executor.submit(new Runnable() {
            @Override
            public void run() {
                onBackgroundJob.doOnBackground();
            }
        });

        return task;
    }

    /**
     * Begins the background execution providing a result, similar to an AsyncTask.
     * It will execute it on a new Thread or using the provided ExecutorService
     */
    public void start() {
        if(actionInBackground != null) {

            Runnable jobToRun = new Runnable() {
                @Override
                public void run() {
                    result = (JobResult) actionInBackground.doAsync();
                    onResult();
                }
            };

            if(getExecutorService() != null) {
               asyncFutureTask = (FutureTask) getExecutorService().submit(jobToRun);
            } else {
                asyncThread = new Thread(jobToRun);
                asyncThread.start();
            }
        }
    }

    /**
     * Cancels the AsyncJob interrupting the inner thread.
     */
    public void cancel() {
        if(actionInBackground != null) {
            if(executorService != null) {
               asyncFutureTask.cancel(true);
            } else {
                asyncThread.interrupt();
            }
        }
    }


    private void onResult() {
        if (actionOnMainThread != null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    actionOnMainThread.onResult(result);
                }
            });
        }
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public AsyncAction getActionInBackground() {
        return actionInBackground;
    }

    /**
     * Specifies which action to run in background
     * @param actionInBackground the action
     */
    public void setActionInBackground(AsyncAction actionInBackground) {
        this.actionInBackground = actionInBackground;
    }

    public AsyncResultAction getActionOnResult() {
        return actionOnMainThread;
    }

    /**
     * Specifies which action to run when the background action is finished
     * @param actionOnMainThread the action
     */
    public void setActionOnResult(AsyncResultAction actionOnMainThread) {
        this.actionOnMainThread = actionOnMainThread;
    }

    public interface AsyncAction<ActionResult> {
        public ActionResult doAsync();
    }

    public interface AsyncResultAction<ActionResult> {
        public void onResult(ActionResult result);
    }

    public interface OnMainThreadJob {
        public void doInUIThread();
    }

    public interface OnBackgroundJob {
        public void doOnBackground();
    }

    /**
     * Builder class to instantiate an AsyncJob in a clean way
     * @param <JobResult> the type of the expected result
     */
    public static class AsyncJobBuilder<JobResult> {

        private AsyncAction<JobResult> asyncAction;
        private AsyncResultAction asyncResultAction;
        private ExecutorService executor;

        public AsyncJobBuilder() {

        }

        /**
         * Specifies which action to run on background
         * @param action the AsyncAction to run
         * @return the builder object
         */
        public AsyncJobBuilder<JobResult> doInBackground(AsyncAction<JobResult> action) {
            asyncAction = action;
            return this;
        }

        /**
         * Specifies which action to run when the background action ends
         * @param action the AsyncAction to run
         * @return the builder object
         */
        public AsyncJobBuilder<JobResult> doWhenFinished(AsyncResultAction action) {
            asyncResultAction = action;
            return this;
        }

        /**
         * Used to provide an ExecutorService to launch the AsyncActions
         * @param executor the ExecutorService which will queue the actions
         * @return the builder object
         */
        public AsyncJobBuilder<JobResult> withExecutor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        /**
         * Instantiates a new AsyncJob of the given type
         * @return a configured AsyncJob instance
         */
        public AsyncJob<JobResult> create() {
            AsyncJob<JobResult> asyncJob = new AsyncJob<JobResult>();
            asyncJob.setActionInBackground(asyncAction);
            asyncJob.setActionOnResult(asyncResultAction);
            asyncJob.setExecutorService(executor);
            return asyncJob;
        }

    }

}
