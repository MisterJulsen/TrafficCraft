package de.mrjulsen.trafficcraft.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;

import de.mrjulsen.mcdragonlib.common.Location;
import de.mrjulsen.mcdragonlib.utils.StatusResult;
import de.mrjulsen.mcdragonlib.utils.Utils;
import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.data.RoadType;
import de.mrjulsen.trafficcraft.client.ClientWrapper;
import de.mrjulsen.trafficcraft.config.ModCommonConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RoadConstructionTool extends Item {

    public static final String NBT_LOCATION1 = "Location1";
    public static final String NBT_LOCATION2 = "Location2";
    public static final String NBT_ROAD_WIDTH = "RoadWidth";    
    public static final String NBT_REPLACE_BLOCKS = "ReplaceBlocks";
    public static final String NBT_ROAD_TYPE = "RoadType";

    public static final boolean DEFAULT_REPLACE_BLOCKS = true;
    public static final byte DEFAULT_ROAD_WIDTH = 7;
    public static final RoadType DEFAULT_ROAD_TYPE = RoadType.ASPHALT;

    private static final int ERROR_TOO_FAR = 1;
    private static final int ERROR_SLOPE_TOO_STEEP = 2;

    public static final int BUILD_DELAY_TICKS = 4;

    private static byte clientTicks;
    private static final byte FAST_GRAPHICS_CLIENT_TICK_DELAY = 8;
    private static final byte FANCY_GRAPHICS_CLIENT_TICK_DELAY = 4;

    
    private final float attackDamage;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public RoadConstructionTool(Tiers tier, Properties properties) {
        super(properties.stacksTo(1).durability(tier.getUses() * 6));
        float attackDamageModifier = 0.5f;
        this.attackDamage = tier.getAttackDamageBonus() + attackDamageModifier;
        Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", (double)this.attackDamage, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -3.0D, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) { 
        Level level = pContext.getLevel();
        BlockPos clickedPos = pContext.getClickedPos();
        Vec3 clickedVec = pContext.getClickLocation();
        Player player = pContext.getPlayer();
        
        if (!player.isShiftKeyDown()) {            
            if (!level.isClientSide) {
                CompoundTag compound = pContext.getItemInHand().getOrCreateTag();

                Location location = new Location(clickedPos.getX(), clickedVec.y, clickedPos.getZ(), level.dimension().location().toString());
                
                if (compound.contains(NBT_LOCATION1)) {
                    if (isLineValid(Location.fromNbt(compound.getCompound(NBT_LOCATION1)).getLocationVec3(), location.getLocationVec3()).result()) {
                        compound.put(NBT_LOCATION2, location.toNbt());
                    }
                } else {
                    compound.put(NBT_LOCATION1, location.toNbt());
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.useOn(pContext);
    }

    @Override
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return true;
    }

    public float getAttackDamage() {
        return attackDamage;
    }

    @Override
    @SuppressWarnings("deprecation")
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pSlot) {
        return pSlot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(pSlot);
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        CompoundTag tag = pStack.getTag();
        return (tag != null && (tag.contains(NBT_LOCATION1) || tag.contains(NBT_LOCATION2))) || super.isFoil(pStack);
    }

    public static void reset(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.remove(RoadConstructionTool.NBT_LOCATION1);
        nbt.remove(RoadConstructionTool.NBT_LOCATION2);
        nbt.putBoolean(RoadConstructionTool.NBT_REPLACE_BLOCKS, DEFAULT_REPLACE_BLOCKS);
        nbt.putByte(RoadConstructionTool.NBT_ROAD_WIDTH, DEFAULT_ROAD_WIDTH);
        nbt.putInt(RoadConstructionTool.NBT_ROAD_TYPE, DEFAULT_ROAD_TYPE.getIndex());
    }

    public static void initStackTag(ItemStack stack) {
        if (!stack.getTag().contains(NBT_ROAD_WIDTH)) {
            stack.getTag().putByte(NBT_ROAD_WIDTH, DEFAULT_ROAD_WIDTH);
        }
        if (!stack.getTag().contains(NBT_ROAD_TYPE)) {
            stack.getTag().putInt(NBT_ROAD_TYPE, DEFAULT_ROAD_TYPE.getIndex());
        }
        if (!stack.getTag().contains(NBT_REPLACE_BLOCKS)) {
            stack.getTag().putBoolean(NBT_REPLACE_BLOCKS, DEFAULT_REPLACE_BLOCKS);
        }
    }

    private static StatusResult isLineValid(Vec3 a, Vec3 b) {
        boolean flag1 = a.distanceTo(b) < ModCommonConfig.ROAD_BUILDER_MAX_DISTANCE.get();
        boolean flag2 = de.mrjulsen.mcdragonlib.utils.Math.slope(a, b) >= ModCommonConfig.ROAD_BUILDER_MAX_SLOPE.get();
        int status = 0;

        if (!flag1) {
            status = ERROR_TOO_FAR;
        } else if (!flag2) {
            status = ERROR_SLOPE_TOO_STEEP;
        }
        return new StatusResult(flag1 && flag2, status, null);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);   
        
        initStackTag(itemstack);
        
        Location startLoc = Location.fromNbt(itemstack.getTag().getCompound(NBT_LOCATION1));
        Location endLoc = Location.fromNbt(itemstack.getTag().getCompound(NBT_LOCATION2));
        Collection<Map<BlockPos, Integer>> blockList = new ArrayList<>();

        if (endLoc != null && startLoc != null) {
            Vec3 start = startLoc.getLocationVec3();
            Vec3 end = endLoc.getLocationVec3();
            byte roadWidth = itemstack.getTag().getByte(NBT_ROAD_WIDTH);
            boolean replaceBlocks = true;
            blockList = calculateRoad(pLevel, start, end, roadWidth, replaceBlocks); 
        }

        if (pLevel.isClientSide) {
            ClientWrapper.showRoadConstructionToolScreen(
                itemstack,
                (int)blockList.stream().flatMap(x -> x.values().stream()).filter(v -> v <= 0 || v >= 8).count(),
                blockList.stream().flatMap(x -> x.values().stream()).filter(v -> v > 0 && v < 8).mapToInt(x -> x).sum()
            );
        }
        
        return InteractionResultHolder.success(itemstack);
    }

    public static RoadBuilderCountResult countBlocksNeeded(Level level, Vec3 start, Vec3 end, byte roadWidth, boolean replaceBlocks) {
        Collection<Map<BlockPos, Integer>> blockList = new ArrayList<>();        
        blockList = calculateRoad(level, start, end, roadWidth, replaceBlocks);
        return new RoadBuilderCountResult(
            (int)blockList.stream().flatMap(x -> x.values().stream()).filter(v -> v <= 0 || v >= 8).count(),
            blockList.stream().flatMap(x -> x.values().stream()).filter(v -> v > 0 && v < 8).mapToInt(x -> x).sum()
        );
    }

    public static Collection<Map<BlockPos, Integer>> calculateRoad(Level level, Vec3 start, Vec3 end, byte roadWidth, boolean replaceBlocks) {
        final double spacingMul = 2;

        if (start.y > end.y) {
            Vec3 temp = end;
            end = start;
            start = temp;
        }

        Vec3 vec = new Vec3(end.x, end.y, end.z).subtract(start);
        Vec3 rVec = new Vec3(vec.z, 0, -vec.x).normalize();

        double lastY = Double.MIN_VALUE;

        Collection<Map<BlockPos, Integer>> blockList = new ArrayList<>();
        for (int i = 0; i <= vec.length() * spacingMul; i++) {
            double d = 1.0D / vec.length() / spacingMul * i;
            double a = d;
            Map<BlockPos, Integer> blockLayer = new HashMap<>();
            Vec3 vecPos = new Vec3(vec.x * a, vec.y * a, vec.z * a).add(start);
            Vec3 rightVec = i == 0 ? rVec.normalize() : new Vec3(vec.z * a, 0, -(vec.x * a)).normalize();

            lastY = setLayer(level, lastY, vecPos, rightVec, blockLayer, roadWidth, replaceBlocks);
            blockList.forEach(x -> x.entrySet().removeIf(y -> blockLayer.keySet().stream().anyMatch(z -> y.getKey().equals(z))));
            blockList.add(blockLayer);
        }

        return blockList;
    }

    public static RoadBuildingData prepareRoadBuilding(Level pLevel, Player pPlayer, InteractionHand pHand, ItemStack pStack, Vec3 start, Vec3 end, byte roadWidth, boolean replaceBlocks, RoadType roadType) { 

        Collection<Map<BlockPos, Integer>> blockList = calculateRoad(pLevel, start, end, roadWidth, replaceBlocks);    
        pPlayer.getCooldowns().addCooldown(pStack.getItem(), blockList.size() * BUILD_DELAY_TICKS);

        if (!pLevel.isClientSide) {
            Utils.giveAdvancement((ServerPlayer)pPlayer, ModMain.MOD_ID, "road_construction_tool", "req");            
        }

        return new RoadBuildingData(blockList, pPlayer, pHand, pStack, start, end, roadWidth, replaceBlocks, roadType);
    }    

    private static double setLayer(Level pLevel, double lastY, Vec3 pos, Vec3 normalizedRightVec, Map<BlockPos, Integer> blockList, final byte roadWidth, final boolean replaceBlocks) {
        final double halfWidth = roadWidth / 2.0D - 0.5D;
        final double step = 0.5D;
        final double pixel = 1.0D / 16;
        final double slopeHeight = pixel * 2;
        final double height = pos.y < 0 ? pos.y - (int)pos.y + (Math.abs(pos.y - (int)pos.y) <= 0 ? 0 : 1) : pos.y - (int)pos.y; // wth
        

        if (lastY < (int)pos.y + slopeHeight) {
            for (double i = -(halfWidth); i < halfWidth + step; i += step) {
                Vec3 vec = pos.add(normalizedRightVec.scale(i));
                BlockPos bPos = new BlockPos(vec.x, vec.y - 1, vec.z);

                if (!replaceBlocks && !pLevel.isEmptyBlock(bPos)) {
                    continue;
                }

                if (blockList.containsKey(bPos)) {
                    blockList.remove(bPos);
                }
                blockList.put(bPos, 8);
            }
        }
        
        if (height >= slopeHeight * 8 + pixel) {
            for (double i = -(halfWidth); i < halfWidth + step; i += step) {
                Vec3 vec = pos.add(normalizedRightVec.scale(i));
                BlockPos bPos = new BlockPos(vec);
                
                if (!replaceBlocks && !pLevel.isEmptyBlock(bPos)) {
                    continue;
                }

                if (blockList.containsKey(bPos)) {
                    blockList.remove(bPos);
                }
                blockList.put(bPos, 8);
            }
        } else if (height >= slopeHeight) {
            for (double i = -(halfWidth); i < halfWidth + step; i += step) {
                Vec3 vec = pos.add(normalizedRightVec.scale(i));
                BlockPos bPos = new BlockPos(vec);
                
                if (!replaceBlocks && !pLevel.isEmptyBlock(bPos)) {
                    continue;
                }                

                if (blockList.containsKey(bPos)) {
                    blockList.remove(bPos);
                }
                blockList.put(bPos, (int)((height) / slopeHeight));
            }
        }
        return pos.y;
    }

	@OnlyIn(Dist.CLIENT)
    @SuppressWarnings("resource")
	public static void clientTick() {
        clientTicks++;
        if (clientTicks > (Minecraft.useFancyGraphics() ? FANCY_GRAPHICS_CLIENT_TICK_DELAY : FAST_GRAPHICS_CLIENT_TICK_DELAY)) {
            clientTicks = 0;
        }

        Player player = Minecraft.getInstance().player;
		Level level = Minecraft.getInstance().level;

		if (player == null || level == null)
			return;
		if (Minecraft.getInstance().screen != null)
			return;

		if (!(player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof RoadConstructionTool) && !(player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof RoadConstructionTool)) {
            return;
        }

        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof RoadConstructionTool ? player.getItemInHand(InteractionHand.MAIN_HAND) : player.getItemInHand(InteractionHand.OFF_HAND);
        CompoundTag nbt = stack.getOrCreateTag();

        initStackTag(stack);
        
        if (!nbt.contains(NBT_LOCATION1)) {
            return;
        }

        Vec3 start = Location.fromNbt(nbt.getCompound(NBT_LOCATION1)).getLocationVec3().add(0.5d, 0, 0.5d);
        Vec3 end = null;

        if (nbt.contains(NBT_LOCATION1) && !nbt.contains(NBT_LOCATION2)) {
            HitResult lookingAt = player.pick(player.getReachDistance() - 0.5D, 0, false);
            Vec3 lookAtVec = lookingAt.getLocation();

            if (!level.isEmptyBlock(new BlockPos(lookAtVec)) || !level.isEmptyBlock(new BlockPos(lookAtVec.x, lookAtVec.y - 0.5d, lookAtVec.z))) {
                double blockHeight = level.getBlockFloorHeight(new BlockPos(lookAtVec));
                int vX = lookAtVec.x() > 0 ? 1 : -1;
                int vZ = lookAtVec.z() > 0 ? 1 : -1;
                end = new Vec3((int)lookAtVec.x() + (vX * 0.5d), (int)lookAtVec.y() + (blockHeight <= 0 ? 0 : blockHeight), (int)lookAtVec.z() + (vZ * 0.5d));
            } else {
                end = null;
            }

            player.displayClientMessage(Utils.translate("item.trafficcraft.road_construction_tool.status_pos1",
                Location.fromNbt(nbt.getCompound(NBT_LOCATION1)).getLocationBlockPos().toShortString()
            ), true);

        } else if (nbt.contains(NBT_LOCATION1) && nbt.contains(NBT_LOCATION2)) {
            end = Location.fromNbt(nbt.getCompound(NBT_LOCATION2)).getLocationVec3().add(0.5d, 0, 0.5d);
            player.displayClientMessage(Utils.translate("item.trafficcraft.road_construction_tool.status_pos2",
                Location.fromNbt(nbt.getCompound(NBT_LOCATION1)).getLocationBlockPos().toShortString(),
                Location.fromNbt(nbt.getCompound(NBT_LOCATION2)).getLocationBlockPos().toShortString()
            ).withStyle(ChatFormatting.GREEN), true);
        }

        if (end == null) {
            return;
        }

        Vec3 line = end.subtract(start);

        final double width = nbt.getByte(NBT_ROAD_WIDTH);
        final double halfWidth = width / 2;
        final double spacing = 0.25D;

        if (line.length() <= 0) {
            return;
        }

        if (line.length() < 256) {
            double mul = 1.0D / Math.min(line.length(), 256) * spacing;

            int lineStatus = isLineValid(start, end).code();
            switch (lineStatus) {
                case ERROR_TOO_FAR:
                    player.displayClientMessage(Utils.translate("item.trafficcraft.road_construction_tool.status_too_far").withStyle(ChatFormatting.RED), true);
                    break;
                case ERROR_SLOPE_TOO_STEEP:
                    player.displayClientMessage(Utils.translate("item.trafficcraft.road_construction_tool.status_slope_too_steep").withStyle(ChatFormatting.RED), true);
                    break;
                default:
                    break;
            }

            if (clientTicks == 0) {
                for (double d = 0; d < 1; d += mul) {
                    Vec3 vecPos = new Vec3(line.x * d, line.y * d, line.z * d).add(start);
                    level.addParticle(new DustParticleOptions(isLineValid(start, end).result() ? new Vector3f(0.2f, 0.9f, 0.2f) : new Vector3f(0.9f, 0.2f, 0.2f), 1f), vecPos.x, vecPos.y, vecPos.z, 0, 0, 0);
                    
                    Vec3 rightVec = vecPos.add(new Vec3(line.z * d, 0, -line.x * d).normalize().scale(halfWidth));
                    level.addParticle(new DustParticleOptions(new Vector3f(1f, 1f, 0.6f), 0.5f), rightVec.x, rightVec.y, rightVec.z, 0, 0, 0);
                    
                    Vec3 leftVec = vecPos.add(new Vec3(-line.z * d, 0, line.x * d).normalize().scale(halfWidth));
                    level.addParticle(new DustParticleOptions(new Vector3f(1f, 1f, 0.6f), 0.5f), leftVec.x, leftVec.y, leftVec.z, 0, 0, 0);
                }
            }
        }
         
	}

    public static class RoadBuilderCountResult {
        public final int blocksCount;
        public final int slopesCount;

        protected RoadBuilderCountResult(int blocksCount, int slopesCount) {
            this.blocksCount = blocksCount;
            this.slopesCount = slopesCount;
        }
    }

    public static class RoadBuildingData {
        public final List<Map<BlockPos, Integer>> blocks;
        public final Player player;
        public final InteractionHand hand;
        public final ItemStack item;
        public final Vec3 start;
        public final Vec3 end;
        public final byte roadWidth;
        public final boolean replaceBlocks;
        public final RoadType roadType;

        public RoadBuildingData(Collection<Map<BlockPos, Integer>> blocks, Player player, InteractionHand hand, ItemStack item, Vec3 start, Vec3 end, byte roadWidth, boolean replaceBlocks, RoadType roadType) {
            this.blocks = new ArrayList<>(blocks);
            this.player = player;
            this.hand = hand;
            this.item = item;
            this.start = start;
            this.end = end;
            this.roadWidth = roadWidth;
            this.replaceBlocks = replaceBlocks;
            this.roadType = roadType;
        }
    }
}
