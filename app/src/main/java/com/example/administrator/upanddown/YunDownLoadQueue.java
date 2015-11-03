package com.example.administrator.upanddown;

import android.content.Context;
import android.os.Looper;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * 下载队列
 * Created by Administrator on 15-11-2.
 */
public class YunDownLoadQueue implements Runnable{
    private ExecutorService executorService;
    private PriorityBlockingQueue<Runnable> queue;
    private Looper mainLooper;
    protected YunDownLoadQueue(Context context) {
        queue = new PriorityBlockingQueue<Runnable>();
        executorService = Executors.newCachedThreadPool();
        executorService.execute(this);
        mainLooper = context.getMainLooper();
    }
    public void addDownload(String url,File file,int tag,OnDownLoadListener onDownLoadListener){
        YunHttpUrlConnection yunHttpUrlConnection = new YunHttpUrlConnection(url,mainLooper);
        yunHttpUrlConnection.setDownLoad(file,tag);
        yunHttpUrlConnection.setOnDownLoadListener(onDownLoadListener);
        queue.add(yunHttpUrlConnection);
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                queue.take().run();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public void stop() {
        queue.clear();
        executorService.shutdown();
    }

    public void clear() {
        queue.poll();
    }
}
