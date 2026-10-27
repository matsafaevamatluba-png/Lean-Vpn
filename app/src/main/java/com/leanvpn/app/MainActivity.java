package com.leanvpn.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.wireguard.android.backend.Backend;
import com.wireguard.android.backend.GoBackend;
import com.wireguard.android.backend.Tunnel;
import com.wireguard.config.Config;

import java.io.BufferedReader;
import java.io.StringReader;

public class MainActivity extends Activity {

    private static final int VPN_REQUEST = 100;
    private TextView status;
    private EditText configInput;
    private Button connectBtn;
    private Backend backend;
    private WgTunnel tunnel;
    private boolean isConnected = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(3, 25, 20));
        getWindow().setNavigationBarColor(Color.rgb(3, 25, 20));

        backend = new GoBackend(this);
        tunnel = new WgTunnel();

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 48, 28, 40);
        root.setBackgroundColor(Color.rgb(3, 25, 20));
        scroll.addView(root);

        TextView logo = new TextView(this);
        logo.setText("⬡  LeanVPN");
        logo.setTextColor(Color.rgb(62, 230, 145));
        logo.setTextSize(32);
        logo.setTypeface(null, Typeface.BOLD);
        logo.setGravity(Gravity.CENTER);
        root.addView(logo);

        TextView subtitle = new TextView(this);
        subtitle.setText("WireGuard клиент");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sp = matchWrap();
        sp.topMargin = 8;
        sp.bottomMargin = 32;
        root.addView(subtitle, sp);

        status = new TextView(this);
        status.setText("Отключён");
        status.setTextColor(Color.WHITE);
        status.setTextSize(18);
        status.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams stp = matchWrap();
        stp.bottomMargin = 20;
        root.addView(status, stp);

        TextView label = new TextView(this);
        label.setText("Вставь конфиг WireGuard (.conf):");
        label.setTextColor(Color.rgb(180, 200, 190));
        label.setTextSize(13);
        root.addView(label);

        configInput = new EditText(this);
        configInput.setHint("[Interface]\nPrivateKey = ...\nAddress = 10.0.0.2/32\nDNS = 1.1.1.1\n\n[Peer]\nPublicKey = ...\nEndpoint = ip:51820\nAllowedIPs = 0.0.0.0/0");
        configInput.setHintTextColor(Color.GRAY);
        configInput.setTextColor(Color.WHITE);
        configInput.setBackgroundColor(Color.rgb(15, 40, 35));
        configInput.setPadding(20, 16, 20, 16);
        configInput.setMinLines(8);
        configInput.setGravity(Gravity.TOP);
        LinearLayout.LayoutParams cip = matchWrap();
        cip.topMargin = 8;
        cip.bottomMargin = 24;
        root.addView(configInput, cip);

        connectBtn = new Button(this);
        connectBtn.setText("Подключить");
        connectBtn.setTextColor(Color.WHITE);
        connectBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(0, 170, 100)));
        root.addView(connectBtn, matchWrap());

        connectBtn.setOnClickListener(v -> {
            if (isConnected) {
                disconnect();
            } else {
                requestVpnPermission();
            }
        });

        TextView note = new TextView(this);
        note.setText("Нужен готовый WireGuard конфиг от твоего сервера.\nБез сервера подключение не заработает.");
        note.setTextColor(Color.rgb(160, 180, 170));
        note.setTextSize(12);
        note.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams np = matchWrap();
        np.topMargin = 28;
        root.addView(note, np);

        setContentView(scroll);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    private void requestVpnPermission() {
        Intent intent = VpnService.prepare(this);
        if (intent != null) {
            startActivityForResult(intent, VPN_REQUEST);
        } else {
            connect();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST && resultCode == RESULT_OK) {
            connect();
        } else if (requestCode == VPN_REQUEST) {
            Toast.makeText(this, "Разрешение VPN не выдано", Toast.LENGTH_SHORT).show();
        }
    }

    private void connect() {
        String confText = configInput.getText().toString().trim();
        if (confText.isEmpty()) {
            Toast.makeText(this, "Вставь конфиг WireGuard", Toast.LENGTH_SHORT).show();
            return;
        }

        status.setText("Подключение...");
        connectBtn.setEnabled(false);

        new Thread(() -> {
            try {
                Config config = Config.parse(new BufferedReader(new StringReader(confText)));
                backend.setState(tunnel, Tunnel.State.UP, config);

                handler.post(() -> {
                    isConnected = true;
                    status.setText("Подключён");
                    status.setTextColor(Color.rgb(62, 230, 145));
                    connectBtn.setText("Отключить");
                    connectBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(180, 60, 60)));
                    connectBtn.setEnabled(true);
                    Toast.makeText(this, "VPN включён", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                handler.post(() -> {
                    status.setText("Ошибка");
                    status.setTextColor(Color.rgb(255, 100, 100));
                    connectBtn.setEnabled(true);
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void disconnect() {
        status.setText("Отключение...");
        connectBtn.setEnabled(false);

        new Thread(() -> {
            try {
                backend.setState(tunnel, Tunnel.State.DOWN, null);
            } catch (Exception ignored) {}

            handler.post(() -> {
                isConnected = false;
                status.setText("Отключён");
                status.setTextColor(Color.WHITE);
                connectBtn.setText("Подключить");
                connectBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(0, 170, 100)));
                connectBtn.setEnabled(true);
                Toast.makeText(this, "VPN выключен", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
}
