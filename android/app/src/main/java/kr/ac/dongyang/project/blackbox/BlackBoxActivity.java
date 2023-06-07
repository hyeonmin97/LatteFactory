package kr.ac.dongyang.project.blackbox;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import kr.ac.dongyang.project.LoadingDialog;
import kr.ac.dongyang.project.R;
import kr.ac.dongyang.project.bluetooth.BluetoothController;
import kr.ac.dongyang.project.streaming.MediaScanner;

public class BlackBoxActivity extends AppCompatActivity {
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    
    //블루투스용 상수
    private static final String BLBK = "blbk";
    private static final String SEND_DATA = "sedt";
    private static final String BLBK_END ="bled";
    private static final String FILE_NAME_END = "fned";


    private static final String FILE_LIST = "fist";
    private static final String ACK_FILE_LIST = "acfl";
    private static final String LIST_START = "lsst";
    private static final String LIST_END = "lied";
    private static final String FLIE_LIST_END = "file";

    private static final String BLUETOOTH_END = "bted";


    public static final String ACK_INIT = "acin";
    public static final String ACK_DATA_RECEIVED = "acdr";

    public static final String DATA_END = "dten";
    public static final String DATA_START = "dtst";

    public static final String LATTEPANDA_END = "laed";
    

    private static final String DATA = "data";
    private static final String ACK_FILE_NAME = "acfn";

    private static final String FILE_SIZE = "fisz";
    private static final String SEND_FILE_SIZE = "sfsz";

    private static final String ACK_FILE_SIZE = "acfs";

    private static final String TAG = "video";

    ImageButton back_black;

    private Handler handler;
    int videoWidth, videoHeight;
    private LinearLayout baseLayout;
    private VideoView videoView;
    String fileName;
    File path;

    ConnectedThread thread;
    
    //bluetooth
    BluetoothController btcl;
    private BluetoothAdapter mBTAdapter;
    private BluetoothSocket latteSocket = null;

    //미디어 스캐너
    private MediaScanner ms = MediaScanner.newInstance(this);

    int filesize=0;
    int recvSize=0;
    int total = 0;
    ProgressDialog dialog;

