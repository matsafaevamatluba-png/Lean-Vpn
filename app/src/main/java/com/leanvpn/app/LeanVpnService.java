package com.leanvpn.app;

import android.net.VpnService;

/**
 * Starter only. A real implementation must establish a secure tunnel to a VPN
 * server (for example, using a vetted WireGuard implementation and valid config).
 * Do not advertise this starter as a working VPN.
 */
public class LeanVpnService extends VpnService {
    @Override public void onRevoke() {
        super.onRevoke();
        stopSelf();
    }
}
