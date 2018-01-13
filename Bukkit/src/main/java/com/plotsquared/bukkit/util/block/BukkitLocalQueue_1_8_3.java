package com.plotsquared.bukkit.util.block;

import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.ChunkWrapper;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.ReflectionUtils;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.block.BasicLocalBlockQueue;
import com.plotsquared.bukkit.util.SendChunk;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;


import static com.intellectualcrafters.plot.util.ReflectionUtils.getRefClass;

public class BukkitLocalQueue_1_8_3 extends BukkitLocalQueue<char[]> {

    private final SendChunk sendChunk;
    private final HashMap<ChunkWrapper, Chunk> toUpdate = new HashMap<>();
    private final ReflectionUtils.RefMethod methodGetHandleChunk;
    private final ReflectionUtils.RefMethod methodGetHandleWorld;
    private final ReflectionUtils.RefMethod methodInitLighting;
    private final ReflectionUtils.RefConstructor classBlockPositionConstructor;
    private final ReflectionUtils.RefConstructor classChunkSectionConstructor;
    private final ReflectionUtils.RefMethod methodX;
    private final ReflectionUtils.RefMethod methodAreNeighborsLoaded;
    private final ReflectionUtils.RefField fieldSections;
    private final ReflectionUtils.RefField fieldWorld;
    private final ReflectionUtils.RefMethod methodGetIdArray;
    private final ReflectionUtils.RefMethod methodGetWorld;
    private final ReflectionUtils.RefField tileEntityListTick;

