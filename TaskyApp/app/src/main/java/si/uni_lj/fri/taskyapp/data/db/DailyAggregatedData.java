/*
 * Copyright (c) 2016, University of Ljubljana, Slovenia
 *
 * Gasper Urh, gu7668@student.uni-lj.si
 *
 * This project was developed as part of the paper submitted for the UbitTention workshop (in conjunction with UbiComp'16) and my master thesis. For more information, please visit http://projects.hcilab.org/ubittention/
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package si.uni_lj.fri.taskyapp.data.db;

import android.content.Context;
import android.util.Log;

import com.orm.dsl.NotNull;
import com.orm.dsl.Table;
import com.orm.dsl.Unique;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import si.uni_lj.fri.taskyapp.R;

/**
 * Created by urgas9 on 21-Feb-16, OpenHours.com
 */
@Table
public class DailyAggregatedData {
    @Unique
    @NotNull
    private Long id;

    @Unique
    private int dayOfYear;
    private long allReadings;
    private long countLabeled;
    private double averageLabel;

    public DailyAggregatedData() {
    }

    public Long getId() {
        return id;
    }

    public long getAllReadings() {
        return allReadings;
    }

    public void setAllReadings(long allReadings) {
        this.allReadings = allReadings;
    }

    public long getCountLabeled() {
        return countLabeled;
    }

    public void setCountLabeled(long countLabeled) {
        this.countLabeled = countLabeled;
    }

    public double getAverageLabel() {
        return averageLabel;
    }

    public void setAverageLabel(double averageLabel) {
        this.averageLabel = averageLabel;
    }

    public int getDayOfYear() {
        return dayOfYear;
    }

    public void setDayOfYear(int dayOfYear) {
        this.dayOfYear = dayOfYear;
    }

    public String getStringDay() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_YEAR, dayOfYear);
        SimpleDateFormat format = new SimpleDateFormat("EEE");
        return format.format(new Date(c.getTimeInMillis()));
    }

    public String getAverageLabelTaskText(Context ctx) {
        String[] complexitiesArray = ctx.getResources().getStringArray(R.array.task_difficulties_array);

        int base = (int) averageLabel;
        double decimal = averageLabel - base;

        Log.d("DailyAggregatedData", "Base: " + base + ", decimal part: " + decimal);
        if (decimal > 0.5) {
            if (base == 4) {
                return "almost " + complexitiesArray[base];
            }
            return "closer to " + complexitiesArray[base + 1] + " than " + complexitiesArray[base];
        } else {
            if (base == 1) {
                return "close to " + complexitiesArray[base];
            }
            return "closer to " + complexitiesArray[base] + " than " + complexitiesArray[base + 1];
        }
    }
}
