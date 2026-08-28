package com.rrmotor.nota;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(30, 30, 30, 30);

        TextView judul = new TextView(this);
        judul.setText("🏍️ RR MOTOR NOTA");
        judul.setTextSize(28);
        judul.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        judul.setGravity(Gravity.CENTER);

        TextView keterangan = new TextView(this);
        keterangan.setText(
                "\n\nAplikasi RR MOTOR NOTA\n\n" +
                "Berhasil dibuka.\n\n" +
                "Versi pemeriksaan."
        );
        keterangan.setTextSize(18);
        keterangan.setGravity(Gravity.CENTER);

        layout.addView(judul);
        layout.addView(keterangan);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(layout);

        setContentView(scroll);
    }
}
