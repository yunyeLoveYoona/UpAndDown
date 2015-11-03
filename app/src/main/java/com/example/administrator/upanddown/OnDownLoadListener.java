package com.example.administrator.upanddown;

/**
 * Created by Administrator on 15-11-2.
 */
public interface OnDownLoadListener {
    public void onSuccess();
    public void onFail(String error);
    public void onProgress(int tag, int percent);

}
