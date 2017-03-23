package com.darsh.couponstracker.ui.view;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

/**
 * <p>Created by darshan on 21/3/17.
 *
 * <p>A special sub-class of {@link AppCompatAutoCompleteTextView} designed for use as a child of
 * {@link TextInputLayout}.
 *
 * <p>Using this class allows us to display a hint in the IME when in 'extract' mode.</p>
 */

public class TextInputAutoCompleteTextView extends AppCompatAutoCompleteTextView {
    /**
     * The hint that was initially set via xml. A reference to this is needed
     * because hint is changed to an empty string the parent containing this
     * view is in focus.
     */
    private String hint = "";

    public TextInputAutoCompleteTextView(Context context) {
        this(context, null);
    }

    public TextInputAutoCompleteTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.autoCompleteTextViewStyle);
    }

    public TextInputAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (getHint() != null) {
            hint = getHint().toString();
        }

        /*
        To invoke onFocusChangeListener's onFocusChange method
         */
        setFocusable(true);
        setFocusableInTouchMode(true);

        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    setHint("");
                } else {
                    setHint(hint);
                }
            }
        });
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        /*
        Taken from:
        https://android.googlesource.com/platform/frameworks/support.git/+/master/design/src/android/support/design/widget/TextInputEditText.java
        to extend TextInputEditText functionality to AppCompatAutoCompleteTextView.
         */

        final InputConnection ic = super.onCreateInputConnection(outAttrs);
        if (ic != null && outAttrs.hintText == null) {
            // If we don't have a hint and our parent is a TextInputLayout, use it's hint for the
            // EditorInfo. This allows us to display a hint in 'extract mode'.
            ViewParent parent = getParent();
            while (parent instanceof View) {
                if (parent instanceof TextInputLayout) {
                    outAttrs.hintText = ((TextInputLayout) parent).getHint();
                    break;
                }
                parent = parent.getParent();
            }
        }
        return ic;
    }
}
