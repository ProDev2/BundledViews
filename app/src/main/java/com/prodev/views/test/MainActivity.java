package com.prodev.views.test;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import com.prodev.views.tabs.SmartTabLayout;
import com.prodev.views.test.fragments.TestFragment;
import com.simplelib.pager.SimpleFragmentPagerAdapter;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private SmartTabLayout tabLayout;

    private ViewPager pager;
    private SimpleFragmentPagerAdapter pagerAdapter;

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
        pagerAdapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);

        tabLayout.setViewPager(pager);

        //Events
        tabLayout.setOnTabClickListener(new SmartTabLayout.OnTabClickListener() {
            @Override
            public boolean onTabClicked(int position) {
                return true;
            }

            @Override
            public boolean onTabLongClicked(int position) {
                if (position >= 0 && position < pagerAdapter.getCount())
                    pagerAdapter.remove(position);

                tabLayout.updateTabStrip();
                tabLayout.scrollNeeded();
                return true;
            }
        });

        final Runnable createTap = new Runnable() {
            @Override
            public void run() {
                String title = Integer.toString(pagerAdapter.getCount() + 1);
                title += " Tab";
                if (Math.random() > 0.5d) title += " space";

                TestFragment fragment = new TestFragment();
                fragment.setText(title);

                pagerAdapter.add(fragment, title);

                pager.setCurrentItem(pagerAdapter.getPageCount() - 1, false);

                tabLayout.updateTabStrip();
                tabLayout.scrollNeeded();
            }
        };
        createTap.run();
        createTap.run();

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTap.run();
            }
        });
    }
}
