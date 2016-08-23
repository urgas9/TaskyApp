package si.uni_lj.fri.taskyapp;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.angel.sdk.BleDevice;
import com.angel.sdk.BleScanner;
import com.angel.sdk.BluetoothInaccessibleException;

import junit.framework.Assert;

import java.lang.reflect.Method;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import si.uni_lj.fri.taskyapp.adapter.ListWearableItem;
import si.uni_lj.fri.taskyapp.adapter.ListWearableItemsAdapter;
import si.uni_lj.fri.taskyapp.global.SensorsHelper;
import si.uni_lj.fri.taskyapp.sensor.Constants;

public class ChooseWearableActivity extends AppCompatActivity {


    private static final String TAG = "ChooseWearableActivity";
    private static final int RSSI_UPDATE_INTERVAL = 3000; // Milliseconds
    private static final int DEVICE_SCAN_INTERVAL = 3500; // Milliseconds

    BleScanner mBleScanner = null;
    BleDevice mBleDevice = null;
    ListWearableItemsAdapter mWearableListAdapter;
    @Bind(R.id.wearable_view_switcher)
    ViewSwitcher mViewSwitcher;
    @Bind(R.id.wearables_list_view)
    ListView mListView;
    @Bind(R.id.no_wearables_view)
    TextView mNoWearables;
    BleScanner.ScanCallback mScanCallback = new BleScanner.ScanCallback() {
        @Override
        public void onBluetoothDeviceFound(BluetoothDevice device) {
            Log.d(TAG, "New device found addr " + device.getAddress() + " name " + device.getName());
            if (device.getName() != null && device.getName().startsWith("Angel")) {

                ListWearableItem newDevice = new ListWearableItem(device.getName(), device.getAddress(), device);
                mWearableListAdapter.add(newDevice);
                mWearableListAdapter.addItem(newDevice);
                mWearableListAdapter.notifyDataSetChanged();
            }
        }
    };
    private BluetoothDevice mBtDevice;
    private BroadcastReceiver mPairReceiver;
    private Handler mHandler;
    private Runnable mPeriodicReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_wearable);

        ButterKnife.bind(this);

        mHandler = new Handler(this.getMainLooper());

        mPeriodicReader = new Runnable() {
            @Override
            public void run() {
                mBleDevice.readRemoteRssi();

                mHandler.postDelayed(mPeriodicReader, RSSI_UPDATE_INTERVAL);
            }
        };

        mWearableListAdapter = new ListWearableItemsAdapter(this, R.layout.wearable_item);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.pair_angel_sensor);
            ab.setDisplayHomeAsUpEnabled(true);
        }
        mListView.setEmptyView(findViewById(R.id.no_wearables_view));
        mListView.setAdapter(mWearableListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                stopScan();
                mBtDevice = mWearableListAdapter.getItem(position).getBluetoothDevice();
                Assert.assertTrue(mBtDevice != null);

                SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
                edit.putString(Constants.PREFS_CHOSEN_WEARABLE_NAME, mBtDevice.getName());
                edit.putString(Constants.PREFS_CHOSEN_WEARABLE_MAC, mBtDevice.getAddress());
                edit.commit();

                mWearableListAdapter.notifyDataSetChanged();

                pairDevice(mBtDevice);
                //connectToBT(addr);
            }
        });

        mPairReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                    final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                    if (state == BluetoothDevice.BOND_BONDED) { // && prevState == BluetoothDevice.BOND_BONDING) {
                        if (mBtDevice != null) {
                            Log.d(TAG, "Device paired!!!!");
                            Toast.makeText(ChooseWearableActivity.this, "Device paired!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ChooseWearableActivity.this, "Something went wrong, while pairing!", Toast.LENGTH_LONG).show();
                        }

                    } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                        Log.d(TAG, "Device unpaired.");
                    }

                }
            }
        };

        IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mPairReceiver, intent);
    }

    @OnClick(R.id.btn_scan)
    public void scanButtonClick(View v) {
        startScan();
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (mPairReceiver != null) {
                unregisterReceiver(mPairReceiver);
            }
        }catch (Exception e){
            Log.e(TAG, "Exception while receiver unregister: " + e.getMessage());
        }

        if (mBleDevice != null) {
            mBleDevice.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SensorsHelper.isBluetoothEnabled()) {
            mNoWearables.setText(R.string.no_angel_sensors_found);
            startScan();
        } else {
            mNoWearables.setText(R.string.disabled_bluetooth_msg);
            mViewSwitcher.setDisplayedChild(1);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startScan() {
        mViewSwitcher.setDisplayedChild(0);
        try {

            if (mBleScanner == null) {
                mBleScanner = new BleScanner(this, mScanCallback);
            }
        } catch (BluetoothInaccessibleException e) {
            e.printStackTrace();
        }
        mBleScanner.startScan();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, DEVICE_SCAN_INTERVAL);

    }


    private void stopScan() {
        mViewSwitcher.setDisplayedChild(1);
        if (mBleScanner != null) {
            mBleScanner.stopScan();
        }
    }

}
