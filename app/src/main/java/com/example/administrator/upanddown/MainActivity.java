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
                textView.setText(percent+"%");

            }
        };
        yunDownLoadQueue.addDownload("http://img3.imgtn.bdimg.com/it/u=4053888973,1375713600&fm=21&gp=0.jpg" +
                "", new File(sdCard + "/img2.jpg"), 1, onDownLoadListener);
        yunDownLoadQueue.addDownload("http://www.netreds.com/bbs/d/file/tupianzha" +
                        "n/meinvtupian/2014-06-29/3bbca576fa4cba8a213d8be2a" +
                        "33304e2.jpg", new File(sdCard + "/img1.jpg"), 1, onDownLoadListener
        );
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
