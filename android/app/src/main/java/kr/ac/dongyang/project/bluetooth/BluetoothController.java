package kr.ac.dongyang.project.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class BluetoothController {
    public static UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public static UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//SerialPortServiceClass_UUID(spp)
    private BluetoothAdapter mBTAdapter;
    private BluetoothSocket latteSocket = null;
    private BluetoothSocket raspberrySocket = null;

    private BluetoothController(){}

    private static class InnerClass {
        private static final BluetoothController instance = new BluetoothController();
    }

    public static BluetoothController getController(){
        return InnerClass.instance;
    }

    public BluetoothAdapter getmBTAdapter() {
        if(InnerClass.instance.mBTAdapter == null)
            InnerClass.instance.mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        return InnerClass.instance.mBTAdapter;
    }

    public BluetoothSocket getLatteSocket() {
        return InnerClass.instance.latteSocket;
    }
    public BluetoothSocket getRaspberrySocket() throws NullPointerException{
        return InnerClass.instance.raspberrySocket;
    }

    public BluetoothSocket createLatteSocket(BluetoothDevice device) throws IOException {
        try {
            InnerClass.instance.latteSocket = device.createInsecureRfcommSocketToServiceRecord(InnerClass.instance.MY_UUID);
            return InnerClass.instance.latteSocket;
        } catch (Exception e) {
            Log.e("BluetoothController", "Could not create Insecure RFComm Connection",e);
        }
        return  device.createRfcommSocketToServiceRecord(InnerClass.instance.MY_UUID);
    }

    public BluetoothSocket createRaspberrySocket(BluetoothDevice device) throws IOException {
        try {
            InnerClass.instance.raspberrySocket = device.createInsecureRfcommSocketToServiceRecord(InnerClass.instance.SPP_UUID);
            return InnerClass.instance.raspberrySocket;
        } catch (Exception e) {
            Log.e("BluetoothController", "Could not create Insecure RFComm Connection",e);
        }
        return  device.createRfcommSocketToServiceRecord(InnerClass.instance.MY_UUID);
    }

    public void closeLatteSocket() throws IOException, NullPointerException {
        InnerClass.instance.latteSocket.close();
        InnerClass.instance.latteSocket = null;
    }

    public void closeRaspberrySocket() throws IOException, NullPointerException {
        InnerClass.instance.raspberrySocket.close();
        InnerClass.instance.raspberrySocket = null;
    }

    public boolean latteIsOpen(){
        boolean open;
        try {
             open = InnerClass.instance.latteSocket.isConnected();
             return open;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean raspberryIsOpen(){
        boolean open;
        try {
            open = InnerClass.instance.raspberrySocket.isConnected();
            return open;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
