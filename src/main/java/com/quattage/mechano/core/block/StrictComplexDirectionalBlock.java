package com.quattage.mechano.core.block;

import com.quattage.mechano.core.placement.StrictComplexDirection;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class StrictComplexDirectionalBlock extends Block implements IWrenchable {

    public static final EnumProperty<StrictComplexDirection> ORIENTATION = EnumProperty.create("orientation", StrictComplexDirection.class); //accomodates for up and down PER CARDINAL, ex. UP_NORTH, or DOWN_EAST
    
    public StrictComplexDirectionalBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(ORIENTATION, StrictComplexDirection.UP_WEST));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ORIENTATION);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level world = context.getLevel();

        Axis intendedRotation = context.getClickedFace().getAxis();
        StrictComplexDirection strictCD;
        if(intendedRotation == state.getValue(ORIENTATION).getLocalUp().getAxis())
            strictCD = StrictComplexDirection.cycleLocalForward(state.getValue(ORIENTATION));
        else
            strictCD = StrictComplexDirection.cycle(state.getValue(ORIENTATION));
        BlockState rotated = state.setValue(ORIENTATION, strictCD);

        if (!rotated.canSurvive(world, context.getClickedPos()))
			return InteractionResult.PASS;
        
        KineticBlockEntity.switchToBlockState(world, context.getClickedPos(), updateAfterWrenched(rotated, context));

        if (world.getBlockState(context.getClickedPos()) != state)
			playRotateSound(world, context.getClickedPos());

		return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction orientation = context.getClickedFace();
        Direction followingDir = ComplexDirectionalBlock.getTriQuadrant(context, orientation, true);

        if(orientation == followingDir) followingDir = context.getHorizontalDirection();
        if(orientation.getAxis() == followingDir.getAxis()) followingDir = followingDir.getClockWise();
        if(context.getPlayer().isCrouching()) orientation = orientation.getOpposite();

        return this.defaultBlockState().setValue(ORIENTATION, StrictComplexDirection.combine(orientation, followingDir));
    }
}
