package com.bridger.momentum;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

public class BridgeTapMod implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "bridgetap";

    @Override
    public void onInitialize() {
        BridgeConfig.load();
        System.out.println("[BridgeTap] Initialized BridgeTap mod!");
    }

    @Override
    public void onInitializeClient() {
        BridgeConfig.load();
        System.out.println("[BridgeTap] Initialized BridgeTap Client!");
    }
}
