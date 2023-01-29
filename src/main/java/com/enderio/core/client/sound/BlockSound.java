package com.enderio.core.client.sound;

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.util.ResourceLocation;

public class BlockSound extends PositionedSound implements ITickableSound {

    private boolean donePlaying = false;

    public BlockSound(ResourceLocation p_i45103_1_) {
        super(p_i45103_1_);
    }

    public boolean isDonePlaying() {
        return this.donePlaying;
    }

    public BlockSound setDonePlaying(boolean donePlaying) {
        this.donePlaying = donePlaying;
        return this;
    }

    public BlockSound setVolume(float vol) {
        this.volume = vol;
        return this;
    }

    public BlockSound setPitch(float pitch) {
        this.field_147663_c = pitch;
        return this;
    }

    public BlockSound setLocation(float x, float y, float z) {
        this.xPosF = x;
        this.yPosF = y;
        this.zPosF = z;
        return this;
    }

    public BlockSound setDoRepeat(boolean bool) {
        this.repeat = bool;
        return this;
    }

    @Override
    public void update() {
        ;
    }
}
