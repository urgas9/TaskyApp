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

package si.uni_lj.fri.taskyapp.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import si.uni_lj.fri.taskyapp.global.AppHelper;

public class AggregateDataDailyIntentService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String TAG = "AggregateDataDaily";

    public AggregateDataDailyIntentService() {
        super("AggregateDataDailyService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startService(Context context) {
        Intent intent = new Intent(context, AggregateDataDailyIntentService.class);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            AppHelper.aggregateDailyData();
        }
    }

}
