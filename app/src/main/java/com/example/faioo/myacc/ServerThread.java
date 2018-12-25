package com.example.faioo.myacc;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static com.example.faioo.myacc.MainActivity.MSG_REV;
import static com.example.faioo.myacc.MainActivity.MSG_SEND;
import static com.example.faioo.myacc.MainActivity.MSG_STOP;

/**
 * Created by Faioo on 2018/5/31.
 */

public class ServerThread implements Runnable {

    //private ServerSocket ss;
    private int mPort;
    private Handler mHandler;
    private Socket s;
    public Handler revHandler;
    OutputStream out = null;
    BufferedReader in = null;
    ServerSocket ss;

    public ServerThread(int port, Handler handler)
    {
        //this.ss = ss;
        mPort = port;
        mHandler = handler;
    }

    @Override
    public void run() {
        try {
            ss = new ServerSocket(mPort);
            while (true)
            {
                //阻塞
                s = ss.accept();
                //socketArrayList.add(s);
                //write
                out = s.getOutputStream();
                //read
                in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                new Thread(){
                    @Override
                    public void run()
                    {
                        super.run();
                        try {
                            String content = null;
                            while ((content = in.readLine()) != null) {
                                Log.d("fai","rev start1");
                                Message msg = new Message();
                                if(content == "stop")
                                {
                                    //s.close();
                                    //in.close();
                                    //out.close();
                                    Log.d("fai",content);
                                    msg.what = MSG_STOP;
                                    msg.obj = content;
                                    mHandler.sendMessage(msg);
                                }
                                if (content == "start")
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
                Looper.prepare();
                revHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == MSG_SEND) {
                            try {
                                Log.d("fai","send start2");
                                out.write((msg.obj.toString() + "\r\n").getBytes("utf-8"));
                                out.flush();
                                Log.d("fai","send start3");
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.d("Fai","异常1");
                            }
                        }
                        if (msg.what == MSG_STOP) {
                            try {
                                out.write((msg.obj.toString() + "\r\n").getBytes("utf-8"));
                                out.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.d("Fai","异常2");
                            }
                        }
                    }
                };
                Looper.loop();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

