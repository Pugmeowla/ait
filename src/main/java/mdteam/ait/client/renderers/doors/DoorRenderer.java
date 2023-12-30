package mdteam.ait.client.renderers.doors;

import mdteam.ait.client.models.doors.DoorModel;
import mdteam.ait.client.renderers.AITRenderLayers;
import mdteam.ait.compat.DependencyChecker;
import mdteam.ait.core.blockentities.DoorBlockEntity;
import mdteam.ait.core.blocks.ExteriorBlock;
import mdteam.ait.tardis.TardisTravel;
import mdteam.ait.tardis.handler.DoorHandler;
import mdteam.ait.tardis.handler.properties.PropertiesHandler;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.DecoratedPotBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import mdteam.ait.tardis.TardisExterior;
import net.minecraft.world.LightType;

public class DoorRenderer<T extends DoorBlockEntity> implements BlockEntityRenderer<T> {

    private DoorModel model;

    public DoorRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.getTardis() == null)
            return;

        TardisExterior tardisExterior = entity.getTardis().getExterior();
        Class<? extends DoorModel> modelClass = tardisExterior.getVariant().door().model().getClass();

        if (model != null && !(model.getClass().isInstance(modelClass)))
            model = null;

        if (model == null)
            this.model = tardisExterior.getVariant().door().model();

        BlockState blockState = entity.getCachedState();
        float f = blockState.get(ExteriorBlock.FACING).asRotation();
        int maxLight = 0xFFFFFF;
        matrices.push();
        matrices.translate(0.5, 0, 0.5);
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f));
        Identifier texture = tardisExterior.getVariant().texture();

        // if (entity.getTardis().getDoor().getDoorState() != DoorHandler.DoorStateEnum.CLOSED)
        //     light = LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE;

        if (DependencyChecker.hasPortals() && entity.getTardis().getTravel().getState() == TardisTravel.State.LANDED && !PropertiesHandler.getBool(entity.getTardis().getHandlers().getProperties(), PropertiesHandler.IS_FALLING) /*&& entity.getTardis().getDoor().getDoorState() != DoorHandler.DoorStateEnum.CLOSED*/) {
            light = WorldRenderer.getLightmapCoordinates(entity.getTardis().getHandlers().getExteriorPos().getWorld(), entity.getTardis().getHandlers().getExteriorPos());;
        }

        if (model != null) {
            model.animateTile(entity);
            model.renderWithAnimations(entity, this.model.getPart(), matrices, vertexConsumers.getBuffer(AITRenderLayers.getEntityTranslucentCull(texture)), light, overlay, 1, 1, 1, 1);
            if (tardisExterior.getVariant().emission() != null)
                model.renderWithAnimations(entity, this.model.getPart(), matrices, vertexConsumers.getBuffer(AITRenderLayers.tardisRenderEmissionCull(tardisExterior.getVariant().emission(), false)), maxLight, overlay, 1, 1, 1, 1);
        }
        matrices.pop();
    }
}
