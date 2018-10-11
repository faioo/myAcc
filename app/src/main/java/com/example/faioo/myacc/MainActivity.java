package com.example.faioo.myacc;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


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
    //存放最终数据
    String mfileName = "texts.txt" ;
    //存放未修正的原始数据
    String mfileName2 = "ttt.txt" ;
    public String path = "/storage/emulated/0/Download";
    //设置LOG标签
    private static final String TAG = "sensor";
    private SensorManager sm;
    private Sensor accelerometer; // 加速度传感器
    private Sensor magnetic; // 地磁场传感器
    private Sensor linerAcc;//线性加速度传感器
    //用于之后计算方向
    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];
    //存放旋转矩阵
    private float[] mRotationMatrix = new float[9];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化权限
        mPermission();
        /**
         * 控件初始化
         */
        initView();
    }

    //所有申请的权限
    String[] permissions = new String[]{
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    //未授权但需要申请权限的列表
    List<String> mPermissionList = new ArrayList<>();

    private void mPermission() {
        mPermissionList.clear();
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        if (mPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了
            Toast.makeText(MainActivity.this,"已经授权",Toast.LENGTH_LONG).show();
        } else {//请求权限方法
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }
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
                int sensorType3 = Sensor.TYPE_GYROSCOPE;

                // 初始化加速度传感器
                accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                // 初始化地磁场传感器
                magnetic = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                //初始化线性加速度传感器
                linerAcc = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

                //20Hz=50000,50Hz=20000 100Hz=10000
                //注册线性加速度传感器
                //sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(sensorType2), 10000);
                sm.registerListener(myAccelerometerListener, linerAcc, 10000);
                //注册磁场传感器
                sm.registerListener(myAccelerometerListener,magnetic,10000);
                //注册加速度传感器
                sm.registerListener(myAccelerometerListener,accelerometer,10000);
                //创建新文件名
                fileNameBasedOnTime();
                //sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(sensorType3), 10000);
                count =0;
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            //  String[] strings= {"http://img.doooor.com/img/forum/201412/05/220220e93j9j809wcwz9hb.jpg"};
            @Override
            public void onClick(View view) {
                sm.unregisterListener(myAccelerometerListener);
                contentWrite.setText("");
                contentRead.setText("手动停止,已保存文件！");
                Toast.makeText(MainActivity.this,"stop！.",Toast.LENGTH_SHORT).show();
            }
        });
    }

    final SensorEventListener myAccelerometerListener = new SensorEventListener(){
        //复写onSensorChanged方法
        public void onSensorChanged(SensorEvent sensorEvent){
            //if(sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = sensorEvent.values;
            }
            if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticFieldValues = sensorEvent.values;
            }
            calculateRotationMatrix();
            if(sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                Log.i(TAG, "onSensorChanged");
                float X_lateral = sensorEvent.values[0];
                float Y_longitudinal = sensorEvent.values[1];
                float Z_vertical = sensorEvent.values[2];
                Log.i(TAG, "\n heading " + X_lateral);
                Log.i(TAG, "\n pitch " + Y_longitudinal);
                Log.i(TAG, "\n roll " + Z_vertical);

                x.setText("X: " + X_lateral);
                y.setText("Y: " + Y_longitudinal);
                z.setText("Z: " + (Z_vertical));

                //String path = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"Download"+File.separator+"123.txt";
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Download" + File.separator + mfileName;
                File file = new File(path);

                float f[] = {X_lateral, Y_longitudinal, Z_vertical};

                //写入原始数据
                String path2 = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Download" + File.separator + mfileName2;
                File file2 = new File(path2);
                try {
                    FileOutputStream out2 = new FileOutputStream(file2,true);
                    out2.write((f[0] + "\t" + f[1] + "\t" + f[2] + "\n").getBytes());
                    out2.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.e("", "path : " + path2);
                //-------------

                UpdateRealDate(f);
                try {
                    //Toast.makeText(MainActivity.this,"文件写入中...",Toast.LENGTH_SHORT).show();
                    contentWrite.setText("");
                    contentWrite.setText("文件写入中...");
                    FileOutputStream out = new FileOutputStream(file, true);
                    out.write((f[0] + "\t" + f[1] + "\t" + f[2] + "\n").getBytes());
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.e("", "path : " + path);
                count++;
                if (count == 12800) {
                    sm.unregisterListener(myAccelerometerListener);
                    contentWrite.setText("");
                    contentRead.setText("时间到，已保存文件！");
                    Toast.makeText(MainActivity.this, "时间到，已保存文件！.", Toast.LENGTH_SHORT).show();
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

    public void fileNameBasedOnTime() {
        //当前时间
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String year;
        String month;
        String day;
        String hour;
        String minute;
        String second;
        String my_time_1;
        String my_time_2;
        year = String.valueOf(cal.get(Calendar.YEAR));
        month = String.valueOf(cal.get(Calendar.MONTH)+1);
        day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        if (cal.get(Calendar.AM_PM) == 0)
            hour = String.valueOf(cal.get(Calendar.HOUR));
        else
            hour = String.valueOf(cal.get(Calendar.HOUR)+12);
        minute = String.valueOf(cal.get(Calendar.MINUTE));
        second = String.valueOf(cal.get(Calendar.SECOND));
        my_time_1 = year + "_" + month + "_" + day;
        my_time_2 = hour + ":" + minute + ":" + second;
        mfileName = "123 "+my_time_1+" "+my_time_2+".txt";
        mfileName2 = "456 "+my_time_1+" "+my_time_2+".txt";
    }
    //计算旋转矩阵
    private void calculateRotationMatrix() {
        SensorManager.getRotationMatrix(mRotationMatrix, null, accelerometerValues,
                magneticFieldValues);
    }
    //通过旋转矩阵，修正实际加速度数据
    private void UpdateRealDate(float [] f) {
        //update f[0]
        f[0] = mRotationMatrix[0]*f[0]+mRotationMatrix[1]*f[1]+mRotationMatrix[2]*f[2];
        //update f[1]
        f[1] = mRotationMatrix[3]*f[0]+mRotationMatrix[4]*f[1]+mRotationMatrix[5]*f[2];
        //update f[2]
        f[2] = mRotationMatrix[6]*f[0]+mRotationMatrix[7]*f[1]+mRotationMatrix[8]*f[2];
    }
}
