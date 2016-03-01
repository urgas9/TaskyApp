package si.uni_lj.fri.taskyapp.splash;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import si.uni_lj.fri.taskyapp.R;
import si.uni_lj.fri.taskyapp.SplashScreenActivity;

public class SplashScreenFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_CURRENT_PAGE = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private int mCurrentPage;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = null;
        if(mCurrentPage == (SplashScreenActivity.ALL_PAGES - 1)) {
            root = inflater.inflate(R.layout.fragment_provide_info, container, false);
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

    private void setupSecondFragment(View root){
        ImageView iconIv = (ImageView)root.findViewById(R.id.icon_splash);
        iconIv.setImageResource(R.drawable.ic_lock_white_48dp);
    }

}
