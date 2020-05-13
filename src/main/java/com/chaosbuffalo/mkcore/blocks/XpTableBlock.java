package com.chaosbuffalo.mkcore.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class XpTableBlock extends Block {

    public XpTableBlock(final Properties properties) {
        super(properties);
//        this.setTranslationKey(translationKey);
//        this.setCreativeTab(MKUltra.MKULTRA_TAB);
//        this.setHardness(hardness);
//        this.setResistance(resistance);
//        setRegistryName(MKUltra.MODID, "xp_table");
    }

    @Override
    public ActionResultType onBlockActivated(final BlockState state, final World worldIn, final BlockPos pos, final PlayerEntity player, final Hand handIn, final BlockRayTraceResult hit) {
        if (!worldIn.isRemote) {
//        IPlayerData pData = MKUPlayerData.get(player);
//        if (pData == null || MKURegistry.getClass(pData.getClassId()) == null)
//            return true;
//
//        player.openGui(MKUltra.INSTANCE, ModGuiHandler.XP_TABLE_SCREEN, player.world,
//                (int) player.posX, (int) player.posY, (int) player.posZ);
        }
        return ActionResultType.SUCCESS;
    }
}
