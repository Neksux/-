package com.triggerbot;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TriggerBotMod implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("triggerbot");

    public static boolean enabled = false;
    public static final int CLICK_DELAY_MIN = 50;
    public static final int CLICK_DELAY_MAX = 120;
    public static final boolean PLAYERS_ONLY = true;
    public static final boolean IGNORE_TEAMMATES = true;

    private static KeyBinding toggleKey;
    private static long lastClickTime = 0;
    private static long nextClickDelay = 0;
    private static boolean wasAimingAtEntity = false;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.triggerbot.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.triggerbot"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                enabled = !enabled;
                assert client.player != null;
                client.player.sendMessage(
                        Text.literal("§6[TriggerBot] §r" + (enabled ? "§aВключён" : "§cВыключён")),
                        true
                );
            }

            if (!enabled) return;
            if (client.player == null || client.world == null) return;
            if (client.currentScreen != null) return;

            handleTrigger(client);
        });
    }

    private static void handleTrigger(MinecraftClient client) {
        HitResult crosshair = client.crosshairTarget;
        boolean aimingNow = false;

        if (crosshair instanceof EntityHitResult entityHit) {
            Entity target = entityHit.getEntity();

            if (isValidTarget(client, target)) {
                aimingNow = true;
                long now = System.currentTimeMillis();

                if (!wasAimingAtEntity) {
                    nextClickDelay = CLICK_DELAY_MIN +
                            (long)(Math.random() * (CLICK_DELAY_MAX - CLICK_DELAY_MIN));
                    lastClickTime = now;
                }

                if (now - lastClickTime >= nextClickDelay) {
                    simulateLeftClick(client);
                    lastClickTime = now;
                    nextClickDelay = CLICK_DELAY_MIN +
                            (long)(Math.random() * (CLICK_DELAY_MAX - CLICK_DELAY_MIN));
                }
            }
        }

        wasAimingAtEntity = aimingNow;
    }

    private static boolean isValidTarget(MinecraftClient client, Entity entity) {
        if (!(entity instanceof LivingEntity living)) return false;
        if (!living.isAlive()) return false;
        if (entity == client.player) return false;
        if (PLAYERS_ONLY && !(entity instanceof PlayerEntity)) return false;

        if (IGNORE_TEAMMATES && entity instanceof PlayerEntity player) {
            if (client.player != null && client.player.isTeammate(player)) {
                return false;
            }
        }

        return true;
    }

    public static void simulateLeftClick(MinecraftClient client) {
        if (client.interactionManager == null || client.player == null) return;
        client.interactionManager.attackEntity(
                client.player,
                client.crosshairTarget instanceof EntityHitResult ehr ? ehr.getEntity() : null
        );
        client.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
    }
              }
