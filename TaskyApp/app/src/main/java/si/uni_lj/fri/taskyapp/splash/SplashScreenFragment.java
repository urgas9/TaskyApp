package si.uni_lj.fri.taskyapp.splash;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import si.uni_lj.fri.taskyapp.R;
import si.uni_lj.fri.taskyapp.SplashScreenActivity;
import si.uni_lj.fri.taskyapp.global.AppHelper;

public class SplashScreenFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_CURRENT_PAGE = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "SplashScreenFragment";

    // TODO: Rename and change types of parameters
    private int mCurrentPage;
    private boolean mEmailFragment;
    private OnSplashScreenFragmentActionListener mCallback;

    private EditText mEmailEditText;
    SharedPreferences mPrefs;

    public SplashScreenFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment ProvideInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SplashScreenFragment newInstance(int param1) {
        SplashScreenFragment fragment = new SplashScreenFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CURRENT_PAGE, param1);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root;
        if(mCurrentPage == (SplashScreenActivity.ALL_PAGES - 1)) {
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
        }
        else{
            root = inflater.inflate(R.layout.fragment_splash_info, container, false);
            if(mCurrentPage == 0){
                setupFirstFragment(root);
            }
            else{
                setupSecondFragment(root);
            }
        }

        return root;
    }

    private void setupFirstFragment(View root){
        ImageView iconIv = (ImageView)root.findViewById(R.id.icon_splash);
        iconIv.setImageResource(R.drawable.ic_school_white_48dp);

    }

    public boolean canGoNext(){
        if(mEmailFragment){
            if(AppHelper.isValidEmail(mEmailEditText.getText().toString())){
                mPrefs.edit().putString("profile_email_text", mEmailEditText.getText().toString()).apply();
                return true;
            }
            mEmailEditText.setError(getContext().getString(R.string.not_vaid_email));
            return false;
        }
        return true;
    }

    private void setupSecondFragment(View root){
        ImageView iconIv = (ImageView)root.findViewById(R.id.icon_splash);
        iconIv.setImageResource(R.drawable.ic_lock_white_48dp);
    }

    // Container Activity must implement this interface
    public interface OnSplashScreenFragmentActionListener {
        void onEmailInput();
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
}
