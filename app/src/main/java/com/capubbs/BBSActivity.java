package com.capubbs;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.capubbs.lib.BaseActivity;
import com.capubbs.lib.Constants;
import com.capubbs.lib.MyFile;
import com.capubbs.lib.view.CustomViewPager;

import java.util.ArrayList;

public class BBSActivity extends BaseActivity {
    public CustomViewPager mViewPager;

    Handler handler = new Handler(msg -> {
        if (msg.what == Constants.MESSAGE_BBS_LOGIN) {
            Userinfo.finishLogin(this, (String) msg.obj);
        }
        return false;
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bbs_main);

        MyFile.setUseSDCard(true, this);

        mViewPager = (CustomViewPager) findViewById(R.id.bbspager);
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public Fragment getItem(int arg0) {
                if (arg0 == 1)
                    return Fragment.instantiate(BBSActivity.this, "com.capubbs.AllBoardsFragment");
                if (arg0 == 2)
                    return Fragment.instantiate(BBSActivity.this, "com.capubbs.SearchFragment");
                if (arg0 == 3)
                    return Fragment.instantiate(BBSActivity.this, "com.capubbs.SettingsFragment");
                return Fragment.instantiate(BBSActivity.this, "com.capubbs.TopFragment");
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) clickTop(null);
                else if (position == 1) clickAllBoards(null);
                else if (position == 2) clickSearch(null);
                else if (position == 3) clickMe(null);
            }
        });
        Board.load();
        Userinfo.load(this);
        SettingsFragment.checkUpdate(this);
        TopFragment.tops = new ArrayList<>();
        clickTop(null);
    }

    private void resetAllTab() {
        ((ImageView) findViewById(R.id.bbs_bottom_img_top)).getDrawable().clearColorFilter();
        ((ImageView) findViewById(R.id.bbs_bottom_img_allboards)).getDrawable().clearColorFilter();
        ((ImageView) findViewById(R.id.bbs_bottom_img_search)).getDrawable().clearColorFilter();
        ((ImageView) findViewById(R.id.bbs_bottom_img_settings)).getDrawable().clearColorFilter();
        ((TextView) findViewById(R.id.bbs_bottom_top)).setTextColor(Color.BLACK);
        ((TextView) findViewById(R.id.bbs_bottom_allboards)).setTextColor(Color.BLACK);
        ((TextView) findViewById(R.id.bbs_bottom_search)).setTextColor(Color.BLACK);
        ((TextView) findViewById(R.id.bbs_bottom_settings)).setTextColor(Color.BLACK);
    }

    public void clickTop(View view) {
        mViewPager.setCurrentItem(0);
        invalidateOptionsMenu();
        resetAllTab();
        ((ImageView) findViewById(R.id.bbs_bottom_img_top)).getDrawable().
                setColorFilter(Color.parseColor("#2d90dc"), PorterDuff.Mode.MULTIPLY);
        ((TextView) findViewById(R.id.bbs_bottom_top)).setTextColor(Color.parseColor("#2d90dc"));
        setTitle("最新帖子");
    }

    public void clickAllBoards(View view) {
        mViewPager.setCurrentItem(1);
        invalidateOptionsMenu();
        resetAllTab();
        ((ImageView) findViewById(R.id.bbs_bottom_img_allboards)).getDrawable().setColorFilter(Color.parseColor("#2d90dc"), PorterDuff.Mode.MULTIPLY);
        ((TextView) findViewById(R.id.bbs_bottom_allboards)).setTextColor(Color.parseColor("#2d90dc"));
        setTitle("板块列表");
    }

    public void clickSearch(View view) {
        mViewPager.setCurrentItem(2);
        invalidateOptionsMenu();
        resetAllTab();
        ((ImageView) findViewById(R.id.bbs_bottom_img_search)).getDrawable().setColorFilter(Color.parseColor("#2d90dc"), PorterDuff.Mode.MULTIPLY);
        ((TextView) findViewById(R.id.bbs_bottom_search)).setTextColor(Color.parseColor("#2d90dc"));
        setTitle("站内搜索");
    }

    public void clickMe(View view) {
        mViewPager.setCurrentItem(3);
        invalidateOptionsMenu();
        resetAllTab();
        ((ImageView) findViewById(R.id.bbs_bottom_img_settings)).getDrawable().setColorFilter(Color.parseColor("#2d90dc"), PorterDuff.Mode.MULTIPLY);
        ((TextView) findViewById(R.id.bbs_bottom_settings)).setTextColor(Color.parseColor("#2d90dc"));
        setTitle("我的");
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (mViewPager.getCurrentItem() ==0)
            menu.add(Menu.NONE, Constants.MENU_BBS_REFRESH, Constants.MENU_BBS_REFRESH, "")
                    .setIcon(R.drawable.ic_refresh_white_36dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == Constants.MENU_BBS_REFRESH) {
            TopFragment.showView(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void finishRequest(int type, String string) {
        if (type == Constants.REQUEST_BBS_GET_TOP)
            TopFragment.finishRequest(this, string);
        if (type == Constants.REQUEST_BBS_LOGIN)
            Userinfo.finishLogin(this, string);
        if (type == Constants.REQUEST_BBS_SEARCH)
            SearchFragment.finishSearch(this, string);
    }

}