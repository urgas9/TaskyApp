package si.uni_lj.fri.taskyapp.data.network;

import android.content.Context;
import android.media.AudioManager;
import android.widget.Toast;

/**
 * Created by urgas9 on 26-Apr-16, OpenHours.com
 */
public class VolumeSettingsData {

    private int ringtoneVolume;
    private int musicVolume;

    public VolumeSettingsData(Context ctx){
        super();
        AudioManager audio = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);

        this.ringtoneVolume = (int)(audio.getStreamVolume(AudioManager.STREAM_RING) * 100 / (double)audio.getStreamMaxVolume(AudioManager.STREAM_RING));
        this.musicVolume = (int)(audio.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / (double)audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        Toast.makeText(ctx, "Current volume percentages, [R]: " + ringtoneVolume + ", [M]:"+musicVolume, Toast.LENGTH_LONG).show();
    }

    public int getRingtoneVolume() {
        return ringtoneVolume;
    }

    public void setRingtoneVolume(int ringtoneVolume) {
        this.ringtoneVolume = ringtoneVolume;
    }

    public int getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(int musicVolume) {
        this.musicVolume = musicVolume;
    }

    @Override
    public String toString() {
        return "VolumeSettingsData{" +
                "ringtoneVolume=" + ringtoneVolume +
                ", musicVolume=" + musicVolume +
                '}';
    }
}
