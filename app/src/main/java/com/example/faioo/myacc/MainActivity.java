package com.example.faioo.myacc;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;



public class MainActivity extends Activity  {



    TextView x = null;
    TextView y = null;
    TextView z = null;
    TextView fileName;
    TextView contentWrite;
    Button btnStart;
    Button btnStop;
    TextView contentRead;

    int count=0;
    final String mfileName = "/texts.txt" ;
    public String path = "/storage/emulated/0/Download";


  //  private FileService fileService;

    public String result = "";
    public String inSdcardPath = "";
    public String outSdcaraPath = "";


    ArrayList<String> mDataListX = new ArrayList<String>();
    ArrayList<String> mDataListY = new ArrayList<String>();
    ArrayList<String> mDataListZ = new ArrayList<String>();
    //设置LOG标签
    private static final String TAG = "sensor";
    private SensorManager sm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**
         * 控件初始化
         */
        initView();
    }

    private long lastShowTime = 0l;
    private String lastShowMsg = null;
    private String curShowMsg = null;


    private void customShowToast(Context context, CharSequence s) {
        curShowMsg = s.toString();
        long curShowTime = System.currentTimeMillis();
        if (curShowMsg.equals(lastShowMsg)) {
            if (curShowTime - lastShowTime > 2000) {
                Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
                lastShowTime = curShowTime;
                lastShowMsg = curShowMsg;
            }
        } else {
            Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
            lastShowTime = curShowTime;
            lastShowMsg = curShowMsg;
        }
    }

    private void write(String content)
    {
        try {
            FileOutputStream fos = openFileOutput("datainfo.txt",MODE_PRIVATE);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(content);

            //customShowToast(MainActivity.this, "文件写入中...");


            //Toast.makeText(MainActivity.this,"文件写入中...",Toast.LENGTH_SHORT).show();
            bw.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void read()
    {
    }

    public void initView() {
        x = (TextView) findViewById(R.id.editTextx);
        y = (TextView) findViewById(R.id.editTexty);
        z = (TextView) findViewById(R.id.editTextz);
        fileName = (TextView) findViewById(R.id.fileName);
        contentWrite = (TextView) findViewById(R.id.contentWrite);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
        contentRead = (TextView) findViewById(R.id.contentRead);

        btnStart.setOnClickListener(new View.OnClickListener() {
            //String[] strings= {"http://img.doooor.com/img/forum/201412/05/220220e93j9j809wcwz9hb.jpg"};
            @Override
            public void onClick(View view) {


                sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                int sensorType = Sensor.TYPE_ACCELEROMETER;
                int sensorType2 = Sensor.TYPE_LINEAR_ACCELERATION;
                //20Hz=50000,50Hz=20000 100Hz=10000
                sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(sensorType2), 10000);
                count =0;


            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
          //  String[] strings= {"http://img.doooor.com/img/forum/201412/05/220220e93j9j809wcwz9hb.jpg"};
            @Override
            public void onClick(View view) {
                sm.unregisterListener(myAccelerometerListener);
                contentWrite.setText("");
                contentRead.setText("已保存文件！");
                Toast.makeText(MainActivity.this,"已保存文件！.",Toast.LENGTH_SHORT).show();
            }
        });
    }

    final SensorEventListener myAccelerometerListener = new SensorEventListener(){

        //复写onSensorChanged方法
        public void onSensorChanged(SensorEvent sensorEvent){
            if(sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
                Log.i(TAG,"onSensorChanged");

                float X_lateral = sensorEvent.values[0];
                float Y_longitudinal = sensorEvent.values[1];
                float Z_vertical = sensorEvent.values[2];
                Log.i(TAG,"\n heading "+X_lateral);
                Log.i(TAG,"\n pitch "+Y_longitudinal);
                Log.i(TAG,"\n roll "+Z_vertical);

                x.setText("X: "+X_lateral);
                y.setText("Y: "+Y_longitudinal);
                z.setText("Z: "+(Z_vertical));

                String path = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"Download"+File.separator+"123.txt";
                File file = new File(path);

                float f[]={X_lateral,Y_longitudinal,Z_vertical};

                try {
                    //Toast.makeText(MainActivity.this,"文件写入中...",Toast.LENGTH_SHORT).show();
                    contentWrite.setText("");
                    contentWrite.setText("文件写入中...");
                    FileOutputStream out = new FileOutputStream(file,true);

                    //out.write(("\n heading "+X_lateral).getBytes());
                    //out.write(("\n pitch "+Y_longitudinal).getBytes());
                    //out.write(("\n roll "+Z_vertical).getBytes());
                    out.write((f[0]+"\t"+f[1]+"\t"+f[2]+"\n").getBytes());
                    //out.write((X_lateral+",").getBytes());
                    //out.write((Y_longitudinal+",").getBytes());
                    //out.write((Z_vertical+"\n").getBytes());
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.e("","path : "+path);

                write(z.getText().toString());
                count++;
                if (count == 12800)
                {
                    sm.unregisterListener(myAccelerometerListener);
                    contentWrite.setText("");
                    contentRead.setText("时间到，已保存文件！");
                    Toast.makeText(MainActivity.this,"时间到，已保存文件！.",Toast.LENGTH_SHORT).show();
                }

            }
        }
        //复写onAccuracyChanged方法
        public void onAccuracyChanged(Sensor sensor , int accuracy){
            Log.i(TAG, "onAccuracyChanged");
        }
    };

    public void onPause(){
        /*
         * 很关键的部分：注意，说明文档中提到，即使activity不可见的时候，感应器依然会继续的工作，测试的时候可以发现，没有正常的刷新频率
         * 也会非常高，所以一定要在onPause方法中关闭触发器，否则讲耗费用户大量电量，很不负责。
         * */
        super.onPause();
    }




}
