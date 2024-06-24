package com.example.bluetoothturretcontrol;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity implements //View.OnClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private final static String MODULE_MAC = "20:14:11:14:09:11";
    private final static int START_TRANSMIT_CHAR = 0xFF88FF88;
    private final static int REQUEST_ENABLE_BT = 1;
    private final static byte SEEK_BAR_BASE_INDEX = 0;
    private final static byte SEEK_BAR_ARM_INDEX = 1;
    private final static byte SEEK_BAR_FIRE_INDEX = 2;
    private final static byte BUTTON_BASE_INDEX = 10;
    private final static byte BUTTON_ARM_INDEX = 11;
    private final static byte BUTTON_FIRE_INDEX = 12;
    private final static byte TRIGGER_MOVE_UP = 1;
    private final static byte TRIGGER_MOVE_DOWN = 0;
    private final static byte TRIGGER_MOVE_RIGHT = 1;
    private final static byte TRIGGER_MOVE_LEFT = 0;
    private final static byte TRIGGER_FIRE_ALL = 1;
    private final static byte TRIGGER_FIRE = 0;
    private final static byte BUTTON_OFF_INDEX = 100;
    private final static byte TRIGGER_MOVE_OFF = 0;

    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothManager bluetoothManager = null;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private OutputStream mmOutputStream;
    private InputStream mmInputStream;

    private SeekBar seekBarBase;
    private SeekBar seekBarArm;
    private SeekBar seekBarFire;

    Button buttonBtConnect;
    Button buttonUp;
    private Button buttonDown;
    private Button buttonLeft;
    private Button buttonRight;
    private Button buttonFire;
    private Button buttonFireAll;

    private TextView textSeekBarBaseProgress;
    private TextView textSeekBarArmProgress;
    private TextView textSeekBarFireProgress;

    byte buttonIndex;
    byte direction;
    boolean buttonPressed = false;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonBtConnect = findViewById(R.id.BtConnect);
        buttonBtConnect.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.i(TAG, "Bt Connect pressed");
                    checkForConnection();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.i(TAG, "Bt Connect no longer pressed");
                }
                //needed to get both calls
                return true;
            }
        });

        buttonUp = findViewById(R.id.ButtonUp);
        buttonUp.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.i(TAG, "Up pressed");
                    sendData(BUTTON_ARM_INDEX, TRIGGER_MOVE_UP);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.i(TAG, "Up no longer pressed");
                    sendData(BUTTON_OFF_INDEX, TRIGGER_MOVE_OFF);
                }
                //needed to get both calls
                return true;
            }
        });


        buttonDown = findViewById(R.id.ButtonDown);
        buttonDown.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.i(TAG, "Down pressed");
                    sendData(BUTTON_ARM_INDEX, TRIGGER_MOVE_DOWN);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.i(TAG, "Down no longer pressed");
                    sendData(BUTTON_OFF_INDEX, TRIGGER_MOVE_OFF);
                }
                //needed to get both calls
                return true;
            }
        });

        buttonLeft = findViewById(R.id.ButtonLeft);
        buttonLeft.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.i(TAG, "Left pressed");
                    sendData(BUTTON_BASE_INDEX, TRIGGER_MOVE_LEFT);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.i(TAG, "Left no longer pressed");
                    sendData(BUTTON_OFF_INDEX, TRIGGER_MOVE_OFF);
                }
                //needed to get both calls
                return true;
            }
        });

        buttonRight = findViewById(R.id.ButtonRight);
        buttonRight.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.i(TAG, "Right pressed");
                    sendData(BUTTON_BASE_INDEX, TRIGGER_MOVE_RIGHT);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.i(TAG, "Right no longer pressed");
                    sendData(BUTTON_OFF_INDEX, TRIGGER_MOVE_OFF);
                }
                //needed to get both calls
                return true;
            }
        });

        buttonFire = findViewById(R.id.ButtonFire);
        buttonFire.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.i(TAG, "Fire pressed");
                    sendData(BUTTON_FIRE_INDEX, TRIGGER_FIRE);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.i(TAG, "Fire no longer pressed");
                    sendData(BUTTON_OFF_INDEX, TRIGGER_MOVE_OFF);
                }
                //needed to get both calls
                return true;
            }
        });

        buttonFireAll = findViewById(R.id.ButtonFireAll);
        buttonFireAll.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.i(TAG, "Fire All pressed");
                    sendData(BUTTON_FIRE_INDEX, TRIGGER_FIRE_ALL);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.i(TAG, "Fire All no longer pressed");
                    sendData(BUTTON_OFF_INDEX, TRIGGER_MOVE_OFF);
                }
                //needed to get both calls
                return true;
            }
        });

        textSeekBarBaseProgress = (TextView) findViewById(R.id.TextSeekBarBaseProgress);
        textSeekBarArmProgress = (TextView) findViewById(R.id.TextSeekBarArmProgress);
        textSeekBarFireProgress = (TextView) findViewById(R.id.TextSeekBarFireProgress);

        SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                byte seekBarIndex = (byte) 255;
                int seekBarId = seekBar.getId();
                if (seekBarId == R.id.SeekBarBase) {
                    seekBarIndex = SEEK_BAR_BASE_INDEX;
                    textSeekBarBaseProgress.setText(String.valueOf(progress));
                    Log.i(TAG, String.valueOf(progress));
//                    Log.i(TAG, String.valueOf(textSeekBarBaseProgress));
                } else if (seekBarId == R.id.SeekBarArm) {
                    seekBarIndex = SEEK_BAR_ARM_INDEX;
                    textSeekBarArmProgress.setText(String.valueOf(progress));
//                    Log.i(TAG, String.valueOf(textSeekBarArmProgress));
                } else if (seekBarId == R.id.SeekBarFire) {
                    seekBarIndex = SEEK_BAR_FIRE_INDEX;
                    textSeekBarFireProgress.setText(String.valueOf(progress));
//                    Log.i(TAG, String.valueOf(textSeekBarFireProgress));
                }

                if (seekBarIndex != 255) {
                    Log.i(TAG, "SeekBar change: " + "Index: " + Byte.toString(seekBarIndex) + " Data: " + Byte.toString((byte) progress));
                    sendData(seekBarIndex, (byte) progress);
                } else {
                    Log.i(TAG, "Error: " + "Index: " + Byte.toString(seekBarIndex) + " Data: " + Byte.toString((byte) progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };

        seekBarArm = (SeekBar) findViewById(R.id.SeekBarArm);
        seekBarArm.setOnSeekBarChangeListener(seekBarListener);

        seekBarBase = (SeekBar) findViewById(R.id.SeekBarBase);
        seekBarBase.setOnSeekBarChangeListener(seekBarListener);

        seekBarFire = (SeekBar) findViewById(R.id.SeekBarFire);
        seekBarFire.setOnSeekBarChangeListener(seekBarListener);

        checkForConnection();
    }


    private void checkForConnection() {
        bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Bt Not Supported",
                    Toast.LENGTH_SHORT);
            toast.show();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Please enable bluetooth",
                        Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Searching for device",
                        Toast.LENGTH_SHORT);
                toast.show();
                findBtDevice();
            }
        }
    }

    //when button is pressed to connect, need to findBtDevice as well or likely  go through checkForConnection protocol
    protected void findBtDevice() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

