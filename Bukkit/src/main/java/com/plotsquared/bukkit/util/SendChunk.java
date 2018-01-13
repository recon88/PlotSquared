package com.plotsquared.bukkit.util;

import static com.intellectualcrafters.plot.util.ReflectionUtils.getRefClass;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefClass;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefConstructor;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefField;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefMethod;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.bukkit.object.BukkitPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

/**
 * An utility that can be used to send chunks, rather than using bukkit code
 * to do so (uses heavy NMS).
 */
public class SendChunk {

    private final RefMethod methodGetHandlePlayer;
    private final RefMethod methodGetHandleChunk;
    private final RefConstructor mapChunk;
    private final RefField connection;
    private final RefMethod send;
    private final RefMethod methodInitLighting;

    /**
     * Constructor.
     */
    public SendChunk() throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException {
        RefConstructor tempMapChunk;
        RefClass classCraftPlayer = getRefClass("{cb}.entity.CraftPlayer");
        this.methodGetHandlePlayer = classCraftPlayer.getMethod("getHandle");
        RefClass classCraftChunk = getRefClass("{cb}.CraftChunk");
        this.methodGetHandleChunk = classCraftChunk.getMethod("getHandle");
        RefClass classChunk = getRefClass("{nms}.Chunk");
        this.methodInitLighting = classChunk.getMethod("initLighting");
        RefClass classMapChunk = getRefClass("{nms}.PacketPlayOutMapChunk");
        if (PS.get().checkVersion(PS.get().IMP.getServerVersion(), BukkitVersion.v1_9_4)) {
            //this works for 1.9.4 and 1.10
            tempMapChunk = classMapChunk.getConstructor(classChunk.getRealClass(),int.class);
        } else {
            try {
                tempMapChunk = classMapChunk.getConstructor(classChunk.getRealClass(), boolean.class, int.class);
            } catch (NoSuchMethodException ignored) {
                tempMapChunk = classMapChunk.getConstructor(classChunk.getRealClass(),boolean.class, int.class, int.class);
            }
        }
        this.mapChunk = tempMapChunk;
        RefClass classEntityPlayer = getRefClass("{nms}.EntityPlayer");
        this.connection = classEntityPlayer.getField("playerConnection");
        RefClass classPacket = getRefClass("{nms}.Packet");
        RefClass classConnection = getRefClass("{nms}.PlayerConnection");
        this.send = classConnection.getMethod("sendPacket", classPacket.getRealClass());
    }

    public void sendChunk(Collection<Chunk> input) {
        HashSet<Chunk> chunks = new HashSet<>(input);
        HashMap<String, ArrayList<Chunk>> map = new HashMap<>();
        int view = Bukkit.getServer().getViewDistance();
        for (Chunk chunk : chunks) {
            String world = chunk.getWorld().getName();
            ArrayList<Chunk> list = map.get(world);
            if (list == null) {
                list = new ArrayList<>();
                map.put(world, list);
            }
            list.add(chunk);
            Object c = this.methodGetHandleChunk.of(chunk).call();
            this.methodInitLighting.of(c).call();
        }
        for (Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
            PlotPlayer pp = entry.getValue();
            Plot plot = pp.getCurrentPlot();
            Location location = null;
            String world;
            if (plot != null) {
                world = plot.getWorldName();
            } else {
                location = pp.getLocation();
                world = location.getWorld();
            }
            ArrayList<Chunk> list = map.get(world);
            if (list == null) {
                continue;
            }
            if (location == null) {
                location = pp.getLocation();
            }
            int cx = location.getX() >> 4;
            int cz = location.getZ() >> 4;
            Player player = ((BukkitPlayer) pp).player;
            Object entity = this.methodGetHandlePlayer.of(player).call();

            for (Chunk chunk : list) {
                int dx = Math.abs(cx - chunk.getX());
                int dz = Math.abs(cz - chunk.getZ());
                if ((dx > view) || (dz > view)) {
                    continue;
                }
                Object c = this.methodGetHandleChunk.of(chunk).call();
                chunks.remove(chunk);
                Object con = this.connection.of(entity).get();
                Object packet = null;
                if (PS.get().checkVersion(PS.get().IMP.getServerVersion(), BukkitVersion.v1_9_4)) {
                    try {
                        packet = this.mapChunk.create(c,65535);
                    } catch (Exception ignored) {}
                } else {
                    try {
                        packet = this.mapChunk.create(c, true, 65535);
                    } catch (ReflectiveOperationException | IllegalArgumentException e) {
                        try {
                            packet = this.mapChunk.create(c, true, 65535, 5);
                        } catch (ReflectiveOperationException | IllegalArgumentException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                if (packet == null) {
                    PS.debug("Error with PacketPlayOutMapChunk reflection.");
                }
                this.send.of(con).call(packet);
            }
        }
        for (final Chunk chunk : chunks) {
            TaskManager.runTask(new Runnable() {
                @Override
                public void run() {
                    try {
                        chunk.unload(true, false);
                    } catch (Throwable ignored) {
                        String worldName = chunk.getWorld().getName();
                        PS.debug("$4Could not save chunk: " + worldName + ';' + chunk.getX() + ";" + chunk.getZ());
                        PS.debug("$3 - $4File may be open in another process (e.g. MCEdit)");
                        PS.debug("$3 - $4" + worldName + "/level.dat or " + worldName
                                + "/level_old.dat may be corrupt (try repairing or removing these)");
                    }
                }
            });
        }
    }

    public void sendChunk(String worldName, Collection<ChunkLoc> chunkLocations) {
        World myWorld = Bukkit.getWorld(worldName);
        ArrayList<Chunk> chunks = new ArrayList<>();
        for (ChunkLoc loc : chunkLocations) {
            if (myWorld.isChunkLoaded(loc.x, loc.z)) {
                chunks.add(myWorld.getChunkAt(loc.x, loc.z));
            }
        }
        sendChunk(chunks);
    }
}
