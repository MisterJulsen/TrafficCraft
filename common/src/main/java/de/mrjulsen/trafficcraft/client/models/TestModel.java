package de.mrjulsen.trafficcraft.client.models;

import de.mrjulsen.mcdragonlib.client.model.ModelContext;
import de.mrjulsen.mcdragonlib.client.model.mesh.BasicMesh;
import de.mrjulsen.mcdragonlib.client.model.mesh.DLModel;
import de.mrjulsen.mcdragonlib.client.model.mesh.Face;
import de.mrjulsen.mcdragonlib.client.model.mesh.Mesh;
import de.mrjulsen.trafficcraft.block.entity.PostBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

public class TestModel extends DLModel {
    @Override
    protected Mesh getMesh(ModelType type, BakedModel originalModel, BlockState state, RandomSource random, ModelContext context) {
        BasicMesh mesh = BasicMesh.fromBakedModel(state, originalModel, random);
        if (context.has(PostBlockEntity.PROPERTY_ATTACHMENTS)) {
            PostBlockEntity.AttachmentModelData[] attachments = context.get(PostBlockEntity.PROPERTY_ATTACHMENTS);
            for (PostBlockEntity.AttachmentModelData attachment : attachments) {
                Mesh m = attachment.attachment().getModel(state, random, context);
                m.rotate(attachment.rotation(), new Vector3f(0.5f));
                mesh.combine(false, m);
            }
        }

        return mesh;
    }
}
