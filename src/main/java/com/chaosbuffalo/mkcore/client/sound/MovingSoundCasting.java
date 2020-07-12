package com.chaosbuffalo.mkcore.client.sound;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.AbilityExecutor;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MovingSoundCasting extends TickableSound {
    private final LivingEntity caster;
    private final int castTime;

    public MovingSoundCasting(LivingEntity caster, SoundEvent event, SoundCategory category, int castTime) {
        super(event, category);
        this.caster = caster;
        this.repeat = true;
        this.repeatDelay = 0;
        this.castTime = castTime;
    }

    @Override
    public void tick() {
        if (!caster.isAlive()) {
            donePlaying = true;
            return;
        }


        donePlaying = MKCore.getEntityData((caster)).map(cap -> {
            AbilityExecutor executor = cap.getAbilityExecutor();
            if (!executor.isCasting()) {
                return true;
            }

            int currentCast = executor.getCastTicks();
            int lerpTime = (int) (castTime * .2f);
            int timeCasting = castTime - currentCast;
            int fadeOutPoint = castTime - lerpTime;
            if (timeCasting <= lerpTime) {
                volume = lerp(0.0f, 1.0f,
                        (float) timeCasting / (float) lerpTime);
            } else if (timeCasting >= fadeOutPoint) {
                volume = lerp(1.0f, 0.0f,
                        (float) (timeCasting - fadeOutPoint) / (float) lerpTime);
            }
            return false;
        }).orElse(true);

        if (donePlaying)
            return;

        x = (float) caster.getPosX();
        y = (float) caster.getPosY();
        z = (float) caster.getPosZ();
    }

    public static float lerp(float v0, float v1, float t) {
        return (1 - t) * v0 + t * v1;
    }
}
