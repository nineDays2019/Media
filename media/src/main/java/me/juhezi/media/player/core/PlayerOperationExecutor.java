package me.juhezi.media.player.core;

import android.text.TextUtils;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * todo 增加线程池
 *
 * 播放器的一些操作比较耗时，在主线程调用可能会造成卡顿或ANR，因此放入统一的异步线程处理
 */
public class PlayerOperationExecutor {

    private static final String PLAYER_THREAD_NAME = "player";

    private ThreadPoolExecutor mExecutor;

    PlayerOperationExecutor(IMediaPlayer player) {
        if (player == null) {
            throw new IllegalArgumentException("cannot allocate thread for a NULL player");
        }

        final String threadName = PLAYER_THREAD_NAME
                + "_"
                + (player != null ? Integer.toHexString(player.hashCode()) : "");
//        mExecutor = makeExecutor(threadName);
    }

    /**
     * 对外提供工具方法，在播放器的异步线程执行任务
     *
     * @param videoId
     * @param task
     */
    public static void runOnPlayerThread(String videoId, Runnable task) {
        if (!TextUtils.isEmpty(videoId) && task != null) {
//            BusProvider.getInstance().post(new PlayerTask(videoId, task));
        }
    }

    /**
     * 在播放器的异步线程执行任务
     *
     * @param runnable
     */
    public void runOnPlayerThread(Runnable runnable) {
        if (runnable == null || mExecutor == null) {
            return;
        }
        //VLogger.d(this, commonToString(runnable), "pool total: " + mExecutor.getTaskCount() + ", active: " + mExecutor.getActiveCount());
        mExecutor.execute(runnable);
    }

    /**
     * 在播放器的异步线程执行任务
     *
     * @param task
     */
//    public void executeOnPlayerThread(ExtendedAsyncTask task) {
//        if (mExecutor == null
//                || task == null
//                || task.getStatus() == ExtendedAsyncTask.Status.RUNNING) {
//            return;
//        }
//        //VLogger.d(this, commonToString(task), "pool total: " + mExecutor.getTaskCount() + ", active: " + mExecutor.getActiveCount());
//        task.execute(mExecutor);
//    }

    /**
     * @param
     * @return
     */
//    private ThreadPoolExecutor makeExecutor(String tag) {
//        return ConcurrentManager.getInsance().createSingleThreadExecutor(tag);
//    }

    public static class PlayerTask {
        public final String videoId;
        public final Runnable job;

        public PlayerTask(String videoId, Runnable job) {
            this.videoId = videoId;
            this.job = job;
        }
    }

}
