package kr.ac.dongyang.project.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Set;

import kr.ac.dongyang.project.LoadingDialog;
import kr.ac.dongyang.project.R;
import kr.ac.dongyang.project.service.BluetoothService;

public class BluetoothActivity extends AppCompatActivity {
    private Handler handler;
    SharedPreferences bluetoothDevice;
    SharedPreferences.Editor editor;
    int flag;

    private BluetoothAdapter mBTAdapter;
    private ArrayAdapter<String> mBTArrayAdapter;
    private Set<android.bluetooth.BluetoothDevice> mPairedDevices;
    private ListView mDevicesListView;

    private String device;

    BluetoothController btcl;
    private BluetoothSocket latteSocket = null;
    private BluetoothSocket raspberrySocket = null;
    TextView textView;
    ImageButton back_blue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        handler = new Handler();

        bluetoothDevice = getSharedPreferences("bluetooth",MODE_PRIVATE);
        editor = bluetoothDevice.edit();

        Button latte = (Button)findViewById(R.id.latte);
        Button raspberry = (Button)findViewById(R.id.raspberry);
        textView = (TextView)findViewById(R.id.mac_address);
        back_blue = findViewById(R.id.backPress_blue);

        mBTArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        mDevicesListView = (ListView)findViewById(R.id.device_lsit);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);

        btcl = BluetoothController.getController();
        mBTAdapter = btcl.getmBTAdapter();

        latte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBTAdapter.isEnabled()) {
                    device = "latte";
                    String mac = bluetoothDevice.getString("latte", "아직 페어링된 장치가 없습니다.");
                    textView.setText(device + " : " + mac);
                    listPairedDevices();
                } else {//블루투스가 꺼져있을때
                    Toast.makeText(getApplicationContext(),"블루투스를 켜주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        raspberry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBTAdapter.isEnabled()) {
                    device = "raspberry";
                    String mac = bluetoothDevice.getString("raspberry","아직 페어링된 장치가 없습니다.");
                    textView.setText(device + " : "+ mac);
                    listPairedDevices();
                } else {//블루투스가 꺼져있을때
                    Toast.makeText(getApplicationContext(),"블루투스를 켜주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        back_blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void listPairedDevices(){
        mBTArrayAdapter.clear();
        mPairedDevices = mBTAdapter.getBondedDevices();
        if(mBTAdapter.isEnabled()) {
            for (android.bluetooth.BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        }
        else
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(!mBTAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }
            String info = ((TextView) view).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0,info.length() - 17);

            //맥주소 저장
            editor.putString(device, address);
            editor.putString(device+"Name", name);
            editor.apply();
            textView.setText(device + " : "+ address);

            //연결됨
            if(device.equals("raspberry")){
                connectSocket(address);
            }else if(device.equals("latte")){
                finish();
            }
        }
    };
    private void connectSocket(String address) {
        LoadingDialog loadingDialog = new LoadingDialog(BluetoothActivity.this);//이거때문에 서비스에 통합 못함
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
                        raspberrySocket = btcl.createRaspberrySocket(btDevice);
                    } catch (IOException e) {//exception 발생 시
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    try {
                        raspberrySocket.connect();
                        //성공시 서비스에 값 전달
                        Intent intent = new Intent(getApplicationContext(), BluetoothService.class);
                        intent.putExtra("bluetooth", true);
                        startService(intent);
                        finish();//액티비티 종료
                    } catch (IOException e) {//exception 발생 시
                        try {
                            raspberrySocket.close();
                            btcl.closeRaspberrySocket();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getBaseContext(), "라즈베리파이의 블루투스를 확인하세요", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (Exception e2) {
                            //insert code to deal with this
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    finally {
                        loadingDialog.dismiss();
                    }
                }
            }
        }.start();
    }
}