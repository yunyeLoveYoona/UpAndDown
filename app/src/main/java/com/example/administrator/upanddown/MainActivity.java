package com.example.administrator.upanddown;

import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;


public class MainActivity extends ActionBarActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.text);
        YunDownLoadQueue yunDownLoadQueue = new YunDownLoadQueue(this);
        String sdCard = Environment.getExternalStorageDirectory().toString();

        OnDownLoadListener onDownLoadListener = new OnDownLoadListener() {
            @Override
            public void onSuccess() {
                textView.setText("下载成功");
            }

            @Override
            public void onFail(String error) {

            }

            @Override
            public void onProgress(int tag, int percent) {
                textView.setText(percent + "%");

            }
        };
        yunDownLoadQueue.addImageDownload("http://img1.3lian.com/2015/w7/98/d/22.jpg", 100, 100, 1, onDownLoadListener);
        yunDownLoadQueue.addImageDownload("http://pic1.nipic.com/2008-12-09/200812910493588_2.jpg"
                , 100, 100, 1, onDownLoadListener
        );
        yunDownLoadQueue.addDownload("http://dlsoft2.downza.cn//2016/02/com.tencent.mm_054825.apk", new File(sdCard + "/wechat.apk"), 1, onDownLoadListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
