package com.prodev.views.tabs.provider;

import android.content.Context;
import androidx.annotation.IdRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.prodev.views.tabs.SmartTabLayout;

public class SimpleTabProvider extends SmartTabLayout.TabProvider {
    private final int tabViewLayoutId;
    private final int tabViewTextViewId;

    public SimpleTabProvider(Context context, @IdRes int layoutResId, @IdRes int textViewId) {
        super(context);

        rebindOnUpdate = true;

        tabViewLayoutId = layoutResId;
        tabViewTextViewId = textViewId;
    }

    public SimpleTabProvider(ViewGroup parentView, @IdRes int layoutResId, @IdRes int textViewId) {
        super(parentView);

        rebindOnUpdate = true;

        tabViewLayoutId = layoutResId;
        tabViewTextViewId = textViewId;
    }

    @Override
    protected View createHolder(Integer position, ViewGroup parentView) {
        return inflateLayout(tabViewLayoutId, true, false);
    }

    @Override
    protected void bindHolder(Integer position, View contentView) {
        CharSequence title = null;
        try {
            title = getAdapter().getPageTitle(position);
        } catch (Exception e) {
        }

        if (tabViewTextViewId != View.NO_ID) {
            TextView textView = (TextView) contentView.findViewById(tabViewTextViewId);
            if (textView != null) textView.setText(title);
        }
    }
}
