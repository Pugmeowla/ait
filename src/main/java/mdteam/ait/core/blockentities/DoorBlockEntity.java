package mdteam.ait.core.blockentities;

import mdteam.ait.compat.DependencyChecker;
import mdteam.ait.core.AITBlockEntityTypes;
import mdteam.ait.core.blocks.DoorBlock;
import mdteam.ait.core.blocks.ExteriorBlock;
import mdteam.ait.core.blocks.types.HorizontalDirectionalBlock;
import mdteam.ait.tardis.data.properties.PropertiesHandler;
import mdteam.ait.tardis.link.LinkableBlockEntity;
import mdteam.ait.tardis.util.TardisUtil;
import mdteam.ait.core.item.KeyItem;
import mdteam.ait.tardis.util.AbsoluteBlockPos;
import mdteam.ait.tardis.data.DoorData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.TardisDesktop;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static mdteam.ait.tardis.util.TardisUtil.findTardisByInterior;

public class DoorBlockEntity extends LinkableBlockEntity {
    public AnimationState DOOR_STATE = new AnimationState();
    public int animationTimer = 0;

    public DoorBlockEntity(BlockPos pos, BlockState state) {
        super(AITBlockEntityTypes.DOOR_BLOCK_ENTITY_TYPE, pos, state);
        if(!this.hasWorld()) return;
        if(this.findTardis().isEmpty()) return;
        this.linkDesktop();
    }

    public static <T extends BlockEntity> void tick(World world, BlockPos pos, BlockState blockState, T tDoor) {
        DoorBlockEntity door = (DoorBlockEntity) tDoor;
        if(world.isClient()) {
            door.checkAnimations();
        }

        if (door.tardisId == null) door.findTardis();

        if (door.findTardis().isEmpty()) return;
        if(!world.isClient() && door.findTardis().get().getTravel().getExteriorPos() == null) return;
        World exteriorWorld = door.findTardis().get().getTravel().getExteriorPos().getWorld();
        if(exteriorWorld == null) return;
        BlockState exteriorBlockState = exteriorWorld.getBlockState(door.findTardis().get().getTravel().getExteriorPos());
        if(exteriorBlockState.getBlock() instanceof ExteriorBlock) {
            world.setBlockState(pos, blockState.with(Properties.WATERLOGGED, exteriorBlockState.get(Properties.WATERLOGGED) && door.findTardis().get().getDoor().isOpen()), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
            world.emitGameEvent(null, GameEvent.BLOCK_CHANGE, pos);
            world.scheduleFluidTick(pos, blockState.getFluidState().getFluid(), blockState.getFluidState().getFluid().getTickRate(world));
        }
    }

    public void useOn(World world, boolean sneaking, PlayerEntity player) {
        if (player == null || this.findTardis().isEmpty())
            return;
        if (this.findTardis().get().isGrowth() && this.findTardis().get().hasGrowthExterior())
            return;
        if (player.getMainHandStack().getItem() instanceof KeyItem && !this.findTardis().get().isSiegeMode()) {
            ItemStack key = player.getMainHandStack();
            NbtCompound tag = key.getOrCreateNbt();
            if (!tag.contains("tardis")) {
                return;
            }
            if (Objects.equals(this.findTardis().get().getUuid().toString(), tag.getString("tardis"))) {
                DoorData.toggleLock(this.findTardis().get(), (ServerPlayerEntity) player);
            } else {
                world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), SoundCategory.BLOCKS, 1F, 0.2F);
                player.sendMessage(Text.literal("TARDIS does not identify with key"), true);
            }
            return;
        }
        DoorData.useDoor(this.findTardis().get(), (ServerWorld) world, this.getPos(), (ServerPlayerEntity) player);
        // fixme maybe this is required idk the DoorData already marks the tardis dirty || tardis().markDirty();
        if (sneaking)
            return;
    }
    public Direction getFacing() {
        return this.getCachedState().get(HorizontalDirectionalBlock.FACING);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
    public void onEntityCollision(Entity entity) {
        // so many ifs
        if (this.getWorld() != TardisUtil.getTardisDimension()) return;
        if (this.findTardis().isEmpty()) return;
        if (this.findTardis().get().getDoor().isClosed()) return;
        if (this.findTardis().get().getLockedTardis()) return;
        if (PropertiesHandler.getBool(findTardis().get().getHandlers().getProperties(), PropertiesHandler.IS_FALLING)) return;
        if (DependencyChecker.hasPortals() && this.findTardis().get().getExterior().getVariant().hasPortals()) return;

        TardisUtil.teleportOutside(this.findTardis().get(), entity);
    }

    public void checkAnimations() {
        // DO NOT RUN THIS ON SERVER!!
        if(findTardis().isEmpty()) return;
        animationTimer++;

        if (findTardis().get().getHandlers().getDoor().getAnimationInteriorState() == null || !(findTardis().get().getHandlers().getDoor().getAnimationInteriorState().equals(findTardis().get().getDoor().getDoorState()))) {
            DOOR_STATE.start(animationTimer);
            findTardis().get().getHandlers().getDoor().tempInteriorState = findTardis().get().getDoor().getDoorState();
        }
    }

    @Override
    public Optional<Tardis> findTardis() {
        if (this.tardisId == null) {
            Tardis found = findTardisByInterior(pos, !this.getWorld().isClient());
            if (found != null)
                this.setTardis(found);
        }
        return super.findTardis();
    }

    public void setTardis(UUID uuid) {
        super.setTardis(uuid);

        this.linkDesktop();
    }

    public void linkDesktop() {
        if (this.findTardis().isEmpty())
            return;
        this.setDesktop(this.getDesktop());
    }

    public TardisDesktop getDesktop() {
        if(this.findTardis().isEmpty()) return null;
        return this.findTardis().get().getDesktop();
    }

    public void setDesktop(TardisDesktop desktop) {
        if (!this.hasWorld() || this.getWorld().isClient()) return;

        desktop.setInteriorDoorPos(new AbsoluteBlockPos.Directed(
                this.pos, TardisUtil.getTardisDimension(), this.getFacing())
        );
    }
}