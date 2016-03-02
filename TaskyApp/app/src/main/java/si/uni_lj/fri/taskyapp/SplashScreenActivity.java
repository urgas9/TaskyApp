package si.uni_lj.fri.taskyapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.viewpagerindicator.CirclePageIndicator;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import si.uni_lj.fri.taskyapp.splash.SplashScreenFragment;

public class SplashScreenActivity extends AppCompatActivity implements SplashScreenFragment.OnSplashScreenFragmentActionListener {

    public static final int ALL_PAGES = 3;
    @Bind(R.id.pager)
    ViewPager mPager;
    @Bind(R.id.splash_circle_indicator)
    CirclePageIndicator mPageIndicator;
    @Bind(R.id.btn_splash_next)
    Button mNextButton;
    @Bind(R.id.btn_splash_back)
    Button mBackButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        ButterKnife.bind(this);
        mPager.setAdapter(new SplashViewPagerAdapter(getSupportFragmentManager()));
        mPageIndicator.setViewPager(mPager);
        mPageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == (mPager.getChildCount() - 1)){
                    mNextButton.setText(R.string.finish);
                }
                else{
                    mNextButton.setText(R.string.next);
                }
                if(position == 0){
                    mBackButton.setVisibility(View.INVISIBLE);
                }
                else{
                    mBackButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private SplashScreenFragment getCurrentlyVisibleFragment(){
        Fragment pageFragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + mPager.getCurrentItem());
        // based on the current position you can then cast the page to the correct
        // class and call the method:
        if (pageFragment != null && pageFragment instanceof SplashScreenFragment) {
            return (SplashScreenFragment)pageFragment;
        }
        return null;
    }
    @OnClick(R.id.btn_splash_next)
    public void onNextClick(View v){
        if(mPager.getCurrentItem() < (mPager.getChildCount()-1)) {
            mPager.setCurrentItem(mPager.getCurrentItem() + 1);
        }
        else{
            SplashScreenFragment curFragment = getCurrentlyVisibleFragment();
            if(curFragment != null && curFragment.canGoNext()) {
                Intent startAppIntent = new Intent(this, MainActivity.class);
                startActivity(startAppIntent);
                finish();
            }
        }

    }

    @OnClick(R.id.btn_splash_back)
    public void onBackClicked(View v){
        if(mPager.getCurrentItem() > 0){
            mPager.setCurrentItem(mPager.getCurrentItem()-1);
        }
    }

    @Override
    public void onEmailInput() {
        onNextClick(null);
    }

    class SplashViewPagerAdapter extends FragmentPagerAdapter {

        public SplashViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            return SplashScreenFragment.newInstance(i);
        }

        @Override
        public int getCount() {
            return ALL_PAGES;
        }
    }
}
