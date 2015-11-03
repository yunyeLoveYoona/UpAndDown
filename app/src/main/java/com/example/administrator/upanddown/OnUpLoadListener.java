package com.example.administrator.upanddown;

/**
 * Created by Administrator on 15-11-2.
 */
public interface OnUpLoadListener {
    public void onSuccess(String result);

    public void onFail(String error);

    /**
     * @param number  上传成功的文件个数
     */
    public void onUpLoadSuccess(int number);
}
