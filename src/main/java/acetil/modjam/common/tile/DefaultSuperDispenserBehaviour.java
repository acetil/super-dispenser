package acetil.modjam.common.tile;

import acetil.modjam.common.ModJam;
import acetil.modjam.common.entity.DispenserItemEntity;
import acetil.modjam.common.network.DispenserEntitySpawnMessage;
import acetil.modjam.common.network.PacketHandler;
import acetil.modjam.common.particle.DispenserItemParticleData;
import acetil.modjam.common.util.QuadFunction;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.logging.log4j.Level;

import java.util.function.Function;
import java.util.function.Predicate;

public class DefaultSuperDispenserBehaviour {
    private static final double DIRECTION_MULT = 0.5;
    private static final double POSITION_OFFSET = 0.5;
    private static final double DISPENSER_ENTITY_VELOCITY = 1;
    public static final Function<ItemStack, ItemStack> ITEM_STACK_SHRINK = (ItemStack stack) -> {
        ItemStack stack1 = stack.copy();
        stack1.shrink(1);
        return stack1;
    };
    public static final Predicate<ItemStack> MATCH_NOT_EMPTY = (ItemStack stack) -> !stack.isEmpty();

    public static final QuadFunction<ItemStack, World, BlockPos, Direction, Boolean> SPAWN_DISPENSER_ENTITY =
            (ItemStack stack, World world, BlockPos pos, Direction d) -> {
                // TODO: deal with case when block in front of dispenser
                ModJam.LOGGER.log(Level.DEBUG, "Spawning dispenser entity at {}!", System.currentTimeMillis());
                Vec3i vec1 = d.getDirectionVec();
                Vec3d velVec = new Vec3d(vec1).scale(DISPENSER_ENTITY_VELOCITY);
                Vec3d offVec = getOffsetSpawnVec(pos, d);
                /*PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(pos)),
                        new DispenserEntitySpawnMessage(stack.copy().split(1), offVec, velVec, 10));*/
                world.addEntity(new DispenserItemEntity(world, offVec).setVelocityCustom(velVec)
                        .setItemStack(stack.copy().split(1)).setDispenserPos(pos));
                return true;
            };
    public static void addDefaultInitialBehaviours () {
        ModJam.LOGGER.log(Level.INFO, "Adding default initial dispenser behaviours!");
        SuperDispenserBehaviour.registerInitial(MATCH_NOT_EMPTY, ITEM_STACK_SHRINK,
                (ItemStack stack, World world, BlockPos pos, Direction direction) -> {
            ModJam.LOGGER.log(Level.DEBUG, "Dispensed item in direction: " + direction.getName());
            Vec3i vec1 = direction.getDirectionVec();
            Vec3d spawnVec = getOffsetSpawnVec(pos, direction);
            ItemEntity entity = new ItemEntity(world, spawnVec.getX(),spawnVec.getY(),
                    spawnVec.getZ(), stack.copy().split(1));
            entity.setMotion((double)vec1.getX()*0, (double)vec1.getY() * 0, (double)vec1.getZ() * 0);
            world.addEntity(entity);
            return true;
        });
        SuperDispenserBehaviour.registerInitial(MATCH_NOT_EMPTY, ITEM_STACK_SHRINK, SPAWN_DISPENSER_ENTITY);
    }

    private static Vec3d getOffsetSpawnVec (BlockPos pos, Direction d) {
        Vec3i vec1 = d.getDirectionVec();
        return new Vec3d(pos.getX() + POSITION_OFFSET + vec1.getX() * DIRECTION_MULT,
                pos.getY() + POSITION_OFFSET + vec1.getY() * DIRECTION_MULT,
                pos.getZ() + POSITION_OFFSET + vec1.getZ() * DIRECTION_MULT);
    }
}