    DisplayMetrics dm;
    int size;
    LinearLayout.LayoutParams param;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_black_box);
        
        //핸들러
        handler = new Handler();
        
        //id 지정
        videoView = (VideoView)findViewById(R.id.videoView);
        baseLayout = (LinearLayout)findViewById(R.id.baseLayout);
        back_black = findViewById(R.id.backPress_black);
        
        //핸드폰 화면크기 불러오기
        viewSize();
        
        //movies폴더 저장
        path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES);

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
        dm = getResources().getDisplayMetrics();
        size = Math.round(10 * dm.density);
        param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        param.topMargin = size;


        super.onCreate(savedInstanceState);

        back_black.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     *라떼판다의 블루투스 소켓과 연결하는 메소드
     * @param address 블루투스 장치 mac 주소
     */
    private void connectSocket(String address) {
        LoadingDialog loadingDialog = new LoadingDialog(BlackBoxActivity.this);
        new Thread() {
            @Override
            public void run() {
                if (address.equals("")){
                    //Toast.makeText(getApplicationContext(),"블루투스 장치를 확인하세요", Toast.LENGTH_LONG).show();
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
                            thread.write(FILE_LIST.getBytes());//파일 리스트 보내주세용
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

    /**
     * 스마트폰의 화면을 구해 videoView의 높이를 지정하고 미디어플레이어를 시작하는 메소드
     */
    private void viewSize(){
        Display display = getWindowManager().getDefaultDisplay();  // in Activity
        Point size = new Point();
        display.getSize(size); // or getSize(size)
        videoWidth = size.x;
        videoHeight = (int)(videoWidth*(float)3/4);
        ViewGroup.LayoutParams lp = videoView.getLayoutParams();
        lp.height = videoHeight;
        videoView.setLayoutParams(lp);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
    }
    
    @Override
    protected void onStart() {

        Log.d(TAG, "onstart");
        //블루투스 스레드 시작

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

        /**
         * 소켓을 입력받아 연결 스트림을 생성
         * @param socket
         */
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

        /**
         * byte배열을 입력받아 동영상으로 저장하는 메소드
         * @param bytes
         */
        public void onReceivedBlackBox(byte[] bytes) {
            String folder="latte";
            try{
                File latte = new File(path, folder);
                if (!latte.exists()) { // 원하는 경로에 폴더가 있는지 확인
                    latte.mkdirs(); // latte 폴더 생성
                }
                //디렉터리, 파일명 추출
                String dir = fileName.substring(1,7);
                String file = fileName.substring(8);
                //날짜 폴더 생성
                File videoDir = new File(path  + "/" + folder, dir);
                if(!videoDir.exists()){
                    videoDir.mkdirs();
                }
                try {
                    String save = path + "/" + folder + "/" + dir + "/" + file;
                    Log.d(TAG , " : "+ save);
                    FileOutputStream lFileOutputStream = new FileOutputStream(save);
                    lFileOutputStream.write(bytes);
                    lFileOutputStream.close();
                    //미디어스캐닝
                    ms.mediaScanning(save);
                    //동영상 실행
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Uri uri = Uri.parse(save);
                            videoView.setVideoURI(uri);
                            videoView.setMediaController(new MediaController(BlackBoxActivity.this));
                        }
                    });
                }catch(Exception e){
                    e.printStackTrace(System.out);
                }
            }catch(Exception e){
                e.printStackTrace(System.out);
            }
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
                            if (key.equals(ACK_INIT)) {
                                write((fileName + FILE_NAME_END).getBytes());
                           } else if(key.equals(ACK_FILE_NAME)){
                                write(SEND_FILE_SIZE.getBytes());
                                byteArrayOutputStream = new ByteArrayOutputStream();
                            } else if(key.equals(ACK_FILE_LIST)){
                                write(LIST_START.getBytes());
                                byteArrayOutputStream = new ByteArrayOutputStream();
                            }else if (key.equals(BLBK_END)) {//데이터 끝일때(크기 작으면 여기로)
                                final ByteArrayOutputStream finalByteArrayOutputStream = byteArrayOutputStream;
                                write(ACK_DATA_RECEIVED.getBytes()); //Sending the data received ack
                                onReceivedBlackBox(finalByteArrayOutputStream.toByteArray());
                                dialog.dismiss();
                                filesize=0;
                            }
                        } else {
                            byteArrayOutputStream.write(data);
                            final int completeDataSize = byteArrayOutputStream.size();
                            recvSize = completeDataSize;
                            if(filesize>0){
                                dialog.setProgress(recvSize);
                                Log.d(TAG, ""+recvSize);
                            }
                            if (completeDataSize > 4) {//크기가 4보다 클때
                                final byte[] completeData = byteArrayOutputStream.toByteArray();//byte배열로 저장
                                byte[] keyData = Arrays.copyOfRange(completeData, completeDataSize - 4, completeDataSize);//응답문자 제외하고 새 배열 keyData에 저장(응답문자 후 ~ 끝)
                                String key = new String(keyData, "UTF-8").trim();//양쪽 공백 제거
                                Log.d("recvdata","recv");
                                if (key.equals(BLBK_END)) {//데이터 끝일때(크기 크면 여기)
                                    final ByteArrayOutputStream finalByteArrayOutputStream = byteArrayOutputStream;
                                    write(ACK_DATA_RECEIVED.getBytes()); //Sending the data received ack
                                    onReceivedBlackBox(finalByteArrayOutputStream.toByteArray());
                                    dialog.dismiss();
                                    filesize=0;
                                } else if (key.equals(FILE_SIZE)) {//파일 사이즈일때
                                    final ByteArrayOutputStream finalByteArrayOutputStream = byteArrayOutputStream;
                                    String temp = finalByteArrayOutputStream.toString();
                                    int length = Integer.parseInt(temp.substring(0,temp.length()-4));
                                    Log.d(TAG, ""+length);
                                    filesize = Integer.parseInt(temp.substring(0,temp.length()-4));
                                    write(ACK_FILE_SIZE.getBytes()); //Sending the data received ack
                                    byteArrayOutputStream = new ByteArrayOutputStream();
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                                 dialog = new ProgressDialog(BlackBoxActivity.this);
                                                 dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                                 dialog.setIndeterminate(false);
                                                 dialog.setMax(filesize);
                                                 dialog.setProgress(0);
                                                 dialog.setCancelable(false);//다이얼로그 안꺼지게
                                                 dialog.setMessage("데이터 다운중..");
                                                 dialog.show();
                                        }
                                    });
                                    total = 0;
                                } else if (key.equals(LIST_END)) {//디렉터리, 파일 리스트 끝
                                    final ByteArrayOutputStream finalByteArrayOutputStream = byteArrayOutputStream;
                                    String temp = finalByteArrayOutputStream.toString();
                                    String str = temp.substring(0,temp.length()-4);//응답문자 뺌
                                    str = str.replace("\'", "");
                                    String[] splitStr = str.split(", ");

                                    //디렉터리, 파일명 구분
                                    LinkedHashMap<String, ArrayList<String>> hashData = getDirFile(splitStr);
                                    for(String dkey : hashData.keySet()){
                                        Log.d(TAG, dkey);
                                        Log.d(TAG, String.valueOf(hashData.get(dkey)));
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            createButton(dkey, hashData.get(dkey),baseLayout);
                                        }
                                    }
                                    write(FLIE_LIST_END.getBytes());//파일리스트 전체작업 끝
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                } catch (NullPointerException e){
                    //소켓 없을때
                    Log.d(TAG, "socket is null");
                    e.printStackTrace();
                    break;

                }
            }
            //인터럽트시(스레드 종료) 종료메시지 보냄
            try {
                write(BLUETOOTH_END.getBytes());
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public synchronized void write(byte[] data) {
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

        /**
         * 날짜와시간으로 구성된 이름을 폴더, 파일로 구분해주는 함수
         * @param data 날짜\\시간.mp4 형식으로 되어있는 문자열의 집합
         * @return 날짜를 KEY로 하고 해당 날짜의 파일들의 목록이 ArrayList에 추가된 VALUE로 구성되는 HashMap리턴 / 순서를 보장하기위해 LinkedHashMap 사용
         */
        public LinkedHashMap<String, ArrayList<String>> getDirFile(String[] data){
            LinkedHashMap<String, ArrayList<String>> hashData = new LinkedHashMap<String, ArrayList<String>>();
            for(int i=0;i<data.length;i++){
                String[] splitData = data[i].split("!");// \\로 구분함
                ArrayList<String> fileList = new ArrayList<String>();
                if(hashData.containsKey(splitData[0])){//해시맵에 해당 날짜 폴더 존재할때
                    fileList = hashData.get(splitData[0]);
                }
                fileList.add(splitData[1]);//어레이리스트에 시간(파일)추가
                hashData.put(splitData[0], fileList);//해시맵에 추가
            }
            return hashData;
        }

        /**
         * 디렉터리명으로 버튼을 생성하고 해당 디렉터리의 파일명들이 들어간 리스트뷰 생성하는 메소드
         * @param dir 디렉터리명
         * @param fileNames ArrayList<String>형식의 파일명
         * @param baseLayout 버튼과 리스트뷰가 들어있는 레이아웃을 추가할 레이아웃
         */
        @RequiresApi(api = Build.VERSION_CODES.M)
        @SuppressLint({"ResourceAsColor", "ResourceType"})
        public void createButton(String dir, ArrayList<String> fileNames, LinearLayout baseLayout){
            int listId = generateViewId();
            //버튼과 리스트뷰를 붙일 레이아웃
            LinearLayout myLinearLayout = new LinearLayout(getApplicationContext());
            myLinearLayout.setOrientation(LinearLayout.VERTICAL);
            //리스트 뷰
            NonScrollListView buttonView = new NonScrollListView(getApplicationContext());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1,fileNames);
            buttonView.setAdapter(adapter);
            buttonView.setId(listId);
            buttonView.setVisibility(View.GONE);//안보이게
            buttonView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    fileName = "/" + dir + "/" + ((TextView) view).getText().toString();//파이썬에 전송하기 위한 파일이름 설정
                    String fullDir = path + "/latte" + fileName;
                    Log.d(TAG, "full path : " + fullDir);
                    File file = new File(fullDir);
                    if(file.exists()){
                        Log.d(TAG, "파일이 존재합니다.");
                        Uri uri = Uri.parse(fullDir);
                        videoView.setVideoURI(uri);
                        videoView.setMediaController(new MediaController(BlackBoxActivity.this));
                    }
                    else {
                        Log.d(TAG, "파일을 다운로드합니다.");
                        thread.write(BLBK.getBytes());
                    }
                }
            });
            Button button = new Button(getApplicationContext());
            button.setText(dir);
            button.setTag(dir);

            button.setBackgroundResource(R.drawable.shape_button);
            button.setTextAppearance(R.style.button);
            button.setLayoutParams(param);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(buttonView.getVisibility() == View.GONE){
                        buttonView.setVisibility(View.VISIBLE);
                    }
                    else{
                        buttonView.setVisibility(View.GONE);
                    }
                }
            });
            myLinearLayout.addView(button);
            myLinearLayout.addView(buttonView);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    baseLayout.addView(myLinearLayout);
                }
            });
        }
    }
    
    //id 지정하기 위한 메서드
    public static int generateViewId() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return generateViewIdSdk17Under();
        } else {
            return View.generateViewId();
        }
    }

    private static int generateViewIdSdk17Under() {
        for (;;) {
            final int result = sNextGeneratedId.get();
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1;
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }


}