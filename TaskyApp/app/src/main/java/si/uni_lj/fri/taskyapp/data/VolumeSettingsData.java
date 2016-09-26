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

package si.uni_lj.fri.taskyapp.data;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

/**
 * Created by urgas9 on 26-Apr-16, OpenHours.com
 */
public class VolumeSettingsData {

    private int ringtoneVolume;
    private int musicVolume;

    public VolumeSettingsData(Context ctx) {
        super();
        AudioManager audio = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);

        this.ringtoneVolume = (int) (audio.getStreamVolume(AudioManager.STREAM_RING) * 100 / (double) audio.getStreamMaxVolume(AudioManager.STREAM_RING));
        this.musicVolume = (int) (audio.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / (double) audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        Log.d("VolumeSettingsData", "Current volume percentages, [Ring]: " + ringtoneVolume + ", [Music]:" + musicVolume);

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
