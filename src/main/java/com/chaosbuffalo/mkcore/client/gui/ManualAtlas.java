package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKAbstractGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;



public class ManualAtlas {

    public class TextureRegion {
        public final String regionName;
        public final int u;
        public final int v;
        public final int width;
        public final int height;

        public TextureRegion(String regionName, int u, int v, int width, int height){
            this.regionName = regionName;
            this.u = u;
            this.v = v;
            this.width = width;
            this.height = height;
        }
    }

    public final ResourceLocation textureLoc;
    public final int width;
    public final int height;
    private final Map<String, TextureRegion> regions = new HashMap<>();

    public ManualAtlas(ResourceLocation textureLoc, int width, int height){
        this.textureLoc = textureLoc;
        this.width = width;
        this.height = height;
    }

    public TextureRegion addTextureRegion(String regionName, int u, int v, int width, int height){
        TextureRegion region = new TextureRegion(regionName, u, v, width, height);
        this.regions.put(regionName, region);
        return region;
    }

    public void bind(Minecraft minecraft){
        minecraft.getTextureManager().bindTexture(textureLoc);
    }

    @Nullable
    public TextureRegion getRegion(String regionName){
        return regions.get(regionName);
    }

    public void drawRegionAtPos(String regionName, int xPos, int yPos){
        TextureRegion region = regions.get(regionName);
        if (region == null){
            MKCore.LOGGER.info("Skip drawing region {} for manual atlas {}, region not found.", regionName, textureLoc);
            return;
        }
        MKAbstractGui.mkBlitUVSizeSame(xPos, yPos, region.u, region.v, region.width, region.height, width, height);
    }

    public void drawRegionAtPosPartialWidth(String regionName, int xPos, int yPos, int partialWidth){
        TextureRegion region = regions.get(regionName);
        if (region == null){
            MKCore.LOGGER.info("Skip drawing region {} for manual atlas {}, region not found.", regionName, textureLoc);
            return;
        }
        MKAbstractGui.mkBlitUVSizeSame(xPos, yPos, region.u, region.v, partialWidth, region.height, width, height);
    }

    public int getCenterXOffset(String regionName, String inRegion){
        TextureRegion main = getRegion(regionName);
        TextureRegion other = getRegion(inRegion);
        if (main == null || other == null){
            return 0;
        }
        return (other.width - main.width) / 2;
    }

    public int getCenterYOffset(String regionName, String inRegion){
        TextureRegion main = getRegion(regionName);
        TextureRegion other = getRegion(inRegion);
        if (main == null || other == null){
            return 0;
        }
        return (other.height - main.height) / 2;
    }
}
