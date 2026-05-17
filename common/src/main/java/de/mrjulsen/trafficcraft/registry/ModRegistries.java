package de.mrjulsen.trafficcraft.registry;

import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.data.attachments.TrafficLightPostAttachment;
import de.mrjulsen.trafficcraft.block.data.attachments.TrafficSignPostAttachment;
import de.mrjulsen.trafficcraft.block.entity.PostBlockEntity;
import de.mrjulsen.trafficcraft.registry.builtin.PostAttachmentRegistry;

public final class ModRegistries {
    private ModRegistries() {}

    public static final PostAttachmentRegistry.PostAttachmentRegistryObject<TrafficLightPostAttachment> TRAFFIC_LIGHT_POST_ATTACHMENT = PostAttachmentRegistry.register(DLUtils.resourceLocation(TrafficCraft.MOD_ID, "traffic_light"), TrafficLightPostAttachment::new);
    public static final PostAttachmentRegistry.PostAttachmentRegistryObject<TrafficSignPostAttachment> TRAFFIC_SIGN_POST_ATTACHMENT = PostAttachmentRegistry.register(DLUtils.resourceLocation(TrafficCraft.MOD_ID, "traffic_sign"), TrafficSignPostAttachment::new);


    public static void init() {
    }

}
