package com.prodev.views.test;

import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.prodev.views.tabs.SmartTabLayout;
import com.prodev.views.test.fragments.TestFragment;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private SmartTabLayout tabLayout;
    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Toolbar
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        tabLayout = (SmartTabLayout) findViewById(R.id.main_tab_layout);

        //Viewpager
        pager = (ViewPager) findViewById(R.id.main_pager);
        //tabLayout.setViewPager(pager);

        //Adapter
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("Page1", TestFragment.class)
                .add("Page2 Special", TestFragment.class)
                .add("Page3", TestFragment.class)
                .add("Page4", TestFragment.class)
                .add("Page5 Special", TestFragment.class)
                .add("Page6", TestFragment.class)
                .add("Page7 Extra long", TestFragment.class)
                .add("Page8", TestFragment.class)
                .create());

        pager.setAdapter(adapter);

        tabLayout.setViewPager(pager);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //tabLayout.scrollToTab(5, 3);
            }
        }, 3000);
    }
}