    public BukkitLocalQueue_1_8_3(String world) throws NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
        super(world);
        ReflectionUtils.RefClass classCraftChunk = getRefClass("{cb}.CraftChunk");
        ReflectionUtils.RefClass classCraftWorld = getRefClass("{cb}.CraftWorld");
        this.methodGetHandleChunk = classCraftChunk.getMethod("getHandle");
        ReflectionUtils.RefClass classChunk = getRefClass("{nms}.Chunk");
        this.methodInitLighting = classChunk.getMethod("initLighting");
        ReflectionUtils.RefClass classBlockPosition = getRefClass("{nms}.BlockPosition");
        this.classBlockPositionConstructor = classBlockPosition.getConstructor(int.class, int.class, int.class);
        ReflectionUtils.RefClass classWorld = getRefClass("{nms}.World");
        this.methodX = classWorld.getMethod("x", classBlockPosition.getRealClass());
        this.fieldSections = classChunk.getField("sections");
        this.fieldWorld = classChunk.getField("world");
        ReflectionUtils.RefClass classChunkSection = getRefClass("{nms}.ChunkSection");
        this.methodGetIdArray = classChunkSection.getMethod("getIdArray");
        this.methodAreNeighborsLoaded = classChunk.getMethod("areNeighborsLoaded", int.class);
        this.classChunkSectionConstructor = classChunkSection.getConstructor(int.class, boolean.class, char[].class);
        this.tileEntityListTick = classWorld.getField("tileEntityList");
        this.methodGetHandleWorld = classCraftWorld.getMethod("getHandle");
        this.methodGetWorld = classChunk.getMethod("getWorld");
        this.sendChunk = new SendChunk();
        TaskManager.runTaskRepeat(new Runnable() {
            @Override
            public void run() {
                if (BukkitLocalQueue_1_8_3.this.toUpdate.isEmpty()) {
                    return;
                }
                int count = 0;
                ArrayList<Chunk> chunks = new ArrayList<>();
                Iterator<Map.Entry<ChunkWrapper, Chunk>> i = BukkitLocalQueue_1_8_3.this.toUpdate.entrySet().iterator();
                while (i.hasNext() && count < 128) {
                    chunks.add(i.next().getValue());
                    i.remove();
                    count++;
                }
                if (count == 0) {
                    return;
                }
                update(chunks);
            }
        }, 1);
        MainUtil.initCache();
    }

    @Override
    public LocalChunk<char[]> getLocalChunk(int x, int z) {
        return new CharLocalChunk_1_8_3(this, x, z);
    }

    public class CharLocalChunk_1_8_3 extends CharLocalChunk {
        public short[] count;
        public short[] air;
        public short[] relight;

        public CharLocalChunk_1_8_3(BasicLocalBlockQueue parent, int x, int z) {
            super(parent, x, z);
            this.count = new short[16];
            this.air = new short[16];
            this.relight = new short[16];
        }

        @Override
        public void setBlock(int x, int y, int z, int id, int data) {
            int i = MainUtil.CACHE_I[y][x][z];
            int j = MainUtil.CACHE_J[y][x][z];
            char[] vs = this.blocks[i];
            if (vs == null) {
                vs = this.blocks[i] = new char[4096];
                this.count[i]++;
            } else if (vs[j] == 0) {
                this.count[i]++;
            }
            switch (id) {
                case 0:
                    this.air[i]++;
                    vs[j] = (char) 1;
                    return;
                case 10:
                case 11:
                case 39:
                case 40:
                case 51:
                case 74:
                case 89:
                case 122:
                case 124:
                case 138:
                case 169:
                    this.relight[i]++;
                case 2:
                case 4:
                case 13:
                case 14:
                case 15:
                case 20:
                case 21:
                case 22:
                case 30:
                case 32:
                case 37:
                case 41:
                case 42:
                case 45:
                case 46:
                case 47:
                case 48:
                case 49:
                case 55:
                case 56:
                case 57:
                case 58:
                case 60:
                case 7:
                case 8:
                case 9:
                case 73:
                case 78:
                case 79:
                case 80:
                case 81:
                case 82:
                case 83:
                case 85:
                case 87:
                case 88:
                case 101:
                case 102:
                case 103:
                case 110:
                case 112:
                case 113:
                case 121:
                case 129:
                case 133:
                case 165:
                case 166:
                case 170:
                case 172:
                case 173:
                case 174:
                case 181:
                case 182:
                case 188:
                case 189:
                case 190:
                case 191:
                case 192:
                    vs[j] = (char) (id << 4);
                    return;
                case 130:
                case 76:
                case 62:
                    this.relight[i]++;
                case 54:
                case 146:
                case 61:
                case 65:
                case 68:
                case 50:
                    if (data < 2) {
                        data = 2;
                    }
                default:
                    vs[j] = (char) ((id << 4) + data);
                    return;
            }
        }

        public char[] getIdArray(int i) {
            return this.blocks[i];
        }

        public int getCount(int i) {
            return this.count[i];
        }

        public int getAir(int i) {
            return this.air[i];
        }

        public void setCount(int i, short value) {
            this.count[i] = value;
        }

        public int getRelight(int i) {
            return this.relight[i];
        }

        public int getTotalCount() {
            int total = 0;
            for (int i = 0; i < 16; i++) {
                total += this.count[i];
            }
            return total;
        }

        public int getTotalRelight() {
            if (getTotalCount() == 0) {
                Arrays.fill(this.count, (short) 1);
                Arrays.fill(this.relight, Short.MAX_VALUE);
                return Short.MAX_VALUE;
            }
            int total = 0;
            for (int i = 0; i < 16; i++) {
                total += this.relight[i];
            }
            return total;
        }
    }

    @Override
    public void setBlocks(LocalChunk lc) {
        CharLocalChunk_1_8_3 fs = (CharLocalChunk_1_8_3) lc;
        Chunk chunk = getChunk(lc.getX(), lc.getZ());
        chunk.load(true);
        World world = chunk.getWorld();
        ChunkWrapper wrapper = new ChunkWrapper(getWorld(), lc.getX(), lc.getZ());
        if (!this.toUpdate.containsKey(wrapper)) {
            this.toUpdate.put(wrapper, chunk);
        }
        try {
            boolean flag = world.getEnvironment() == World.Environment.NORMAL;

            // Sections
            Method getHandle = chunk.getClass().getDeclaredMethod("getHandle");
            Object c = getHandle.invoke(chunk);
            Object w = this.methodGetWorld.of(c).call();
            Class<?> clazz = c.getClass();
            Field sections1 = clazz.getDeclaredField("sections");
            sections1.setAccessible(true);
            Field tileEntities = clazz.getDeclaredField("tileEntities");
            Field entitySlices = clazz.getDeclaredField("entitySlices");
            Object[] sections = (Object[]) sections1.get(c);
            Map<?, ?> tiles = (Map<?, ?>) tileEntities.get(c);
            Collection<?>[] entities = (Collection<?>[]) entitySlices.get(c);

            Method getX = null;
            Method getY = null;
            Method getZ = null;

            // Trim tiles
            boolean removed = false;
            Set<Map.Entry<?, ?>> entrySet = (Set<Map.Entry<?, ?>>) (Set<?>) tiles.entrySet();
            Iterator<Map.Entry<?, ?>> iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                Map.Entry<?, ?> tile = iterator.next();
                Object pos = tile.getKey();
                if (getX == null) {
                    Class<? extends Object> clazz2 = pos.getClass().getSuperclass();
                    getX = clazz2.getDeclaredMethod("getX");
                    getY = clazz2.getDeclaredMethod("getY");
                    getZ = clazz2.getDeclaredMethod("getZ");
                }
                int lx = (int) getX.invoke(pos) & 15;
                int ly = (int) getY.invoke(pos);
                int lz = (int) getZ.invoke(pos) & 15;
                int j = MainUtil.CACHE_I[ly][lx][lz];
                int k = MainUtil.CACHE_J[ly][lx][lz];
                char[] array = fs.getIdArray(j);
                if (array == null) {
                    continue;
                }
                if (array[k] != 0) {
                    removed = true;
                    iterator.remove();
                }
            }
            if (removed) {
                ((Collection) this.tileEntityListTick.of(w).get()).clear();
            }

            // Trim entities
            for (int i = 0; i < 16; i++) {
                if ((entities[i] != null) && (fs.getCount(i) >= 4096)) {
                    entities[i].clear();
                }
            }

            // Efficiently merge sections
            for (int j = 0; j < sections.length; j++) {
                if (fs.getCount(j) == 0) {
                    continue;
                }
                char[] newArray = fs.getIdArray(j);
                if (newArray == null) {
                    continue;
                }
                Object section = sections[j];
                if ((section == null) || (fs.getCount(j) >= 4096)) {
                    section = sections[j] = newChunkSection(j << 4, flag, newArray);
                    continue;
                }
                char[] currentArray = getIdArray(section);
                boolean fill = true;
                for (int k = 0; k < newArray.length; k++) {
                    char n = newArray[k];
                    switch (n) {
                        case 0:
                            fill = false;
                            continue;
                        case 1:
                            fill = false;
                            currentArray[k] = 0;
                            continue;
                        default:
                            currentArray[k] = n;
                            continue;
                    }
                }
                if (fill) {
                    fs.setCount(j, Short.MAX_VALUE);
                }
            }
            // Clear
        } catch (IllegalArgumentException | SecurityException | ReflectiveOperationException e) {
            e.printStackTrace();
        }
        fixLighting(chunk, fs, true);
    }

    public Object newChunkSection(int i, boolean flag, char[] ids) throws ReflectiveOperationException {
        return this.classChunkSectionConstructor.create(i, flag, ids);
    }

    public char[] getIdArray(Object obj) {
        return (char[]) this.methodGetIdArray.of(obj).call();
    }

    @Override
    public void fixChunkLighting(int x, int z) {
        Object c = this.methodGetHandleChunk.of(getChunk(x, z)).call();
        this.methodInitLighting.of(c).call();
    }

    public boolean fixLighting(Chunk chunk, CharLocalChunk_1_8_3 bc, boolean fixAll) {
        try {
            if (!chunk.isLoaded()) {
                chunk.load(false);
            } else {
                chunk.unload(true, false);
                chunk.load(false);
            }

            // Initialize lighting
            Object c = this.methodGetHandleChunk.of(chunk).call();

            if (fixAll && !(boolean) this.methodAreNeighborsLoaded.of(c).call(1)) {
                World world = chunk.getWorld();
                ChunkWrapper wrapper = new ChunkWrapper(getWorld(), chunk.getX(), chunk.getZ());
                for (int x = wrapper.x - 1; x <= wrapper.x + 1; x++) {
                    for (int z = wrapper.z - 1; z <= wrapper.z + 1; z++) {
                        if (x != 0 && z != 0) {
                            Chunk other = world.getChunkAt(x, z);
                            while (!other.isLoaded()) {
                                other.load(true);
                            }
                            ChunkManager.manager.loadChunk(wrapper.world, new ChunkLoc(x, z), true);
                        }
                    }
                }
            /*
                if (!(boolean) methodAreNeighborsLoaded.of(c).call(1)) {
                    return false;
                }
            */
            }

            this.methodInitLighting.of(c).call();

            if (bc.getTotalRelight() == 0 && !fixAll) {
                return true;
            }

            Object[] sections = (Object[]) this.fieldSections.of(c).get();
            Object w = this.fieldWorld.of(c).get();

            int X = chunk.getX() << 4;
            int Z = chunk.getZ() << 4;

            ReflectionUtils.RefMethod.RefExecutor relight = this.methodX.of(w);
            for (int j = 0; j < sections.length; j++) {
                Object section = sections[j];
                if (section == null) {
                    continue;
                }
                if ((bc.getRelight(j) == 0 && !fixAll) || bc.getCount(j) == 0 || (bc.getCount(j) >= 4096 && bc.getAir(j) == 0)) {
                    continue;
                }
                char[] array = getIdArray(section);
                int l = PseudoRandom.random.random(2);
                for (int k = 0; k < array.length; k++) {
                    int i = array[k];
                    if (i < 16) {
                        continue;
                    }
                    short id = (short) (i >> 4);
                    switch (id) { // Lighting
                        default:
                            if (!fixAll) {
                                continue;
                            }
                            if ((k & 1) == l) {
                                l = 1 - l;
                                continue;
                            }
                        case 10:
                        case 11:
                        case 39:
                        case 40:
                        case 50:
                        case 51:
                        case 62:
                        case 74:
                        case 76:
                        case 89:
                        case 122:
                        case 124:
                        case 130:
                        case 138:
                        case 169:
                            int x = MainUtil.x_loc[j][k];
                            int y = MainUtil.y_loc[j][k];
                            int z = MainUtil.z_loc[j][k];
                            if (isSurrounded(sections, x, y, z)) {
                                continue;
                            }
                            Object pos = this.classBlockPositionConstructor.create(X + x, y, Z + z);
                            relight.call(pos);
                    }
                }
            }
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isSurrounded(Object[] sections, int x, int y, int z) {
        return isSolid(getId(sections, x, y + 1, z))
                && isSolid(getId(sections, x + 1, y - 1, z))
                && isSolid(getId(sections, x - 1, y, z))
                && isSolid(getId(sections, x, y, z + 1))
                && isSolid(getId(sections, x, y, z - 1));
    }

    public boolean isSolid(int i) {
        return i != 0 && Material.getMaterial(i).isOccluding();
    }

    public int getId(Object[] sections, int x, int y, int z) {
        if (x < 0 || x > 15 || z < 0 || z > 15) {
            return 1;
        }
        if (y < 0 || y > 255) {
            return 1;
        }
        int i = MainUtil.CACHE_I[y][x][z];
        Object section = sections[i];
        if (section == null) {
            return 0;
        }
        char[] array = getIdArray(section);
        int j = MainUtil.CACHE_J[y][x][z];
        return array[j] >> 4;
    }

    public void update(Collection<Chunk> chunks) {
        if (chunks.isEmpty()) {
            return;
        }
        if (!MainUtil.canSendChunk) {
            for (Chunk chunk : chunks) {
                chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
                chunk.unload(true, false);
                chunk.load();
            }
            return;
        }
        try {
            this.sendChunk.sendChunk(chunks);
        } catch (Throwable e) {
            e.printStackTrace();
            MainUtil.canSendChunk = false;
        }
    }

    @Override
    public void refreshChunk(int x, int z) {
        update(Arrays.asList(Bukkit.getWorld(getWorld()).getChunkAt(x, z)));
    }
}
