/*
 * Copyright (c) 2016, University of Ljubljana, Slovenia
 *
 * Gasper Urh, gu7668@student.uni-lj.si
 *
 * This project was developed as part of the paper submitted for the UbitTention workshop paper (in conjunction with UbiComp'16) and my master thesis. For more information, please visit http://projects.hcilab.org/ubittention/
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

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
