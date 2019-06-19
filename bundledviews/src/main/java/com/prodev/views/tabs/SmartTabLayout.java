/**
 * Copyright (C) 2015 ogaclejapan
 * Copyright (C) 2013 The Android Open Source Project
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.prodev.views.tabs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prodev.views.R;
import com.prodev.views.tabs.provider.SimpleTabProvider;
import com.prodev.views.tools.holder.ViewsHolder;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * To be used with ViewPager to provide a tab indicator component which give constant feedback as
 * to
 * the user's scroll progress.
 * <p>
 * To use the component, simply add it to your view hierarchy. Then in your
 * {@link android.app.Activity} or {@link android.support.v4.app.Fragment} call
 * {@link #setViewPager(ViewPager)} providing it the ViewPager this
 * layout
 * is being used for.
 * <p>
 * The colors can be customized in two ways. The first and simplest is to provide an array of
 * colors
 * via {@link #setSelectedIndicatorColors(int...)} and {@link #setDividerColors(int...)}. The
 * alternative is via the {@link TabColorizer} interface which provides you complete control over
 * which color is used for any individual position.
 * <p>
 * The views used as tabs can be customized by calling {@link #setCustomTabView(int, int)},
 * providing the layout ID of your custom layout.
 * <p>
 * Forked from Google Samples &gt; SlidingTabsBasic &gt;
 * <a href="https://developer.android.com/samples/SlidingTabsBasic/src/com.example.android.common/view/SlidingTabLayout.html">SlidingTabLayout</a>
 */
public class SmartTabLayout extends HorizontalScrollView {
    private static final boolean DEFAULT_DISTRIBUTE_EVENLY = false;
    private static final int TAB_VIEW_PADDING_DIPS = 16;
    private static final boolean TAB_VIEW_TEXT_ALL_CAPS = true;
    private static final int TAB_VIEW_TEXT_SIZE_SP = 12;
    private static final int TAB_VIEW_TEXT_COLOR = 0xFC000000;
    private static final int TAB_VIEW_TEXT_MIN_WIDTH = 0;
    private static final boolean TAB_CLICKABLE = true;

    protected final SmartTabStrip tabStrip;
    private int tabViewBackgroundResId;
    private boolean tabViewTextAllCaps;
    private ColorStateList tabViewTextColors;
    private float tabViewTextSize;
    private int tabViewTextHorizontalPadding;
    private int tabViewTextMinWidth;
    private ViewPager viewPager;
    private ViewPager.OnPageChangeListener viewPagerPageChangeListener;
    private OnScrollChangeListener onScrollChangeListener;
    private TabProvider tabProvider;
    private InternalTabClickListener internalTabClickListener;
    private OnTabClickListener onTabClickListener;
    private boolean distributeEvenly;

    private InternalChangeListener internalChangeListener;

    private float startTabPos = -1;
    private float markedTabPos = -1;
    private float targetTabPos = -1;
    private int scrollPos = -1;

    public SmartTabLayout(Context context) {
        this(context, null);
    }

    public SmartTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmartTabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Disable the Scroll Bar
        setHorizontalScrollBarEnabled(false);

        final DisplayMetrics dm = getResources().getDisplayMetrics();
        final float density = dm.density;

