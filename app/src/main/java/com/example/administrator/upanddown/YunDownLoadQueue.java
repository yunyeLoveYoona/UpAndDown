package com.example.administrator.upanddown;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Looper;

import com.example.administrator.upanddown.cache.ImageDiskLruCache;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * 下载队列
 * Created by Administrator on 15-11-2.
 */
public class YunDownLoadQueue implements Runnable {
    /**
     * 图片
     **/
    public static final int IMAGE = 10001;
    /**
     * 非图片的其他文件
     **/
    public static final int FILE = 10002;

    private ExecutorService executorService;
    private PriorityBlockingQueue<Runnable> queue;
    private Looper mainLooper;
    private ImageDiskLruCache diskLruCache;


    protected YunDownLoadQueue(Context context) {
        queue = new PriorityBlockingQueue<Runnable>();
        executorService = Executors.newCachedThreadPool();
        executorService.execute(this);
        mainLooper = context.getMainLooper();
        diskLruCache = new ImageDiskLruCache(context, "image",
                10 * 1024 * 1024, Bitmap.CompressFormat.JPEG, 100);
    }

    public void addDownload(String url, File file, int tag, OnDownLoadListener onDownLoadListener) {
        YunHttpUrlConnection yunHttpUrlConnection = new YunHttpUrlConnection(url, mainLooper);
        yunHttpUrlConnection.setDownLoad(file, tag);
        yunHttpUrlConnection.setOnDownLoadListener(onDownLoadListener);
        queue.add(yunHttpUrlConnection);
    }

    public void addImageDownload(String url, int width, int height, int tag, OnDownLoadListener onDownLoadListener) {
        if (diskLruCache.containsKey(url + width + "*" + height)) {
            onDownLoadListener.onSuccess();
        } else {
            YunHttpUrlConnection yunHttpUrlConnection = new YunHttpUrlConnection(url, mainLooper);
            yunHttpUrlConnection.setImageDownLoad(width, height, tag);
            yunHttpUrlConnection.setOnDownLoadListener(onDownLoadListener);
            yunHttpUrlConnection.setDiskLruCache(diskLruCache);
            queue.add(yunHttpUrlConnection);
        }

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
