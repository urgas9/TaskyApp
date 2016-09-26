/*
 * Copyright (c) 2016, University of Ljubljana, Slovenia
 *
 * Gasper Urh, gu7668@student.uni-lj.si
 *
 * This library was developed as part of the paper submitted for the UbitTention workshop paper (in conjunction with UbiComp'16) and my master thesis. For more information, please visit http://projects.hcilab.org/ubittention/
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package si.uni_lj.fri.taskyapp.data;

import java.util.ArrayList;

import si.uni_lj.fri.taskyapp.data.db.SensorReadingRecord;

/**
 * Created by urgas9 on 7. 02. 2016.
 */
public class SensorReadingDataWithSections {
    private int numSections;
    private ArrayList<SensorReadingRecord> dataList;

    public SensorReadingDataWithSections(int numSections, ArrayList<SensorReadingRecord> data) {
        super();
        this.numSections = numSections;
        this.dataList = data;
    }

    public int getNumSections() {
        return numSections;
    }

    public void setNumSections(int numSections) {
        this.numSections = numSections;
    }

    public ArrayList<SensorReadingRecord> getDataList() {
        return dataList;
    }

    public void setDataList(ArrayList<SensorReadingRecord> dataList) {
        this.dataList = dataList;
    }

    @Override
    public String toString() {
        return "SensorReadingDataWithSections{" +
                "numSections=" + numSections +
                ", dataList=" + dataList +
                '}';
    }
}
