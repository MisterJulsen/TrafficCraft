package de.mrjulsen.trafficcraft.registry.builtin;

import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.data.attachments.IAttachableBlockEntity;
import de.mrjulsen.trafficcraft.block.data.attachments.IPostAttachment;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class PostAttachmentRegistry {
    private PostAttachmentRegistry() {}

    public record PostAttachmentContext<B extends BlockEntity & IAttachableBlockEntity>(B blockEntity, Direction direction) {}

    @FunctionalInterface
    public interface PostAttachmentFactory<T extends IPostAttachment<?>> {
        <B extends BlockEntity & IAttachableBlockEntity> T create(PostAttachmentContext<B> context);
    }


    private static final Map<ResourceLocation, PostAttachmentRegistryObject<? extends IPostAttachment<?>>> TYPES = new HashMap<>();

    public static <T extends IPostAttachment<?>> PostAttachmentRegistryObject<T> register(ResourceLocation id, PostAttachmentFactory<T> factory) {
        PostAttachmentRegistryObject<T> type = new PostAttachmentRegistryObject<>(id, factory);
        TYPES.put(id, type);
        return type;
    }

    public static <T extends IPostAttachment<T>> Optional<T> load(CompoundTag tag, PostAttachmentContext<?> params) {
        return Optional.ofNullable(PostAttachmentRegistryObject.load(tag, params, PostAttachmentRegistry::get));
    }

    @SuppressWarnings("unchecked")
    private static <T extends IPostAttachment<?>> PostAttachmentRegistryObject<T> get(ResourceLocation id) {
        return (PostAttachmentRegistryObject<T>)TYPES.get(id);
    }



    public record PostAttachmentRegistryObject<T extends IPostAttachment<?>>(ResourceLocation id, PostAttachmentFactory<T> factory) {

        public static final String NBT_ID = "Id";

        public CompoundTag wrap(IPostAttachment<?> data) {
            CompoundTag tag = data.serializeNbt();
            tag.putString(NBT_ID, id.toString());
            return tag;
        }

        T unwrap(CompoundTag tag, PostAttachmentContext<?> context) {
            T instance = create(context);
            instance.deserializeNbt(tag);
            return instance;
        }

        public static <T extends IPostAttachment<?>> T load(CompoundTag tag, PostAttachmentContext<?> context, Function<ResourceLocation, PostAttachmentRegistryObject<T>> registryGetter) {
            ResourceLocation typeId = DLUtils.resourceLocation(tag.getString(NBT_ID));
            PostAttachmentRegistryObject<T> type = registryGetter.apply(typeId);
            if (type == null) {
                TrafficCraft.LOGGER.warn("Unable to load post attachment. An attachment type with id '" + typeId + "' does not exist.");
                return null;
            }
            return type.unwrap(tag, context);
        }

        T create(PostAttachmentContext<?> context) {
            return factory.create(context);
        }
    }

}
