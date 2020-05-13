package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.blocks.XpTableBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, MKCore.MOD_ID);

    public static final RegistryObject<Block> XP_TABLE = BLOCKS.register("xp_table", () ->
            new XpTableBlock(Block.Properties.create(Material.ANVIL).hardnessAndResistance(5.0f)));

}
