package com.example.faioo.myacc;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import static com.example.faioo.myacc.MainActivity.MSG_REV;
import static com.example.faioo.myacc.MainActivity.MSG_SEND;
import static com.example.faioo.myacc.MainActivity.MSG_STOP;

/**
 * Created by Faioo on 2018/5/31.
 */

public class ClientThread implements Runnable {
    private Socket mSocket;
    private BufferedReader mBufferedReader = null;
    private OutputStream mOutputStream = null;
    public String mServerAddress;
    //用于向主线程发送消息
    private Handler mHandler;

    public Handler revHandler;

    public ClientThread(String serverAddress, Handler handler) {
        mHandler = handler;
        mServerAddress = serverAddress;
    }
    @Override
    public void run() {
        try {
            mSocket = new Socket(mServerAddress, 30000);
            Log.d("fai","connect success");
            //读
            mBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            //写
            mOutputStream = mSocket.getOutputStream();
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    try {
                        String content = null;
                        while ((content = mBufferedReader.readLine()) != null) {
                            Log.d("fai","rev start1");
                            Log.d("fai","+++++++++++++++++" + content);
                            Message msg = new Message();
                            if(content.equals("stop"))
                            {
                                //mSocket.close();
                                //mBufferedReader.close();
                                //mOutputStream.close();
                                Log.d("fai",content);
                                msg.what = MSG_STOP;
                                msg.obj = content;
                                mHandler.sendMessage(msg);
                            }
                            if (content.equals("start"))
                            {
                                Log.d("fai","rev start2");
                                Log.d("fai",content);
                                msg.what = MSG_REV;
                                msg.obj = content;
                                mHandler.sendMessage(msg);
                                Log.d("fai","rev start3");
                            }
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }.start();
            //由于子线程中没有默认初始化Looper，要在子线程中创建Handler，需要自己写
            Looper.prepare();
            revHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == MSG_SEND) {
                        try {
                            mOutputStream.write((msg.obj.toString() + "\r\n").getBytes("utf-8"));
                            mOutputStream.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d("fai","异常1");
                        }
                    }
                    if (msg.what == MSG_STOP) {
                        try {
                            mOutputStream.write((msg.obj.toString() + "\r\n").getBytes("utf-8"));
                            mOutputStream.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d("Fai","异常2");
                        }
                    }
                }
            };
            Looper.loop();
            mSocket.close();
            mOutputStream.close();
            mBufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("fai","异常2");
        }
    }
}

