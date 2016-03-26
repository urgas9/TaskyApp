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
    public static String getOHUrl(Context ctx, ApiUrls urlType, String... params) {
        String coreUrl = getCoreURL(ctx);
        String urlTypeValue = urlType.getValue();
        StringBuilder sb = new StringBuilder()
                .append(coreUrl)
                .append(urlTypeValue);

        /*if (appendApiParameters) {
            if (urlTypeValue.charAt(urlTypeValue.length() - 1) != '?') {
                sb.append("?");
            }
            sb.append(new DeviceInfo(ctx).toGetString());
        }*/

        return String.format(sb.toString(), params);
    }

    public static String getApiCall(Context ctx, ApiUrls urlType, String... params) {
        return getOHUrl(ctx, urlType, params);
    }

    public static String getWebUrlWithNoParams(Context ctx, ApiUrls urlType, String... params) {
        return getOHUrl(ctx, urlType, params);
    }

    public static String getCoreURL(Context ctx) {
        return ctx.getString(R.string.server_url);
    }

    public String getValue() {
        return url;
    }
}
