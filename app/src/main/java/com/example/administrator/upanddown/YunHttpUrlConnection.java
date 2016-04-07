package com.example.administrator.upanddown;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import com.example.administrator.upanddown.cache.DiskLruCache;
import com.example.administrator.upanddown.cache.ImageCache;
import com.example.administrator.upanddown.cache.ImageDiskLruCache;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 15-10-30.
 */
public class YunHttpUrlConnection implements Runnable, Comparable {
    private HttpURLConnection httpURLConnection;
    private URL url;
    public Map<String, String> strParams;
    private Map<String, File> fileParams;
    private OnUpLoadListener onUpLoadListener = null;
    private OnDownLoadListener onDownLoadListener;
    private String boundary = "--------httpPost";
    private DataOutputStream dataOutputStream;
    private Handler handler;
    private int compeleteSize;
    private int number = 0;
    private File file;
    private int tag;
    private int width;
    private int height;
    private ImageDiskLruCache diskLruCache;


    public YunHttpUrlConnection(String url, Looper mainLooper) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            fail(e.getMessage());
        }
        strParams = new HashMap<String, String>();
        fileParams = new HashMap<String, File>();
        handler = new Handler(mainLooper);
    }

    public void addStrParams(String name, String value) {
        strParams.put(name, value);
    }

    public void addFileParams(String fileName, File file) {
        fileParams.put(fileName, file);
    }

    public void setOnUpLoadListener(OnUpLoadListener listener) {
        this.onUpLoadListener = listener;
    }

    public void setOnDownLoadListener(OnDownLoadListener onDownLoadListener) {
        this.onDownLoadListener = onDownLoadListener;
    }

    public void post() {

        if (url != null) {
            try {
                initConnection();
                httpURLConnection.connect();
                dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
                writeStringParams();
                writeFileParams();
                dataOutputStream.writeBytes("--" + boundary + "--" + "\r\n");
                dataOutputStream.writeBytes("\r\n");
                InputStream in = httpURLConnection.getInputStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int b;
                while ((b = in.read()) != -1) {
                    out.write(b);
                }
                httpURLConnection.disconnect();
                if (onUpLoadListener != null) {
                    onUpLoadListener.onSuccess(new String(out.toByteArray()));
                }
            } catch (IOException e) {
                e.printStackTrace();
                fail(e.getMessage());
            }
        }
    }

    private void writeFileParams() throws IOException {
        Set<String> keySet = fileParams.keySet();
        for (Iterator<String> it = keySet.iterator(); it.hasNext(); ) {
            String name = it.next();
            File value = fileParams.get(name);
            dataOutputStream.writeBytes("--" + boundary + "\r\n");
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + name
                    + "\"; filename=\"" + encode(value.getName()) + "\"\r\n");
            dataOutputStream.writeBytes("Content-Type:application/octet-stream\r\n");
            dataOutputStream.writeBytes("\r\n");
            dataOutputStream.write(getBytes(value));
            dataOutputStream.writeBytes("\r\n");
            number = number + 1;
            if (onUpLoadListener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onUpLoadListener.onUpLoadSuccess(number);
                    }
                });
            }
        }
    }

    private byte[] getBytes(File f) throws IOException {
        FileInputStream in = new FileInputStream(f);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int n;
        while ((n = in.read(b)) != -1) {
            out.write(b, 0, n);
        }
        in.close();
        return out.toByteArray();
    }

    private void writeStringParams() throws IOException {
        Set<String> keySet = strParams.keySet();
        for (Iterator<String> it = keySet.iterator(); it.hasNext(); ) {
            String name = it.next();
            String value = strParams.get(name);
            dataOutputStream.writeBytes("--" + boundary + "\r\n");
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + name
                    + "\"\r\n");
            dataOutputStream.writeBytes("\r\n");
            dataOutputStream.writeBytes(encode(value) + "\r\n");
        }
    }

    private void initConnection() throws IOException {
        httpURLConnection = (HttpURLConnection) this.url.openConnection();
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setConnectTimeout(10000); //连接超时为10秒
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
    }

    private void fail(final String msg) {
        handler.post(new Runnable() {
                         @Override
                         public void run() {
                             if (onUpLoadListener != null) {
                                 onUpLoadListener.onFail(msg);
                             }
                         }
                     }
        );
    }

    public void setDownLoad(File file, int tag) {
        this.file = file;
        this.tag = tag;
    }

    public void setImageDownLoad(int width, int height, int tag) {
        this.width = width;
        this.height = height;
        this.tag = tag;
    }

    private void downLoad() {
        if (url != null) {
            try {
                httpURLConnection = (HttpURLConnection) this.url.openConnection();
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("GET");
                RandomAccessFile randomAccessFile = null;

                if (file.exists()) {
                    String start = "bytes=" + file.length() + "-";
                    httpURLConnection.setRequestProperty("Range", start);
                    randomAccessFile = new RandomAccessFile(file.getAbsolutePath(), "rw");
                    randomAccessFile.seek(file.length());

                } else {
                    file.createNewFile();
                }
                final int fileLenght = httpURLConnection.getContentLength();
                if (file.length() == fileLenght) {
                    return;
                }
                InputStream inputStream = httpURLConnection.getInputStream();
                byte[] buffer = new byte[1024];
                int length = -1;
                compeleteSize = 0;
                OutputStream ouput = new FileOutputStream(file);
                while ((length = inputStream.read(buffer)) > 0) {
                    if (randomAccessFile != null) {
                        randomAccessFile.write(buffer, 0, length);
                    } else {
                        ouput.write(buffer, 0, length);
                    }
                    if (length > 0) {
                        compeleteSize = compeleteSize + length;
                    }
                    if (onDownLoadListener != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onDownLoadListener.onProgress(tag, compeleteSize * 100 / fileLenght);
                            }
                        });
                    }
                }
                ouput.close();
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                if (onDownLoadListener != null) {
                    handler.post(new Runnable() {
                                     @Override
                                     public void run() {
                                         onDownLoadListener.onSuccess();
                                     }
                                 }
                    );
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (onDownLoadListener != null) {
                    onDownLoadListener.onFail(e.getMessage());
                }
            }
        }
    }

    private void downImage() {
        if (url != null) {
            try {
                httpURLConnection = (HttpURLConnection) this.url.openConnection();
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("GET");
                InputStream is = httpURLConnection.getInputStream();
                Bitmap bitmap = null;
                if (is != null) {
                    try {
                        byte[] data = readStream(is);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeByteArray(data, 0, data.length,
                                options);
                        if (options.outWidth > width
                                && options.outHeight > height
                                && (width != 0 && height != 0)) {
                            options.inSampleSize = options.outWidth / width >= options.outHeight
                                    / height ? options.outWidth / width
                                    : options.outHeight / height;
                        }
                        options.inJustDecodeBounds = false;
                        bitmap = BitmapFactory.decodeByteArray(data, 0,
                                data.length, options);
                        ImageCache.getInstance().put(url.getPath() + width + "*" + height, bitmap);
                        diskLruCache.putBitmap(url.getPath() + width + "*" + height, bitmap);
                        if (onDownLoadListener != null) {
                            handler.post(new Runnable() {
                                             @Override
                                             public void run() {
                                                 onDownLoadListener.onSuccess();
                                             }
                                         }
                            );
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (onDownLoadListener != null) {
                        onDownLoadListener.onFail("file not found");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (onDownLoadListener != null) {
                    onDownLoadListener.onFail(e.getMessage());
                }
            }
        }

    }

    /*
     * 得到图片字节流 数组大小
	 */
    public byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }

    private String encode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }

    public void setDiskLruCache(ImageDiskLruCache diskLruCache) {
        this.diskLruCache = diskLruCache;
    }

    @Override
    public void run() {
        if (file != null) {
            downLoad();
        } else {
            downImage();
            diskLruCache = null;
        }
    }


    @Override
    public int compareTo(Object another) {
        return 0;
    }


}
