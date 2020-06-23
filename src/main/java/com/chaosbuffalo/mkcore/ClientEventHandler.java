package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.client.gui.CharacterScreen;
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
    private static KeyBinding[] activeAbilityBinds;
    private static KeyBinding[] ultimateAbilityBinds;

    private static int currentGCDTicks;

    public static void initKeybindings() {
        playerMenuBind = new KeyBinding("key.hud.playermenu", GLFW.GLFW_KEY_J, "key.mkcore.category");
        ClientRegistry.registerKeyBinding(playerMenuBind);

        activeAbilityBinds = new KeyBinding[GameConstants.MAX_ACTIVES];
        for (int i = 0; i < GameConstants.MAX_ACTIVES; i++) {
            String bindName = String.format("key.hud.active_ability%d", i + 1);
            int key = GLFW.GLFW_KEY_1 + i;
            KeyBinding bind = new KeyBinding(bindName, KeyConflictContext.IN_GAME, KeyModifier.ALT,
                    InputMappings.getInputByCode(key, 0), "key.mkcore.abilitybar");

            ClientRegistry.registerKeyBinding(bind);
            activeAbilityBinds[i] = bind;
        }

        ultimateAbilityBinds = new KeyBinding[GameConstants.MAX_ULTIMATES];
        for (int i = 0; i < GameConstants.MAX_ULTIMATES; i++) {
            String bindName = String.format("key.hud.ultimate_ability%d", i + 1);
            int key = GLFW.GLFW_KEY_6 + i;
            KeyBinding bind = new KeyBinding(bindName, KeyConflictContext.IN_GAME, KeyModifier.ALT,
                    InputMappings.getInputByCode(key, 0), "key.mkcore.abilitybar");

            ClientRegistry.registerKeyBinding(bind);
            ultimateAbilityBinds[i] = bind;
        }
    }

    public static float getGlobalCooldown() {
        return (float) currentGCDTicks / GameConstants.TICKS_PER_SECOND;
    }

    public static float getTotalGlobalCooldown() {
        return (float) GameConstants.GLOBAL_COOLDOWN_TICKS / GameConstants.TICKS_PER_SECOND;
    }

    static boolean isOnGlobalCooldown() {
        return currentGCDTicks > 0;
    }

    static void startGlobalCooldown() {
        currentGCDTicks = GameConstants.GLOBAL_COOLDOWN_TICKS;
    }

    @SubscribeEvent
    public static void onKeyEvent(InputEvent.KeyInputEvent event) {
        handleInputEvent();
    }

    @SubscribeEvent
    public static void onMouseEvent(InputEvent.MouseInputEvent event) {
        handleInputEvent();
    }


    static void handleAbilityBarPressed(PlayerEntity player, MKAbility.AbilityType type, int slot) {
        if (isOnGlobalCooldown())
            return;

        MKCore.getPlayer(player).ifPresent(pData -> {
            ResourceLocation abilityId = pData.getKnowledge().getAbilityInSlot(type, slot);
            if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY))
                return;

            MKAbility ability = MKCoreRegistry.getAbility(abilityId);
            if (ability == null || !ability.meetsRequirements(pData))
                return;

            MKCore.LOGGER.info("sending execute ability {} {}", type, slot);
            PacketHandler.sendMessageToServer(new ExecuteActiveAbilityPacket(type, slot));
            startGlobalCooldown();
        });
    }

    public static void handleInputEvent() {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null)
            return;

        while (playerMenuBind.isPressed()) {
            MKCore.LOGGER.info("open player menu");
            Minecraft.getInstance().displayGuiScreen(new CharacterScreen());

        }

        for (int i = 0; i < activeAbilityBinds.length; i++) {
            KeyBinding bind = activeAbilityBinds[i];
            while (bind.isPressed()) {
                handleAbilityBarPressed(player, MKAbility.AbilityType.Active, i);
            }
        }

        for (int i = 0; i < ultimateAbilityBinds.length; i++) {
            KeyBinding bind = ultimateAbilityBinds[i];
            while (bind.isPressed()) {
                handleAbilityBarPressed(player, MKAbility.AbilityType.Ultimate, i);
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
