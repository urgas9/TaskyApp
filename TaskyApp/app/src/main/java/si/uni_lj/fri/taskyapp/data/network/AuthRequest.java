package si.uni_lj.fri.taskyapp.data.network;

import android.content.Context;

import si.uni_lj.fri.taskyapp.data.Auth;

/**
 * Created by urgas9 on 14.3.16, OpenHours.com
 */
public class AuthRequest {
    private Auth auth;

    public AuthRequest(Context ctx) {
        super();
        this.auth = new Auth(ctx);
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }
}
