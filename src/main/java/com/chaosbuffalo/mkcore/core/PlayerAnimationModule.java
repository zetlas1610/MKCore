package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.MKAbility;

public class PlayerAnimationModule {
    private final MKPlayerData playerData;
    private int castAnimTimer;
    private PlayerVisualCastState playerVisualCastState;
    private MKAbility castingAbility;
    public enum PlayerVisualCastState {
        NONE,
        CASTING,
        RELEASE,
    }

    public PlayerAnimationModule(MKPlayerData playerData){
        this.playerData = playerData;
        playerVisualCastState = PlayerVisualCastState.NONE;
        castAnimTimer = 0;
        castingAbility = null;
    }

    protected MKPlayerData getPlayerData() {
        return playerData;
    }

    public MKAbility getCastingAbility() {
        return castingAbility;
    }

    public PlayerVisualCastState getPlayerVisualCastState() {
        return playerVisualCastState;
    }

    public int getCastAnimTimer() {
        return castAnimTimer;
    }

    protected void updateEntityCastState(){
        if (castAnimTimer > 0){
            castAnimTimer--;
            if (castAnimTimer == 0){
                castingAbility = null;
                playerVisualCastState = PlayerVisualCastState.NONE;
            }
        }
    }

    public void tick(){
        updateEntityCastState();
    }

    public void startCast(MKAbility ability){
        playerVisualCastState = PlayerVisualCastState.CASTING;
        castingAbility = ability;
    }

    public void endCast(MKAbility ability){
        castingAbility = ability;
        playerVisualCastState = PlayerVisualCastState.RELEASE;
        castAnimTimer = 15;
    }
}
