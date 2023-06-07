package kr.ac.dongyang.project.streaming;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import kr.ac.dongyang.project.LoadingDialog;
import kr.ac.dongyang.project.MainActivity2;
import kr.ac.dongyang.project.R;
import kr.ac.dongyang.project.bluetooth.BluetoothController;

public class VideoStreaming extends AppCompatActivity {
    //미디어 스캐너
    private MediaScanner ms = MediaScanner.newInstance(this);

    private Handler handler;
    ConnectedThread thread;

    int videoWidth, videoHeight;

    private static final String TAG = "video";

    //블루투스용 상수
    private static final String START_STREAMING = "stst";
    public static final String ACK_INIT = "acin";
    public static final String ACK_DATA_RECEIVED = "acdr";

    public static final String DATA_END = "dten";
    public static final String DATA_START = "dtst";

    public static final String BLUETOOTH_END = "bted";
    public static final String LATTEPANDA_END = "laed";

    //bluetooth
    BluetoothController btcl;
    private BluetoothAdapter mBTAdapter;
    private BluetoothSocket latteSocket = null;

    //id지정용
    private ImageView imageView;
    Button btnmain;
    Button capture;

    File path;

    ImageButton back_streaming;

    private Boolean socketConnect = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_video_streaming);

        //videoView 높이 지정
        viewSize();

        //핸들러
        handler = new Handler();

        //id지정
        imageView = (ImageView) findViewById(R.id.imageView2);
        capture = findViewById(R.id.capture);
        btnmain = findViewById(R.id.btnmain);
        back_streaming = findViewById(R.id.back_streaming);

        //블루투스
        btcl = BluetoothController.getController();
        mBTAdapter = btcl.getmBTAdapter();

        if (mBTAdapter.isEnabled()) {
            //블루투스 mac주소
            SharedPreferences device = getSharedPreferences("bluetooth", 0);
            String address = device.getString("latte", "");
            if (address.equals("")) {
                Toast.makeText(getApplicationContext(), "설정에서 라떼판다를 지정해주세요", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                //블루투스 소켓 연결
                connectSocket(address);
            }
        }else{
            Toast.makeText(getApplicationContext(), "블루투스를 켜주세요", Toast.LENGTH_SHORT).show();
            finish();
        }
        btnmain.setOnClickListener((v) -> {
            //인텐트 선언 -> 현재 액티비티, 넘어갈 액티비티
//            Intent intent5 = new Intent(this, MainActivity2.class);
//            startActivity(intent5);
            finish();
        });

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                path = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM);//movies폴더 저장
                String folder = "LatteFactory"; // 폴더 이름
                try {
                    // 현재 날짜로 파일을 저장하기
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                    // 년월일시분초
                    Date currentTime = new Date();
                    String dateString = formatter.format(currentTime);

                    File latte = new File(path, folder);
                    if (!latte.exists()) { // 원하는 경로에 폴더가 있는지 확인
                        latte.mkdirs(); // 폴더 생성
                    }

                    String save = path + "/" + folder + "/" + dateString + ".jpg";
                    FileOutputStream lFileOutputStream = new FileOutputStream(save);

                    imageView.buildDrawingCache();
                    Bitmap captureView = imageView.getDrawingCache();
                    captureView.compress(Bitmap.CompressFormat.JPEG, 100, lFileOutputStream); // 캡쳐
                    lFileOutputStream.close();
                    //미디어스캐닝
                    ms.mediaScanning(save);

                    Toast.makeText(getApplicationContext(), dateString + ".jpg 저장",
                            Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    // TODO: handle exception
                    Log.e("Screen", "" + e.toString());
                }
            }
        });

        back_streaming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        super.onCreate(savedInstanceState);
    }

    private void connectSocket(String address) {
        LoadingDialog loadingDialog = new LoadingDialog(VideoStreaming.this);
        new Thread() {
            @Override
            public void run() {
                if (address.equals("")){
                    Toast.makeText(getApplicationContext(),"블루투스 장치를 확인하세요", Toast.LENGTH_LONG).show();
                }
                else{
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            loadingDialog.setCancelable(false);
                            loadingDialog.show();
                        }
                    });
                    android.bluetooth.BluetoothDevice btDevice = mBTAdapter.getRemoteDevice(address);
                    try {
                        latteSocket = btcl.createLatteSocket(btDevice);
                    } catch (IOException e) {//exception 발생 시
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    try {
                        latteSocket.connect();
                        try{
                            thread = new ConnectedThread(btcl.getLatteSocket());
                            thread.start();
                            thread.write(START_STREAMING.getBytes());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    } catch (IOException e) {//exception 발생 시
                        try {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getBaseContext(), "라떼판다의 블루투스를 확인하세요", Toast.LENGTH_SHORT).show();
                                }
                            });
                            btcl.closeLatteSocket();
                        } catch (Exception e2) {
                            //insert code to deal with this
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                            e.printStackTrace();
                        }
                        finish();//연결 실패시 BlackBoxActivity 종료
                    }
                    loadingDialog.dismiss();
                }
            }
        }.start();
    }


    private void viewSize(){
        Display display = getWindowManager().getDefaultDisplay();  // in Activity
        Point size = new Point();
        display.getSize(size); // or getSize(size)
        videoWidth = size.x;
        videoHeight = (int)(videoWidth*(float)3/4);
    }
    @Override
    protected void onStart() {

        Log.d(TAG, "onstart");
        super.onStart();
    }


    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");

        super.onStop();
    }
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        //소켓 연결 끄기
        //소켓 연결 끄기

        try {
            thread.write(BLUETOOTH_END.getBytes());
            thread.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            btcl.closeLatteSocket();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        super.onDestroy();
    }


    public class ConnectedThread extends Thread {

        private static final String TAG = "BluetoothHandler";
        private InputStream mInputStream;
        private OutputStream mOutputStream;
        private BluetoothSocket mBluetoothSocket;

        public ConnectedThread(BluetoothSocket socket) {
            mBluetoothSocket = socket;
            try {
                mInputStream = mBluetoothSocket.getInputStream();
                mOutputStream = mBluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e){
                e.printStackTrace();

            }


        }

        public void onReceivedData(byte[] bytes) {
            final Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            //이미지 크기 변경
            final Bitmap resized = Bitmap.createScaledBitmap(image, videoWidth, videoHeight,true);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, ""+bytes.length);
                    imageView.setImageBitmap(resized);
                }
            });
        }

        @Override
        public void run() {
            byte[] data;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while (!Thread.currentThread().isInterrupted()){
                try {
                    if(btcl.getLatteSocket() == null){
                        break;
                    }
                    int available = mInputStream.available();
                    if (available > 0) {//값이 있을때
                        data = new byte[available];
                        mInputStream.read(data);
                        int dataLength = data.length;
                        if (dataLength == 4) {//ack나 이런거일때
                            String key = new String(data, "UTF-8").trim();
                            if (key.equals(DATA_START)) {//데이터 시작일때
                                write(ACK_INIT.getBytes());
                                byteArrayOutputStream = new ByteArrayOutputStream();
                            } else if (key.equals(DATA_END)) {//데이터 끝일때
                                final ByteArrayOutputStream finalByteArrayOutputStream = byteArrayOutputStream;
                                onReceivedData(finalByteArrayOutputStream.toByteArray());
                                write(ACK_DATA_RECEIVED.getBytes()); //Sending the data received ack
                            } else if(key.equals(LATTEPANDA_END)){
                                Log.d(TAG, "keyboard interrupt");
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        onDestroy();
                                    }
                                });
                                break;
                            }
                        } else {
                            byteArrayOutputStream.write(data);
                            final int completeDataSize = byteArrayOutputStream.size();
                            if (completeDataSize > 4) {//크기가 4보다 클때
                                final byte[] completeData = byteArrayOutputStream.toByteArray();//byte배열로 저장
                                byte[] keyData = Arrays.copyOfRange(completeData, completeDataSize - 4, completeDataSize);//응답문자 제외하고 새 배열 keyData에 저장(응답문자 후 ~ 끝)
                                String key = new String(keyData, "UTF-8").trim();//양쪽 공백 제거
                                if (key.equals(DATA_END)) {
                                    onReceivedData(Arrays.copyOfRange(completeData, 0, completeDataSize - 4));//data_end제외 후 보냄

                                    //ack-data-received 보냄
                                    write(ACK_DATA_RECEIVED.getBytes()); //Sending the data received ack
                                }else if(key.equals(LATTEPANDA_END)){
                                    Log.d(TAG, "keyboard interrupt");
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            onDestroy();
                                        }
                                    });
                                    break;
                                }
                            }
                        }

                    }


                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e){
                    //소켓 없을때
                    Log.d(TAG, "socket is null");
                    e.printStackTrace();
                    break;

                }
            }
            //인터럽트시(스레드 종료) 종료메시지 보냄
            write(BLUETOOTH_END.getBytes());

        }
        public void write(byte[] data) {
            try {
                mOutputStream.write(data);
                mOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                mBluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }

}