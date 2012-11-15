
package com.kth.baasio.baassample.ui;

import static com.kth.common.utils.LogUtils.makeLogTag;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.kth.baasio.baassample.BaseActivity;
import com.kth.baasio.baassample.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

public class BaasMainActivity extends BaseActivity implements OnPageChangeListener, TabListener {
    private static final String TAG = makeLogTag(BaasMainActivity.class);

    private ViewPager mViewPager;

    private AuthFragment mAuthFragment;

    private UserFragment mUserFragment;

    private EntityFragment mEntityFragment;

    private PushFragment mPushFragment;

    private FileFragment mFileFragment;

    private OptionsItemSelectedListener mListener;

    public interface OptionsItemSelectedListener {
        public boolean onParentOptionsItemSelected(MenuItem item);
    }

    public void setOnOptionsItemSelectedListener(OptionsItemSelectedListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baasio_sample);

        if (getSupportActionBar() != null)
            getSupportActionBar().setHomeButtonEnabled(false);

        mViewPager = (ViewPager)findViewById(R.id.pager);
        if (mViewPager != null) {
            // Phone setup
            mViewPager.setAdapter(new SessionPagerAdapter(getSupportFragmentManager()));
            mViewPager.setOnPageChangeListener(this);
            mViewPager.setPageMarginDrawable(R.drawable.grey_border_inset_lr);
            mViewPager.setPageMargin(getResources()
                    .getDimensionPixelSize(R.dimen.page_margin_width));

            final ActionBar actionBar = getSupportActionBar();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            actionBar.addTab(actionBar.newTab().setText(R.string.auth_title).setTabListener(this));
            actionBar.addTab(actionBar.newTab().setText(R.string.user_title).setTabListener(this));
            actionBar
                    .addTab(actionBar.newTab().setText(R.string.entity_title).setTabListener(this));
            actionBar.addTab(actionBar.newTab().setText(R.string.push_title).setTabListener(this));
            actionBar.addTab(actionBar.newTab().setText(R.string.file_title).setTabListener(this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (mViewPager.getCurrentItem() == 4) {
            getSupportMenuInflater().inflate(R.menu.fragment_file, menu);
        } else if (mViewPager.getCurrentItem() == 2) {
            getSupportMenuInflater().inflate(R.menu.fragment_entity, menu);
        } else if (mViewPager.getCurrentItem() == 1) {
            getSupportMenuInflater().inflate(R.menu.fragment_user, menu);
        } else {
            getSupportMenuInflater().inflate(R.menu.activity_baas_sample, menu);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                break;

            default: {
                if (mListener != null) {
                    mListener.onParentOptionsItemSelected(item);
                }
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private class SessionPagerAdapter extends FragmentPagerAdapter {
        public SessionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    mAuthFragment = new AuthFragment();
                    setOnOptionsItemSelectedListener(mAuthFragment);
                    return mAuthFragment;

                case 1:
                    mUserFragment = new UserFragment();
                    setOnOptionsItemSelectedListener(mUserFragment);
                    return mUserFragment;

                case 2:
                    mEntityFragment = new EntityFragment();
                    setOnOptionsItemSelectedListener(mEntityFragment);
                    return mEntityFragment;

                case 3:
                    mPushFragment = new PushFragment();
                    setOnOptionsItemSelectedListener(mPushFragment);
                    return mPushFragment;

                case 4:
                    mFileFragment = new FileFragment();
                    setOnOptionsItemSelectedListener(mFileFragment);
                    return mFileFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 5;
        }
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.view.ViewPager.OnPageChangeListener#
     * onPageScrollStateChanged(int)
     */
    @Override
    public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see
     * android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrolled
     * (int, float, int)
     */
    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see
     * android.support.v4.view.ViewPager.OnPageChangeListener#onPageSelected
     * (int)
     */
    @Override
    public void onPageSelected(int position) {
        getSupportActionBar().setSelectedNavigationItem(position);
        invalidateOptionsMenu();

        switch (position) {
            case 0:
                if (mAuthFragment != null) {
                    setOnOptionsItemSelectedListener(mAuthFragment);
                }
                break;

            case 1:
                if (mUserFragment != null) {
                    setOnOptionsItemSelectedListener(mUserFragment);
                }
                break;

            case 2:
                if (mEntityFragment != null) {
                    setOnOptionsItemSelectedListener(mEntityFragment);
                }
                break;

            case 3:
                if (mPushFragment != null) {
                    setOnOptionsItemSelectedListener(mPushFragment);
                }
                break;

            case 4:
                if (mFileFragment != null) {
                    setOnOptionsItemSelectedListener(mFileFragment);
                }
                break;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.actionbarsherlock.app.ActionBar.TabListener#onTabSelected(com.
     * actionbarsherlock.app.ActionBar.Tab,
     * android.support.v4.app.FragmentTransaction)
     */
    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    /*
     * (non-Javadoc)
     * @see com.actionbarsherlock.app.ActionBar.TabListener#onTabUnselected(com.
     * actionbarsherlock.app.ActionBar.Tab,
     * android.support.v4.app.FragmentTransaction)
     */
    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.actionbarsherlock.app.ActionBar.TabListener#onTabReselected(com.
     * actionbarsherlock.app.ActionBar.Tab,
     * android.support.v4.app.FragmentTransaction)
     */
    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub

    }
}
