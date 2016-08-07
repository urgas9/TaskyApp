package si.uni_lj.fri.taskyapp.adapter;

import android.bluetooth.BluetoothDevice;

/**
 * Created by veljko on 17/05/16.
 */
public class ListWearableItem implements Comparable<ListWearableItem> {

    private final String mItemName;
    private final String mItemKey;
    private final BluetoothDevice mBluetoothDevice;

    public ListWearableItem(String itemName, String itemKey, BluetoothDevice bluetoothDevice) {
        mItemKey = itemKey;
        mItemName = itemName;
        mBluetoothDevice = bluetoothDevice;
    }


    public String getItemKey() {
        return mItemKey;
    }


    public String getItemName() {
        return mItemName;
    }


    public BluetoothDevice getBluetoothDevice() {
        return mBluetoothDevice;
    }


    @Override
    public String toString() {
        return mItemName;
    }


    @Override
    public int compareTo(ListWearableItem item) {
        int nameCmp = mItemName.compareTo(item.getItemName());
        return (nameCmp != 0 ? nameCmp : mItemKey.compareTo(item.getItemKey()));
    }
}
