package com.leanvpn.app;

import com.wireguard.android.backend.Tunnel;

public class WgTunnel implements Tunnel {
    @Override
    public String getName() {
        return "LeanVPN";
    }

    @Override
    public void onStateChange(State newState) {
        // состояние меняется
    }
}
