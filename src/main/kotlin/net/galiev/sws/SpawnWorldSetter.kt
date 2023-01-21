package net.galiev.sws

import com.mojang.logging.LogUtils
import net.fabricmc.api.ModInitializer
import net.galiev.sws.config.ConfigManager
import net.galiev.sws.event.PlayerFirstJoinCallback
import net.galiev.sws.helper.WorldHelper.getRandInt
import net.galiev.sws.helper.WorldHelper.isSafe
import net.galiev.sws.helper.WorldHelper.safeCheck
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.slf4j.Logger

object SpawnWorldSetter : ModInitializer {
    const val MOD_ID = "sws"
    val LOGGER: Logger = LogUtils.getLogger();
    override fun onInitialize() {
        ConfigManager
        PlayerFirstJoinCallback.EVENT.register(object : PlayerFirstJoinCallback.FirstJoin {
            override fun joinServerForFirstTime(player: ServerPlayerEntity, server: MinecraftServer) {
                val x: Int = getRandInt(ConfigManager.read().rangeX)
                var y = 50
                val z: Int = getRandInt(ConfigManager.read().rangeZ)

                val blockPos = BlockPos.Mutable(x, y, z)

                val world: ServerWorld = ConfigManager.read().dimension.split(":").let { value ->
                    server.worlds.find { it.registryKey.value == Identifier(value[0], value[1]) }
                } ?: return server.close()

                safeCheck(world, blockPos)

                if (isSafe(world, blockPos)) {
                    if (world.registryKey == World.NETHER) {
                        player.setSpawnPoint(world.registryKey, blockPos, player.limbAngle, true, false)
                        player.teleport(
                            world,
                            blockPos.x.toDouble(),
                            blockPos.y.toDouble(),
                            blockPos.z.toDouble(),
                            player.bodyYaw,
                            player.prevPitch
                        )
                    } else {
                        player.setSpawnPoint(world.registryKey, blockPos, player.limbAngle, true, false)
                        player.moveToWorld(world)
                    }
                }
            }
        })
    }
}