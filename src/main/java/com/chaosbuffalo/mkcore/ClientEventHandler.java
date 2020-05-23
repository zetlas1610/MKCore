package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.abilities.PlayerAbility;
import com.chaosbuffalo.mkcore.network.ExecuteActiveAbilityPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber
public class ClientEventHandler {

    private static KeyBinding playerMenuBind;
    private static KeyBinding[] abilityBinds;

    private static int currentGCDTicks;

    public static void initKeybinds() {
        playerMenuBind = new KeyBinding("key.hud.playermenu", GLFW.GLFW_KEY_J, "key.mkcore.category");
        ClientRegistry.registerKeyBinding(playerMenuBind);

        abilityBinds = new KeyBinding[GameConstants.ACTION_BAR_SIZE];
        for (int i = 0; i < GameConstants.ACTION_BAR_SIZE; i++) {
            String bindName = String.format("key.hud.ability%d", i + 1);
            int key = GLFW.GLFW_KEY_1 + i;
            KeyBinding bind = new KeyBinding(bindName, KeyConflictContext.IN_GAME, KeyModifier.ALT,
                    InputMappings.getInputByCode(key, 0), "key.mkcore.abilitybar");

            ClientRegistry.registerKeyBinding(bind);
            abilityBinds[i] = bind;
        }
    }

    public static float getGlobalCooldown() {
        return (float) currentGCDTicks / GameConstants.TICKS_PER_SECOND;
    }

    public static float getTotalGlobalCooldown() {
        return (float) GameConstants.GLOBAL_COOLDOWN_TICKS / GameConstants.TICKS_PER_SECOND;
    }

    @SubscribeEvent
    public static void onKeyEvent(InputEvent.KeyInputEvent event) {
//        MKCore.LOGGER.info("key {}", event.getKey());
        handleInputEvent();
    }

    @SubscribeEvent
    public static void onMouseEvent(InputEvent.MouseInputEvent event) {
        handleInputEvent();
    }

    static void handleAbilityBarPressed(PlayerEntity player, int slot) {
        if (currentGCDTicks == 0) {
            MKCore.getPlayer(player).ifPresent(pData -> {

                MKCore.LOGGER.info("sending execute ability {}", slot);

                    ResourceLocation abilityId = pData.getAbilityInSlot(slot);
                    PlayerAbility ability = MKCoreRegistry.getAbility(abilityId);
//                    if (ability == null)
//                        continue;
//
//                    if (ability.meetsRequirements(pData)) {
//                        PacketHandler.sendMessageToServer(new ExecuteActivePacket(i));
//                        currentGCDTicks = GameConstants.GLOBAL_COOLDOWN_TICKS;
//                        break;
//                    }

                PacketHandler.sendMessageToServer(new ExecuteActiveAbilityPacket(slot));
                currentGCDTicks = GameConstants.GLOBAL_COOLDOWN_TICKS;
            });
        }
    }

    public static void handleInputEvent() {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null)
            return;

        while (playerMenuBind.isPressed()) {
            MKCore.LOGGER.info("open player menu");
        }

        for (int i = 0; i < abilityBinds.length; i++) {
            KeyBinding bind = abilityBinds[i];
            while (bind.isPressed()) {
                handleAbilityBarPressed(player, i);
            }
        }
    }

    @SubscribeEvent
    public static void onTickEvent(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (currentGCDTicks > 0) {
                currentGCDTicks--;
            }
        }
    }
}