//        if (pairedDevices.size() > 0) {
        if (!pairedDevices.isEmpty()) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                Toast toast = Toast.makeText(getApplicationContext(),
                        deviceName,
                        Toast.LENGTH_SHORT);
                toast.show();
                toast = Toast.makeText(getApplicationContext(),
                        deviceHardwareAddress,
                        Toast.LENGTH_SHORT);
                toast.show();
                if (deviceHardwareAddress.equals(MODULE_MAC)) {
                    mmDevice = device;
                    break;
                }
            }
        }

        if (mmDevice != null) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;

            bluetoothAdapter.cancelDiscovery();

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = mmSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }
            mmInputStream = tmpIn;
            mmOutputStream = tmpOut;


            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
            }

        }

        /*
        Toast toast = Toast.makeText(getApplicationContext(),
                "before Discovery",
                Toast.LENGTH_SHORT);
        toast.show();
        boolean discoveryStarted = bluetoothAdapter.startDiscovery();
        toast = Toast.makeText(getApplicationContext(),
                "Started Discovery",
                Toast.LENGTH_SHORT);
        toast.show();
         */

    }


    protected void sendData(byte index, byte data) {
        try {
            //START_TRANSMIT_CHAR sequence
            byte valueToSend = (byte)((0xFF000000 & START_TRANSMIT_CHAR) >>> 24);
//            Log.i(TAG, "Sequence 1: " + Byte.toString(valueToSend));
            mmOutputStream.write(valueToSend);
            valueToSend = (byte)((0x00FF0000 & START_TRANSMIT_CHAR) >>> 16);
//            Log.i(TAG, "Sequence 2: " + Byte.toString(valueToSend));
            mmOutputStream.write(valueToSend);
            valueToSend = (byte)((0x0000FF00 & START_TRANSMIT_CHAR) >>> 8);
//            Log.i(TAG, "Sequence 3: " + Byte.toString(valueToSend));
            mmOutputStream.write(valueToSend);
            valueToSend = (byte)((0x000000FF & START_TRANSMIT_CHAR) >>> 0);
//            Log.i(TAG, "Sequence 4: " + Byte.toString(valueToSend));
            mmOutputStream.write(valueToSend);

            //index transmission
            mmOutputStream.write(index);
            Log.i(TAG, "Index: " + Byte.toString(index));

            mmOutputStream.write(data);
            Log.i(TAG, "Data: " + Byte.toString(data));
            //Thread.sleep(0);
        } catch (IOException /*| InterruptedException*/ closeException) {
            Log.e(TAG, "Could not write data", closeException);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        /*
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        */

        // Don't forget to unregister the ACTION_FOUND receiver.
        //unregisterReceiver(receiver);
    }

}