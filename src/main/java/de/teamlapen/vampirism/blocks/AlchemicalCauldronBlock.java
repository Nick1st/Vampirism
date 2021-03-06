package de.teamlapen.vampirism.blocks;

import de.teamlapen.vampirism.core.ModSounds;
import de.teamlapen.vampirism.core.ModStats;
import de.teamlapen.vampirism.tileentity.AlchemicalCauldronTileEntity;
import de.teamlapen.vampirism.util.REFERENCE;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Random;


public class AlchemicalCauldronBlock extends AbstractFurnaceBlock {
    public static final String regName = "alchemical_cauldron";
    /**
     * 0: No liquid,
     * 1: Liquid,
     * 2: Boiling liquid
     */
    public static final IntegerProperty LIQUID = IntegerProperty.create("liquid", 0, 2);
    protected static final VoxelShape cauldronShape = makeShape();

    private static VoxelShape makeShape() {
        VoxelShape a = Block.makeCuboidShape(2, 0, 2, 14, 9, 14);
        VoxelShape b = Block.makeCuboidShape(1, 9, 1, 15, 13, 15);
        VoxelShape c = Block.makeCuboidShape(2, 13, 2, 14, 14, 14);
        return VoxelShapes.or(a, b, c);
    }

    public AlchemicalCauldronBlock() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(4f).notSolid());
        this.setDefaultState(this.stateContainer.getBaseState().with(LIQUID, 0).with(FACING, Direction.NORTH).with(LIT, false));
        this.setRegistryName(REFERENCE.MODID, regName);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return new AlchemicalCauldronTileEntity();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return cauldronShape;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos blockPos, BlockState blockState, LivingEntity entity, ItemStack stack) {
        super.onBlockPlacedBy(world, blockPos, blockState, entity, stack);
        TileEntity tile = world.getTileEntity(blockPos);
        if (entity instanceof PlayerEntity && tile instanceof AlchemicalCauldronTileEntity) {
            ((AlchemicalCauldronTileEntity) tile).setOwnerID((PlayerEntity) entity);
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(LIT, FACING, LIQUID);
    }

    @Override
    protected void interactWith(World world, BlockPos blockPos, PlayerEntity playerEntity) {
        TileEntity tile = world.getTileEntity(blockPos);
        if (tile instanceof AlchemicalCauldronTileEntity) {
            playerEntity.openContainer((INamedContainerProvider) tile);
            playerEntity.addStat(ModStats.interact_alchemical_cauldron);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random rng) {
        super.animateTick(state, world, pos, rng);
        if (state.get(LIQUID) == 2) {
            world.playSound(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, ModSounds.boiling, SoundCategory.BLOCKS, 0.05F, 1, false);
        }
    }
}
