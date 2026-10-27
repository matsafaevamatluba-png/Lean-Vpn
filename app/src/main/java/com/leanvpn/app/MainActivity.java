package com.leanvpn.app;

import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int VPN_REQUEST = 100;
    private TextView status;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(3, 25, 20));
        getWindow().setNavigationBarColor(Color.rgb(3, 25, 20));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(28, 48, 28, 28);
        root.setBackgroundColor(Color.rgb(3, 25, 20));

        TextView logo = new TextView(this);
        logo.setText("⬡  LeanVPN");
        logo.setTextColor(Color.rgb(62, 230, 145));
        logo.setTextSize(34);
        logo.setTypeface(null, Typeface.BOLD);
        logo.setGravity(Gravity.CENTER);
        root.addView(logo, matchWrap());

        TextView subtitle = new TextView(this);
        subtitle.setText("Просто. Безопасно. Под твоим контролем.");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sp = matchWrap();
        sp.topMargin = 12;
        root.addView(subtitle, sp);

        status = new TextView(this);
        status.setText("VPN не подключён");
        status.setTextColor(Color.WHITE);
        status.setTextSize(19);
        status.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams stp = matchWrap();
        stp.topMargin = 56;
        stp.bottomMargin = 24;
        root.addView(status, stp);

        Button connect = new Button(this);
        connect.setText("Разрешить VPN");
        connect.setTextColor(Color.WHITE);
        connect.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(0, 170, 100)));
        root.addView(connect, matchWrap());
        connect.setOnClickListener(v -> {
            Intent intent = VpnService.prepare(this);
            if (intent != null) startActivityForResult(intent, VPN_REQUEST);
            else onActivityResult(VPN_REQUEST, RESULT_OK, null);
        });

        TextView note = new TextView(this);
        note.setText("Это стартовая версия интерфейса. Для настоящего VPN нужен настроенный сервер и VPN-туннель. Эта сборка пока не передаёт интернет-трафик через VPN.");
        note.setTextColor(Color.rgb(190, 210, 200));
        note.setTextSize(14);
        note.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams np = matchWrap();
        np.topMargin = 32;
        root.addView(note, np);

        TextView footer = new TextView(this);
        footer.setText("LeanVPN • версия 0.1.0");
        footer.setTextColor(Color.GRAY);
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, 0, 1);
        root.addView(footer, fp);

        setContentView(root);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST && resultCode == RESULT_OK) {
            status.setText("Разрешение получено — сервер ещё не настроен");
            Toast.makeText(this, "Разрешение Android получено. VPN-сервер пока не подключён.", Toast.LENGTH_LONG).show();
        } else if (requestCode == VPN_REQUEST) {
            status.setText("VPN не подключён");
        }
    }
}