        int tabBackgroundResId = NO_ID;
        boolean textAllCaps = TAB_VIEW_TEXT_ALL_CAPS;
        ColorStateList textColors;
        float textSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, TAB_VIEW_TEXT_SIZE_SP, dm);
        int textHorizontalPadding = (int) (TAB_VIEW_PADDING_DIPS * density);
        int textMinWidth = (int) (TAB_VIEW_TEXT_MIN_WIDTH * density);
        boolean distributeEvenly = DEFAULT_DISTRIBUTE_EVENLY;
        int customTabLayoutId = NO_ID;
        int customTabTextViewId = NO_ID;
        boolean clickable = TAB_CLICKABLE;

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.stl_SmartTabLayout, defStyle, 0);
        tabBackgroundResId = a.getResourceId(
                R.styleable.stl_SmartTabLayout_stl_defaultTabBackground, tabBackgroundResId);
        textAllCaps = a.getBoolean(
                R.styleable.stl_SmartTabLayout_stl_defaultTabTextAllCaps, textAllCaps);
        textColors = a.getColorStateList(
                R.styleable.stl_SmartTabLayout_stl_defaultTabTextColor);
        textSize = a.getDimension(
                R.styleable.stl_SmartTabLayout_stl_defaultTabTextSize, textSize);
        textHorizontalPadding = a.getDimensionPixelSize(
                R.styleable.stl_SmartTabLayout_stl_defaultTabTextHorizontalPadding, textHorizontalPadding);
        textMinWidth = a.getDimensionPixelSize(
                R.styleable.stl_SmartTabLayout_stl_defaultTabTextMinWidth, textMinWidth);
        customTabLayoutId = a.getResourceId(
                R.styleable.stl_SmartTabLayout_stl_customTabTextLayoutId, customTabLayoutId);
        customTabTextViewId = a.getResourceId(
                R.styleable.stl_SmartTabLayout_stl_customTabTextViewId, customTabTextViewId);
        distributeEvenly = a.getBoolean(
                R.styleable.stl_SmartTabLayout_stl_distributeEvenly, distributeEvenly);
        clickable = a.getBoolean(
                R.styleable.stl_SmartTabLayout_stl_clickable, clickable);
        a.recycle();

        this.tabViewBackgroundResId = tabBackgroundResId;
        this.tabViewTextAllCaps = textAllCaps;
        this.tabViewTextColors = (textColors != null)
                ? textColors
                : ColorStateList.valueOf(TAB_VIEW_TEXT_COLOR);
        this.tabViewTextSize = textSize;
        this.tabViewTextHorizontalPadding = textHorizontalPadding;
        this.tabViewTextMinWidth = textMinWidth;
        this.internalTabClickListener = clickable ? new InternalTabClickListener() : null;
        this.distributeEvenly = distributeEvenly;

        if (customTabLayoutId != NO_ID) {
            setCustomTabView(customTabLayoutId, customTabTextViewId);
        }

        this.tabStrip = new SmartTabStrip(context, attrs);

        if (distributeEvenly && tabStrip.isIndicatorAlwaysInCenter()) {
            throw new UnsupportedOperationException(
                    "'distributeEvenly' and 'indicatorAlwaysInCenter' both use does not support");
        }

        // Make sure that the Tab Strips fills this View
        setFillViewport(!tabStrip.isIndicatorAlwaysInCenter());

        addView(tabStrip, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        //Set default tab provider
        setToDefaultTabView();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (onScrollChangeListener != null) {
            onScrollChangeListener.onScrollChanged(l, oldl);
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        if (tabStrip.isIndicatorAlwaysInCenter() && tabStrip.getChildCount() > 0) {
            View firstTab = tabStrip.getChildAt(0);
            View lastTab = tabStrip.getChildAt(tabStrip.getChildCount() - 1);

            int start = (width - Utils.getMeasuredWidth(firstTab)) / 2 - Utils.getMarginStart(firstTab);
            int end = (width - Utils.getMeasuredWidth(lastTab)) / 2 - Utils.getMarginEnd(lastTab);
            tabStrip.setMinimumWidth(tabStrip.getMeasuredWidth());

            ViewCompat.setPaddingRelative(this, start, getPaddingTop(), end, getPaddingBottom());
            setClipToPadding(false);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // Ensure first scroll
        if (changed && viewPager != null) {
            int tabPos = viewPager.getCurrentItem();
            scrollToTab(tabPos, 0);
        }
    }

    /**
     * Set the behavior of the Indicator scrolling feedback.
     *
     * @param interpolator {@link com.prodev.views.tabs.SmartTabIndicationInterpolator}
     */
    public void setIndicationInterpolator(SmartTabIndicationInterpolator interpolator) {
        tabStrip.setIndicationInterpolator(interpolator);
    }

    /**
     * Set the custom {@link TabColorizer} to be used.
     * <p>
     * If you only require simple customisation then you can use
     * {@link #setSelectedIndicatorColors(int...)} and {@link #setDividerColors(int...)} to achieve
     * similar effects.
     */
    public void setCustomTabColorizer(TabColorizer tabColorizer) {
        tabStrip.setCustomTabColorizer(tabColorizer);
    }

    /**
     * Set the color used for styling the tab text. This will need to be called prior to calling
     * {@link #setViewPager(ViewPager)} otherwise it will not get set
     *
     * @param color to use for tab text
     */
    public void setDefaultTabTextColor(int color) {
        tabViewTextColors = ColorStateList.valueOf(color);
    }

    /**
     * Sets the colors used for styling the tab text. This will need to be called prior to calling
     * {@link #setViewPager(ViewPager)} otherwise it will not get set
     *
     * @param colors ColorStateList to use for tab text
     */
    public void setDefaultTabTextColor(ColorStateList colors) {
        tabViewTextColors = colors;
    }

    /**
     * Set the same weight for tab
     */
    public void setDistributeEvenly(boolean distributeEvenly) {
        this.distributeEvenly = distributeEvenly;
    }

    /**
     * Sets the colors to be used for indicating the selected tab. These colors are treated as a
     * circular array. Providing one color will mean that all tabs are indicated with the same color.
     */
    public void setSelectedIndicatorColors(int... colors) {
        tabStrip.setSelectedIndicatorColors(colors);
    }

    /**
     * Sets the colors to be used for tab dividers. These colors are treated as a circular array.
     * Providing one color will mean that all tabs are indicated with the same color.
     */
    public void setDividerColors(int... colors) {
        tabStrip.setDividerColors(colors);
    }

    /**
     * Set the {@link ViewPager.OnPageChangeListener}. When using {@link SmartTabLayout} you are
     * required to set any {@link ViewPager.OnPageChangeListener} through this method. This is so
     * that the layout can update it's scroll position correctly.
     *
     * @see ViewPager#setOnPageChangeListener(ViewPager.OnPageChangeListener)
     */
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        viewPagerPageChangeListener = listener;
    }

    /**
     * Set {@link OnScrollChangeListener} for obtaining values of scrolling.
     *
     * @param listener the {@link OnScrollChangeListener} to set
     */
    public void setOnScrollChangeListener(OnScrollChangeListener listener) {
        onScrollChangeListener = listener;
    }

    /**
     * Set {@link OnTabClickListener} for obtaining click event.
     *
     * @param listener the {@link OnTabClickListener} to set
     */
    public void setOnTabClickListener(OnTabClickListener listener) {
        onTabClickListener = listener;
    }

    /**
     * Set the custom layout to be inflated for the tab views.
     *
     * @param layoutResId Layout id to be inflated
     * @param textViewId  id of the {@link android.widget.TextView} in the inflated view
     */
    public void setCustomTabView(@IdRes int layoutResId, @IdRes int textViewId) {
        setCustomTabView(new SimpleTabProvider(getContext(), layoutResId, textViewId));
    }

    /**
     * Sets the tab layout back to default
     */
    public void setToDefaultTabView() {
        if (tabStrip != null)
            setCustomTabView(new DefaultTabProvider(tabStrip));
        else
            setCustomTabView(new DefaultTabProvider(getContext()));
    }

    /**
     * Set the custom layout to be inflated for the tab views.
     *
     * @param provider {@link TabProvider}
     */
    public synchronized void setCustomTabView(TabProvider provider) {
        setCustomTabView(provider, true);
    }

    public synchronized void setCustomTabView(TabProvider provider, boolean update) {
        tabProvider = provider;

        if (update) {
            if (tabProvider != null) {
                updateTabStrip();
            } else {
                try {
                    if (tabStrip != null)
                        tabStrip.removeAllViews();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * Sets the associated view pager. Note that the assumption here is that the pager content
     * (number of tabs and tab titles) does not change after this call has been made.
     */
    public synchronized void setViewPager(ViewPager viewPager) {
        if (this.viewPager != viewPager) {
            try {
                if (tabStrip != null)
                    tabStrip.removeAllViews();
            } catch (Exception e) {
            }

            if (internalChangeListener == null)
                internalChangeListener = new InternalChangeListener();

            if (this.viewPager != null) {
                this.viewPager.removeOnPageChangeListener(internalChangeListener);
                this.viewPager.removeOnAdapterChangeListener(internalChangeListener);
            }

            this.viewPager = viewPager;

            if (this.viewPager != null) {
                this.viewPager.addOnPageChangeListener(internalChangeListener);
                this.viewPager.addOnAdapterChangeListener(internalChangeListener);
            }
        }

        if (this.viewPager != null && this.viewPager.getAdapter() != null) {
            updateTabStrip();

            scrollToTab(viewPager.getCurrentItem(), 0);
        }
    }

    /**
     * Returns the view at the specified position in the tabs.
     *
     * @param position the position at which to get the view from
     * @return the view at the specified position or null if the position does not exist within the
     * tabs
     */
    public synchronized View getTabAt(int position) {
        return tabStrip.getChildAt(position);
    }

    /**
     * Create a default view to be used for tabs. This is called if a custom tab view is not set via
     * {@link #setCustomTabView(int, int)}.
     */

    public synchronized void updateTabStrip() {
        final PagerAdapter adapter = viewPager != null ? viewPager.getAdapter() : null;

        if (tabStrip == null) {
            return;
        }
        if (tabProvider == null) {
            try {
                tabStrip.removeAllViews();
            } catch (Exception e) {
            }
        }

        tabProvider.setParentView(tabStrip);
        tabProvider.setData(this, false);

        int tabCount = adapter != null ? adapter.getCount() : 0;
        int viewCount = tabStrip.getChildCount();

        if (tabCount != viewCount) {
            ArrayList<Integer> tabPositionList = new ArrayList<>();
            for (int pos = 0; pos < tabCount; pos++) tabPositionList.add(pos);

            tabProvider.setKeys(tabPositionList, true, false);

            try {
                tabStrip.removeAllViews();
                for (int pos = 0; pos < tabCount; pos++) {
                    final View tabView = tabProvider.getContentView(pos);
                    if (tabView == null) continue;

                    if (distributeEvenly) {
                        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tabView.getLayoutParams();
                        lp.width = 0;
                        lp.weight = 1;
                    }

                    if (internalTabClickListener != null) {
                        tabView.setOnClickListener(internalTabClickListener);
                    }

                    tabStrip.addView(tabView);

                    try {
                        tabView.setSelected(pos == viewPager.getCurrentItem());
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {
            }
        }

        tabProvider.setData(this, true);
    }

    public void scrollToTab(int tabIndex, float positionOffset) {
        if (positionOffset < 0) {
            int changeBy = (int) Math.floor(positionOffset);
            tabIndex += changeBy;
            positionOffset -= changeBy;
        }

        float tabPos = (float) tabIndex + positionOffset;
        scrollToTab(tabPos);
    }

    public void scrollToTab(float tabPos) {
        //Abort viewpager scroll
        try {
            if (viewPager != null)
                viewPager.setCurrentItem(viewPager.getCurrentItem(), false);
        } catch (Exception e) {
        }

        //Scroll
        startScroll(tabPos, tabPos);
        scroll(tabPos);
        stopScroll();
    }

    public float getStartTabPos() {
        return startTabPos;
    }

    public void setStartTabPos(float startTabPos) {
        this.startTabPos = startTabPos;
    }

    public float getMarkedTabPos() {
        return markedTabPos;
    }

    public void setMarkedTabPos(float markedTabPos) {
        this.markedTabPos = markedTabPos;
    }

    public float getTargetTabPos() {
        return targetTabPos;
    }

    public void markTargetTabIndex() {
        markedTabPos = targetTabPos;
    }

    public void startScroll(int tabIndex, float positionOffset, float markedTabIndex) {
        if (positionOffset < 0) {
            int changeBy = (int) Math.floor(positionOffset);
            tabIndex += changeBy;
            positionOffset -= changeBy;
        }

        float startTabPos = (float) tabIndex + positionOffset;
        startScroll(startTabPos, markedTabIndex);
    }

    public void startScroll(float startTabPos, float markedTabIndex) {
        this.startTabPos = startTabPos;
        this.markedTabPos = markedTabIndex;
        this.targetTabPos = markedTabIndex;
        this.scrollPos = getScrollX();
    }

    public void stopScroll() {
        this.startTabPos = -1;
        this.markedTabPos = -1;
        this.targetTabPos = -1;
        this.scrollPos = -1;
    }

    public void scroll(int tabIndex, float positionOffset) {
        if (positionOffset < 0) {
            int changeBy = (int) Math.floor(positionOffset);
            tabIndex += changeBy;
            positionOffset -= changeBy;
        }
        if (tabIndex < 0) return;

        final float tabPos = (float) tabIndex + positionOffset;
        scroll(tabPos);
    }

    public void scroll(final float tabPos) {
        // Calculate movement
        final float tabMovement = tabPos - this.startTabPos;

        final int nextTabIndex = (int) (tabMovement > 0 ? Math.floor(tabPos) + 1d : Math.floor(tabPos));
        final float targetTabPos = this.markedTabPos >= 0 ? this.markedTabPos : nextTabIndex;
        final float tabAmountScroll = targetTabPos - this.startTabPos;
        final float tabAmountDist = targetTabPos - tabPos;

        this.targetTabPos = targetTabPos;

        final float movement = 1f - (tabAmountScroll != 0f ? tabAmountDist / tabAmountScroll : 0f);

        // Calculate scroll
        if (tabStrip == null) return;
        final int tabIndex = (int) Math.floor(tabPos);
        final int tabStripChildCount = tabStrip.getChildCount();
        if (tabIndex < 0 || tabStripChildCount <= 0 || tabIndex >= tabStripChildCount) return;

        final boolean isLayoutRtl = Utils.isLayoutRtl(this);

        View targetTab = tabStrip.getChildAt((int) Math.floor(targetTabPos));

        int targetScrollPos = 0;
        if (tabStrip.isIndicatorAlwaysInCenter()) {
            if (!isLayoutRtl) {
                targetScrollPos = Utils.getStart(targetTab);
            } else {
                targetScrollPos = Utils.getEnd(targetTab);
            }

            View firstTab = tabStrip.getChildAt(0);
            int first = Utils.getWidth(firstTab) + Utils.getMarginEnd(firstTab);
            int target = Utils.getWidth(targetTab) + Utils.getMarginEnd(targetTab);
            targetScrollPos += (target - first) / 2;
        } else {
            if (!isLayoutRtl) {
                targetScrollPos = Utils.getStart(targetTab);
            } else {
                int boundaryPos = getWidth() - Utils.getPaddingHorizontally(this);
                targetScrollPos = Utils.getStart(targetTab) - boundaryPos;
            }
        }

        int scrollDist = targetScrollPos - scrollPos;
        int currentScrollPos = scrollPos + (int) ((float) scrollDist * movement);

        scrollTo(currentScrollPos, 0);
    }

    /**
     * Allows complete control over the colors drawn in the tab layout. Set with
     * {@link #setCustomTabColorizer(TabColorizer)}.
     */
    public interface TabColorizer {
        /**
         * @return return the color of the indicator used when {@code position} is selected.
         */
        int getIndicatorColor(int position);

        /**
         * @return return the color of the divider drawn to the right of {@code position}.
         */
        int getDividerColor(int position);

    }

    /**
     * Interface definition for a callback to be invoked when the scroll position of a view changes.
     */
    public interface OnScrollChangeListener {

        /**
         * Called when the scroll position of a view changes.
         *
         * @param scrollX    Current horizontal scroll origin.
         * @param oldScrollX Previous horizontal scroll origin.
         */
        void onScrollChanged(int scrollX, int oldScrollX);
    }

    /**
     * Interface definition for a callback to be invoked when a tab is clicked.
     */
    public interface OnTabClickListener {

        /**
         * Called when a tab is clicked.
         *
         * @param position tab's position
         */
        void onTabClicked(int position);
    }

    /**
     * Create the custom tabs in the tab layout. Set with
     * {@link #setCustomTabView(com.prodev.views.tabs.SmartTabLayout.TabProvider)}
     */
    public static abstract class TabProvider extends ViewsHolder<Integer> {
        protected boolean rebindOnUpdate;

        private SmartTabLayout tabLayout;

        private ViewPager pager;
        private PagerAdapter adapter;

        public TabProvider(Context context) {
            super(context);

            rebindOnUpdate = true;
        }

        public TabProvider(ViewGroup parentView) {
            super(parentView);

            rebindOnUpdate = true;
        }

        private synchronized final void setData(SmartTabLayout tabLayout, boolean update) {
            this.tabLayout = tabLayout;

            this.pager = null;
            this.adapter = null;

            if (this.tabLayout != null) {
                this.pager = this.tabLayout.viewPager;

                if (this.pager != null)
                    this.adapter = this.pager.getAdapter();
            }

            if (update) {
                Iterator<Integer> keyIterator = keyIterator();
                if (keyIterator != null) {
                    while (keyIterator.hasNext()) {
                        Integer key = keyIterator.next();

                        try {
                            update(key);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                createAll(rebindOnUpdate);
            }
        }

        public final boolean rebindOnUpdate() {
            return rebindOnUpdate;
        }

        public final SmartTabLayout getTabLayout() {
            return tabLayout;
        }

        public final ViewPager getPager() {
            return pager;
        }

        public final PagerAdapter getAdapter() {
            return adapter;
        }

        public void update(int position) {
        }
    }

    private class InternalChangeListener implements ViewPager.OnPageChangeListener, ViewPager.OnAdapterChangeListener {
        private float lastTabPos = -1;
        private int scrollState;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            lastTabPos = (float) position + positionOffset;

            int tabStripChildCount = tabStrip.getChildCount();
            if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount))
                return;

            tabStrip.onViewPagerPageChanged(position, positionOffset);

            scroll(position, positionOffset);

            if (viewPagerPageChangeListener != null) {
                viewPagerPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE)
                stopScroll();
            if (scrollState == ViewPager.SCROLL_STATE_IDLE && state != ViewPager.SCROLL_STATE_IDLE)
                startScroll(lastTabPos >= 0 ? lastTabPos : (float) (viewPager != null ? viewPager.getCurrentItem() : 0), markedTabPos);
            if (scrollState != ViewPager.SCROLL_STATE_SETTLING && state == ViewPager.SCROLL_STATE_SETTLING)
                markTargetTabIndex();

            scrollState = state;

            if (viewPagerPageChangeListener != null) {
                viewPagerPageChangeListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (scrollState == ViewPager.SCROLL_STATE_IDLE) {
                tabStrip.onViewPagerPageChanged(position, 0f);
            }

            for (int i = 0, size = tabStrip.getChildCount(); i < size; i++) {
                tabStrip.getChildAt(i).setSelected(position == i);
            }

            if (viewPagerPageChangeListener != null) {
                viewPagerPageChangeListener.onPageSelected(position);
            }
        }

        @Override
        public void onAdapterChanged(@NonNull ViewPager viewPager, @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {
            if (viewPager != null) {
                lastTabPos = viewPager.getCurrentItem();
                scrollToTab(lastTabPos);
            }

            if (oldAdapter != newAdapter) {
                try {
                    if (tabStrip != null)
                        tabStrip.removeAllViews();
                } catch (Exception e) {
                }
            }

            setViewPager(viewPager);
        }
    }

    private class InternalTabClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < tabStrip.getChildCount(); i++) {
                if (v == tabStrip.getChildAt(i)) {
                    if (onTabClickListener != null) {
                        onTabClickListener.onTabClicked(i);
                    }
                    startScroll(viewPager.getCurrentItem(), 0, i);
                    viewPager.setCurrentItem(i);
                    return;
                }
            }
        }
    }

    /**
     * Default tab layout provider
     */
    public class DefaultTabProvider extends TabProvider {
        public DefaultTabProvider(Context context) {
            super(context);

            rebindOnUpdate = true;
        }

        public DefaultTabProvider(ViewGroup parentView) {
            super(parentView);

            rebindOnUpdate = true;
        }

        @Override
        protected View createHolder(Integer position, ViewGroup parentView) {
            return createTabView();
        }

        @SuppressLint("ResourceType")
        private TextView createTabView() {
            TextView textView = new TextView(getContext());
            textView.setId(1);
            textView.setGravity(Gravity.CENTER);

            textView.setTextColor(tabViewTextColors);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabViewTextSize);
            textView.setTypeface(Typeface.DEFAULT_BOLD);

            textView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            ));

            if (tabViewBackgroundResId != NO_ID) {
                textView.setBackgroundResource(tabViewBackgroundResId);
            } else {
                // If we're running on Honeycomb or newer, then we can use the Theme's
                // selectableItemBackground to ensure that the View has a pressed state
                TypedValue outValue = new TypedValue();
                getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground,
                        outValue, true);
                textView.setBackgroundResource(outValue.resourceId);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                // If we're running on ICS or newer, enable all-caps to match the Action Bar tab style
                textView.setAllCaps(tabViewTextAllCaps);
            }

            textView.setPadding(
                    tabViewTextHorizontalPadding, 0,
                    tabViewTextHorizontalPadding, 0);

            if (tabViewTextMinWidth > 0) {
                textView.setMinWidth(tabViewTextMinWidth);
            }

            return textView;
        }

        @Override
        protected void bindHolder(Integer position, View contentView) {
            CharSequence title = null;
            try {
                title = getAdapter().getPageTitle(position);
            } catch (Exception e) {
            }

            @SuppressLint("ResourceType")
            TextView textView = (TextView) contentView.findViewById(1);
            if (textView != null) textView.setText(title);
        }
    }
}
