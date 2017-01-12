package com.walli_app.walli;

import android.app.DatePickerDialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by dado on 06/07/2016.
 */
//listener per mostrare un DatePickerDialog al click e gestire la scelta dell'utente
public class DateOnClickListener implements View.OnClickListener {
    private TextView view;
    private Calendar c;
    private MainActivity activity;
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private DateOnClickListener me = this;

    public DateOnClickListener(TextView view, Calendar c,MainActivity activity) {
        this.view = view;
        this.c = c;
        this.activity = activity;
        String text = format.format(this.c.getTime());
        this.view.setText(text);
    }

    public String toFormattedString(){
        return format.format(c.getTime());
    }

    @Override
    public void onClick(View v) {
        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                c.set(year,monthOfYear,dayOfMonth);
                String date = toFormattedString();
                me.view.setText(date);
                //aggiorno grafico
                activity.refresh_charts(false);
            }
        };
        DatePickerDialog d = new DatePickerDialog(activity,listener,c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
        d.show();
    }
}
