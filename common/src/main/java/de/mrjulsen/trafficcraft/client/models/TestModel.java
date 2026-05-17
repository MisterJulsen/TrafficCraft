package de.mrjulsen.trafficcraft.client.models;

import com.mojang.math.Axis;
import de.mrjulsen.mcdragonlib.client.model.ModelContext;
import de.mrjulsen.mcdragonlib.client.model.ModelResourceLocationBuilder;
import de.mrjulsen.mcdragonlib.client.model.mesh.BasicMesh;
import de.mrjulsen.mcdragonlib.client.model.mesh.DLModel;
import de.mrjulsen.mcdragonlib.client.model.mesh.Face;
import de.mrjulsen.mcdragonlib.client.model.mesh.Mesh;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.Pair;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.data.attachments.IPostAttachment;
import de.mrjulsen.trafficcraft.block.entity.PostBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
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
