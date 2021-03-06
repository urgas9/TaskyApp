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

package si.uni_lj.fri.taskyapp.networking;

import android.content.Context;

import si.uni_lj.fri.taskyapp.R;

/**
 * Created by urgas9 on 13. 01. 2016.
 * <p/>
 * Class to server Api url addresses
 */
public enum ApiUrls {

    POST_RESULTS("/api.php?action=post_records"),
    POST_LEADERBOARD_MSG("/api.php?action=leaderboard_message"),
    POST_OPT_OUT("/api.php?action=opt_out");

    private String url;

    //public static String POST_COMMENT_URL = "/spots/%s/comments?";

    ApiUrls(String s) {
        this.url = s;
    }

    /**
     * Method builds requested URL
     *
     * @param ctx     Context
     * @param urlType type of url to be invoked
     * @param params  params to be passed to urlType's value
     * @return an url string
     */
    public static String getServerUrl(Context ctx, ApiUrls urlType, String... params) {
        String coreUrl = getCoreURL(ctx);
        String urlTypeValue = urlType.getValue();

        /*if (appendApiParameters) {
            if (urlTypeValue.charAt(urlTypeValue.length() - 1) != '?') {
                sb.append("?");
            }
            sb.append(new DeviceInfo(ctx).toGetString());
        }*/

        return String.format(coreUrl + urlTypeValue, params);
    }

    public static String getApiCall(Context ctx, ApiUrls urlType, String... params) {
        return getServerUrl(ctx, urlType, params);
    }

    public static String getWebUrlWithNoParams(Context ctx, ApiUrls urlType, String... params) {
        return getServerUrl(ctx, urlType, params);
    }

    public static String getCoreURL(Context ctx) {
        return ctx.getString(R.string.server_url);
    }

    public String getValue() {
        return url;
    }
}
