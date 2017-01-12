package com.walli_app.walli;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by dado on 20/04/2016.
 */
public class RobotoItalicTextView extends TextView {
    protected String font;

    public RobotoItalicTextView(Context context) {
        super(context);
        if(!isInEditMode()){
            putTheFuckingFontInTheView(context);
        }
    }

    public RobotoItalicTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(!isInEditMode()){
            putTheFuckingFontInTheView(context);
            parseAttributes(context);
        }
    }

    public RobotoItalicTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if(!isInEditMode()){
            putTheFuckingFontInTheView(context);
            parseAttributes(context);
        }
    }

    protected void putTheFuckingFontInTheView(Context context){
        setFont(context.getString(R.string.roboto));
    }

    protected void setFont(String font){
        this.font = font;
    }

    protected void parseAttributes(Context context) {
        Typeface tf = obtaintTypeface(context);
        setTypeface(tf);
    }

    protected Typeface obtaintTypeface(Context context) throws IllegalArgumentException {
        return createTypeface(context);
    }

    protected Typeface createTypeface(Context context) throws IllegalArgumentException {
        return Typeface.createFromAsset(context.getAssets(),font);
    }
}
