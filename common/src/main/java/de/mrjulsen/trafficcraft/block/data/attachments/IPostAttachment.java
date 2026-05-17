package de.mrjulsen.trafficcraft.block.data.attachments;

import de.mrjulsen.mcdragonlib.client.ber.BERGraphics;
import de.mrjulsen.mcdragonlib.client.model.ModelContext;
import de.mrjulsen.mcdragonlib.client.model.mesh.Mesh;
import de.mrjulsen.mcdragonlib.data.INBTSerializable;
import de.mrjulsen.trafficcraft.registry.builtin.PostAttachmentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface IPostAttachment<T extends IPostAttachment<T>> extends INBTSerializable {

    Direction getDirection();
    float getRotation();
    PostAttachmentRegistry.PostAttachmentRegistryObject<T> getRegistryType();

    Mesh getModel(BlockState state, RandomSource random, ModelContext context);
    VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos);

    default InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    default void renderAdditional(BERGraphics<?> graphics, float partialTick) {}

    default List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of();
    }

    default void tick(Level level, BlockPos pos) {}

    default void onLoad(Level level, BlockPos pos) {}

    default void onUnload(Level level, BlockPos pos) {}

    default void onCreated(Level level, BlockPos pos) {}

    default void onRemoved(Level level, BlockPos pos) {}
}
