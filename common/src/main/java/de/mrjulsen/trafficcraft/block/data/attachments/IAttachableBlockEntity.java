package de.mrjulsen.trafficcraft.block.data.attachments;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.Direction;

import java.util.Map;
import java.util.Optional;

public interface IAttachableBlockEntity {

    Map<Direction, IPostAttachment<?>> getAttachments();

    default Optional<IPostAttachment<?>> getAttachment(Direction direction) {
        if (!getAttachments().containsKey(direction)) {
            return Optional.empty();
        }
        return Optional.ofNullable(getAttachments().get(direction));
    }

}
