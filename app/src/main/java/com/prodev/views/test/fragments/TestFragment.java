package com.prodev.views.test.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.prodev.views.test.R;

public class TestFragment extends Fragment {
    private String text;

    private TextView textView;

    public TestFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.test_fragment, container, false);
        create(contentView);
        return contentView;
    }

    private void create(View contentView) {
        if (contentView == null) return;

        textView = contentView.findViewById(R.id.test_fragment_text);
        textView.setText(text);
    }

    public void setText(String text) {
        this.text = text;

        if (textView != null)
            textView.setText(text);
    }
}
