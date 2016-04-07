package si.uni_lj.fri.taskyapp;


import android.annotation.TargetApi;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import si.uni_lj.fri.taskyapp.data.OfficeHoursObject;
import si.uni_lj.fri.taskyapp.data.network.AuthRequest;
import si.uni_lj.fri.taskyapp.data.network.OptOutResponse;
import si.uni_lj.fri.taskyapp.global.AppHelper;
import si.uni_lj.fri.taskyapp.networking.ApiUrls;
import si.uni_lj.fri.taskyapp.networking.ConnectionHelper;
import si.uni_lj.fri.taskyapp.networking.ConnectionResponse;
import si.uni_lj.fri.taskyapp.sensor.SensingInitiator;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    private static Context mContext;
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(final Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                setPreferenceTextSummaryOnValue((ListPreference) preference, stringValue);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                if (stringValue.isEmpty()) {
                    if (mContext != null) {
                        Toast.makeText(mContext, "Value cannot be empty!", Toast.LENGTH_LONG).show();
                    }
                    return false;
                } else if (preference.getKey().equals("profile_email_text") && !AppHelper.isValidEmail(stringValue)) {
                    if (mContext != null) {
                        Toast.makeText(mContext, "Please enter a valid email address!", Toast.LENGTH_LONG).show();
                    }
                    return false;
                } else if (preference.getKey().equals("profile_office_hours_text")) {


                    if (OfficeHoursObject.validateAndSaveOfficeHoursString(mContext, stringValue)) {
                        stringValue = OfficeHoursObject.prettifyTimeRangeStringValue(stringValue);
                    } else {
                        return false;
                    }

                }

                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static void setPreferenceTextSummaryOnValue(ListPreference listPreference, String stringValue) {
        // For list preferences, look up the correct display value in
        // the preference's 'entries' list.
        int index = listPreference.findIndexOfValue(stringValue);

        // Set the summary to reflect the new value.
        listPreference.setSummary(
                index >= 0
                        ? listPreference.getEntries()[index]
                        : null);
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        mContext = getBaseContext();

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new GeneralPreferenceFragment()).commit();

    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            //bindPreferenceSummaryToValue(findPreference("profile_name_text"));
            bindPreferenceSummaryToValue(findPreference("profile_email_text"));
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
            bindPreferenceSummaryToValue(findPreference("participate_preference"));
            bindPreferenceSummaryToValue(findPreference("profile_office_hours_text"));

            ListPreference participatePreference = (ListPreference) findPreference("participate_preference");

            participatePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(final Preference preference, Object newValue) {
                    final String stringValue = newValue.toString();
                    if (preference.getKey().equals("participate_preference")) {
                        if (!stringValue.equals("0")) {
                            String content = "Are you sure you want to opt-out? We are very sad to see you go :( \n\n" +
                                    "TaskyApp will stop collecting data in background. You can still start sensing on your own.";
                            if (stringValue.equals("2")) {
                                content = "We need your data to finish this research. Data is completely anonymous and we would like to keep it. \n\n" +
                                        "Would you really like to opt-out and delete collected data? You can opt-out without deleting data.";
                            }
                            new MaterialDialog.Builder(getActivity())
                                    .content(content)
                                    .positiveText("Yes")
                                    .negativeText("No")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(MaterialDialog dialog, DialogAction which) {
                                            if (stringValue.equals("2")) {
                                                new OptOutAsyncTask(mContext).execute();
                                            }
                                            new SensingInitiator(mContext).stopAllUpdates();
                                            setPreferenceTextSummaryOnValue((ListPreference) preference, stringValue);
                                        }
                                    })
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(MaterialDialog dialog, DialogAction which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();
                        } else {
                            new SensingInitiator(mContext).senseWithDefaultSensingConfiguration();
                            setPreferenceTextSummaryOnValue((ListPreference) preference, stringValue);
                        }
                    }
                    return true;
                }
            });

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

    }


    static class OptOutAsyncTask extends AsyncTask<Void, Void, ConnectionResponse<OptOutResponse>> {
        private Context appContext;

        public OptOutAsyncTask(Context context) {
            super();
            this.appContext = context.getApplicationContext();
        }

        @Override
        protected ConnectionResponse<OptOutResponse> doInBackground(Void... params) {

            AuthRequest request = new AuthRequest(appContext);

            return ConnectionHelper.postHttpDataCustomUrl(
                    appContext,
                    ApiUrls.getApiCall(appContext, ApiUrls.POST_OPT_OUT),
                    request,
                    OptOutResponse.class);
        }

        @Override
        protected void onPostExecute(ConnectionResponse<OptOutResponse> optOutResponseConnectionResponse) {
            super.onPostExecute(optOutResponseConnectionResponse);

            if (!optOutResponseConnectionResponse.isSuccess()
                    || !optOutResponseConnectionResponse.getContent().isSuccess()) {
                Toast.makeText(appContext, "Opt-out failed. Please check you internet connection.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(appContext, "Thanks for participating. TaskyApp will stop sensing and all you data was removed from server.", Toast.LENGTH_LONG).show();
            }
        }
    }

}
