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

package si.uni_lj.fri.taskyapp.splash;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.Bind;
import butterknife.ButterKnife;
import si.uni_lj.fri.taskyapp.R;
import si.uni_lj.fri.taskyapp.SplashScreenActivity;
import si.uni_lj.fri.taskyapp.data.OfficeHoursObject;
import si.uni_lj.fri.taskyapp.global.AppHelper;

public class SplashScreenFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_CURRENT_PAGE = "current_page";
    private static final String TAG = "SplashScreenFragment";
    private static final String TIMERANGEPICKER_TAG = "timerangepicker";

    SharedPreferences mPrefs;
    @Bind(R.id.icon_splash)
    ImageView mIconView;
    @Bind(R.id.title)
    TextView mTitleText;
    @Bind(R.id.content1)
    TextView mText1;
    @Bind(R.id.content2)
    TextView mText2;

    private int mCurrentPage;
    private boolean mEmailFragment;
    private OnSplashScreenFragmentActionListener mCallback;
    private EditText mEmailEditText;
    private TextView mOfficeTimeRangeTv;

    public SplashScreenFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param currentPage Parameter 1.
     * @return A new instance of fragment ProvideInfoFragment.
     */
    public static SplashScreenFragment newInstance(int currentPage) {
        SplashScreenFragment fragment = new SplashScreenFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CURRENT_PAGE, currentPage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCurrentPage = getArguments().getInt(ARG_CURRENT_PAGE);
        }
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root;
        if (mCurrentPage == (SplashScreenActivity.ALL_PAGES - 1)) {
            mEmailFragment = true;
            root = inflater.inflate(R.layout.fragment_provide_info, container, false);
            mEmailEditText = (EditText) root.findViewById(R.id.splash_edit_email);
            mEmailEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_NEXT) {
                        mCallback.onEmailInput();
                    }
                    return false;
                }
            });
        } else if (mCurrentPage == (SplashScreenActivity.ALL_PAGES - 3)) {
            root = inflater.inflate(R.layout.fragment_terms_of_use, container, false);
        } else if (mCurrentPage == (SplashScreenActivity.ALL_PAGES) - 2) {
            root = inflater.inflate(R.layout.fragment_provide_office_hours, container, false);
            mOfficeTimeRangeTv = (TextView) root.findViewById(R.id.content_timerage);
            final String currentOfficeHours = new OfficeHoursObject(getContext()).toString();
            mOfficeTimeRangeTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new MaterialDialog.Builder(getActivity())
                            .content("Please input your usual office hours (e.g. " + getString(R.string.pref_default_office_hours) + ").")
                            .inputType(InputType.TYPE_CLASS_DATETIME)
                            .input(getString(R.string.pref_default_office_hours), currentOfficeHours, new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(MaterialDialog dialog, CharSequence input) {
                                    if (OfficeHoursObject.validateAndSaveOfficeHoursString(getContext(), input.toString())) {
                                        mOfficeTimeRangeTv.setText(OfficeHoursObject.prettifyTimeRangeStringValue(input.toString()));
                                    }
                                }
                            }).show();
                }
            });

            SwitchCompat switchCompat = (SwitchCompat) root.findViewById(R.id.office_weekends_switch);
            switchCompat.setChecked(OfficeHoursObject.areInOfficeForWeekends(getContext()));

            switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d(TAG, "Weekends checked changed to: " + isChecked);
                    OfficeHoursObject.saveWeekendsDecision(getContext(), isChecked);
                }
            });
            mOfficeTimeRangeTv.setText(currentOfficeHours);

        } else {
            root = inflater.inflate(R.layout.fragment_splash_info, container, false);
            ButterKnife.bind(this, root);
            switch (mCurrentPage) {
                case 0:
                    setupWelcomeFragment();
                    break;
                case 1:
                    setupCollectFragment();
                    break;
                case 2:
                    setupNeedYouFragment();
                    break;
                case 3:
                    setupSafeFragment();
                    break;
                default:
                    Log.e(TAG, "Page not handled.");
            }
        }

        return root;
    }

    public boolean canGoNext() {
        if (mEmailFragment) {
            if (mEmailEditText.getText().toString().isEmpty()) {
                return true;
            }
            if (AppHelper.isValidEmail(mEmailEditText.getText().toString())) {
                mPrefs.edit().putString("profile_email_text", mEmailEditText.getText().toString()).apply();
                return true;
            }
            mEmailEditText.setError(getContext().getString(R.string.not_vaid_email));
            return false;
        }
        return true;
    }

    private void setupWelcomeFragment() {
        mIconView.setImageResource(R.drawable.ic_school_white_48dp);
        mTitleText.setText("Welcome!");
        mText1.setText("Thanks for installing TaskyApp!");
        mText2.setText("This app is part of a research project for a master thesis at Faculty of Computer and Information Science, University of Ljubljana.");
    }

    private void setupCollectFragment() {
        mIconView.setImageResource(R.drawable.ic_developer_board_white_48dp);
        mTitleText.setText("Collecting data...");
        mText1.setText("App will collect data from your phone's sensors periodically, trying to detect your daily tasks.");
        mText2.setText("We will sense nearby bluetooth and WiFi devices, accelerometer, gyroscope, location and ambient noise loudness.");
    }

    private void setupNeedYouFragment() {
        mIconView.setImageResource(R.drawable.ic_sentiment_very_satisfied_white_48dp);
        mTitleText.setText("...but we need you!");
        mText1.setText("We will kindly ask you to label detected tasks on scale from very easy to very hard.");
        mText2.setText("Very easy means that you are still able to interact with phone (play games for instance) " +
                "whereas very hard means you are very busy - you don't have time even to turn your phone screen on.");
    }

    private void setupSafeFragment() {
        mIconView.setImageResource(R.drawable.ic_vpn_lock_white_48dp);
        mTitleText.setText("Safety first!");
        mText1.setText("We need to send your data to server to learn from it...");
        mText2.setText("... but don't worry! Your data will be anonymised and stored safely on a faculty server.");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnSplashScreenFragmentActionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnSplashScreenFragmentActionListener");
        }
    }

    // Container Activity must implement this interface
    public interface OnSplashScreenFragmentActionListener {
        void onEmailInput();
    }
}
