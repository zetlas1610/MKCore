package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TalentTreeRecord {
    private final TalentTreeDefinition tree;
    private final Map<String, TalentLineRecord> lines = new HashMap<>();
    private final TalentTreeUpdater updater;

    public TalentTreeRecord(TalentTreeDefinition tree) {
        this.tree = tree;
        updater = new TalentTreeUpdater(this);
    }

    @Nonnull
    ISyncObject getUpdater() {
        return updater;
    }

    public void setUpdateCallback(Consumer<CompoundNBT> callback) {
        updater.deserializationCallback = callback;
    }

    @Nonnull
    public TalentTreeDefinition getTreeDefinition() {
        return tree;
    }

    public Stream<TalentRecord> getRecordStream() {
        return lines.values().stream()
                .flatMap(e -> e.getRecords().stream());
    }

    @Nullable
    public TalentRecord getNodeRecord(String lineName, int index) {
        TalentLineRecord lineRecord = getLineRecord(lineName);
        if (lineRecord == null)
            return null;
        return lineRecord.getRecord(index);
    }

    @Nullable
    private TalentLineRecord getLineRecord(String lineName) {
        return lines.computeIfAbsent(lineName, this::createMissingLine);
    }

    private boolean pointMotionCheck(TalentRecord record, int amount) {
        TalentNode node = record.getNode();
        String lineName = node.getLine().getName();
        int index = node.getIndex();

        TalentLineRecord lineRecord = getLineRecord(lineName);
        if (lineRecord == null) {
            MKCore.LOGGER.error("pointMotionCheck({}, {}, {}) - line does not exist", lineName, index, amount);
            return false;
        }

        if (index >= lineRecord.getLength()) {
            MKCore.LOGGER.error("pointMotionCheck({}, {}, {}) - index out of range (max {})", lineName, index, amount, lineRecord.getLength());
            return false;
        }

        if (amount > 0) {
            // trying to add
            if (index != 0) {
                TalentRecord previous = lineRecord.getRecord(index - 1);
                if (!previous.isKnown()) {
                    MKCore.LOGGER.error("pointMotionCheck({}, {}, {}) - cannot learn talent if the previous is unknown", lineName, index, amount);
                    return false;
                }
            }

            return record.getRank() < node.getMaxRanks();
        } else if (amount < 0) {
            // trying to remove
            TalentRecord next = lineRecord.getRecord(index + 1);
            if (next != null && next.isKnown() && record.getRank() <= 1) {
                MKCore.LOGGER.error("pointMotionCheck({}, {}, {}) - cannot unlearn talent if children have points", lineName, index, amount);
                return false;
            }

            return record.getRank() > 0;
        }

        return false;
    }

    private boolean modifyPoint(TalentRecord record, int points) {
        if (record.modifyRank(points)) {
            TalentNode node = record.getNode();
            updater.markUpdated(node.getLine().getName(), node.getIndex());
            return true;
        }
        return false;
    }

    public boolean trySpendPoint(String line, int index) {
        MKCore.LOGGER.info("trySpendPoint({}, {})", line, index);
        TalentRecord record = getNodeRecord(line, index);
        if (record == null)
            return false;

        int motion = 1;
        if (pointMotionCheck(record, motion)) {
            return modifyPoint(record, motion);
        }

        return false;
    }

    public boolean tryRefundPoint(String line, int index) {
        MKCore.LOGGER.info("tryRefundPoint({}, {})", line, index);
        TalentRecord record = getNodeRecord(line, index);
        if (record == null || !record.isKnown())
            return false;

        int motion = -1;
        if (pointMotionCheck(record, motion)) {
            return modifyPoint(record, motion);
        }
        return false;
    }

    public <T> T serialize(DynamicOps<T> ops) {
        ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        builder.put(ops.createString("version"), ops.createInt(tree.getVersion()));
        builder.put(
                ops.createString("lines"),
                ops.createMap(
                        lines.entrySet()
                                .stream()
                                .collect(Collectors.toMap(
                                        e -> ops.createString(e.getKey()),
                                        e -> e.getValue().serialize(ops)))
                )
        );
        return ops.createMap(builder.build());
    }

    public <T> boolean deserialize(Dynamic<T> dynamic) {

        int version = dynamic.get("version").asInt(-1);
        if (version == -1 || version != tree.getVersion()) {
            return false;
        }

        dynamic.get("lines")
                .asMap(Dynamic::asString, Function.identity())
                .forEach((nameOpt, dyn) -> nameOpt.ifPresent(name -> deserializeLineRecord(name, dyn)));
        return true;
    }

    private <T> void deserializeLineRecord(String name, Dynamic<T> dyn) {
//        MKCore.LOGGER.info("TalentTreeRecord.deserializeLineRecord line {} {}", name, dyn);
        TalentLineRecord lineRecord = getLineRecord(name);
        if (lineRecord == null) {
            MKCore.LOGGER.error("TalentTreeRecord.deserializeLineRecord line {} - line does not exist!", name);
            return;
        }

        lineRecord.deserialize(dyn);
    }

    private TalentLineRecord createMissingLine(String name) {
        TalentTreeDefinition.TalentLineDefinition lineDef = tree.getLine(name);
        if (lineDef != null) {
            return new TalentLineRecord(lineDef);
        }
        return null;
    }

    static class TalentLineRecord {
        private final TalentTreeDefinition.TalentLineDefinition lineDefinition;
        private final List<TalentRecord> lineRecords;

        public TalentLineRecord(TalentTreeDefinition.TalentLineDefinition lineDefinition) {
            this.lineDefinition = lineDefinition;
            this.lineRecords = lineDefinition.getNodes()
                    .stream()
                    .map(TalentNode::createRecord)
                    .collect(Collectors.toList());
        }

        public TalentRecord getRecord(int index) {
            if (index < lineRecords.size()) {
                return lineRecords.get(index);
            }
            return null;
        }

        public List<TalentRecord> getRecords() {
            return Collections.unmodifiableList(lineRecords);
        }

        public int getLength() {
            return lineDefinition.getLength();
        }

        public TalentTreeDefinition.TalentLineDefinition getLineDefinition() {
            return lineDefinition;
        }


        public <T> T serialize(DynamicOps<T> ops) {
            return ops.createList(lineRecords.stream().map(record -> record.serialize(ops)));
        }

        public <T> void deserialize(Dynamic<T> dynamic) {
//            MKCore.LOGGER.info("TalentLineRecord.deserialize {}", dynamic);
            List<Dynamic<T>> entries = dynamic.asList(Function.identity());
            IntStream.range(0, entries.size()).forEach(i -> {
//                MKCore.LOGGER.info("TalentLineRecord.deserialize entry {}", entries.get(i));
                getRecord(i).deserialize(entries.get(i));
            });
        }
    }

    private static class TalentTreeUpdater implements ISyncObject {
        private final TalentTreeRecord treeRecord;
        private final Multimap<String, Integer> updated = MultimapBuilder.hashKeys().hashSetValues().build();
        private ISyncNotifier parentNotifier = ISyncNotifier.NONE;
        private Consumer<CompoundNBT> deserializationCallback;

        public TalentTreeUpdater(TalentTreeRecord record) {
            this.treeRecord = record;
        }

        public void markUpdated(String lineName, int index) {
            updated.put(lineName, index);
            parentNotifier.notifyUpdate(this);
        }

        @Override
        public void setNotifier(ISyncNotifier notifier) {
            parentNotifier = notifier;
        }

        @Override
        public boolean isDirty() {
            return updated.size() > 0;
        }

        @Override
        public void deserializeUpdate(CompoundNBT tag) {
            CompoundNBT root = tag.getCompound(treeRecord.getTreeDefinition().getTreeId().toString());

//            MKCore.LOGGER.info("TalentTreeUpdater.deserialize {}", tag);

            if (root.getBoolean("f")) {
                treeRecord.lines.clear();
            }

            if (root.contains("u")) {
                CompoundNBT updated = root.getCompound("u");

                for (String line : updated.keySet()) {
                    TalentLineRecord lineRecord = treeRecord.getLineRecord(line);
                    if (lineRecord == null) {
                        MKCore.LOGGER.warn("TalentTreeUpdater.deserializeUpdate unknown line {}", line);
                        continue;
                    }
                    updated.getList(line, Constants.NBT.TAG_COMPOUND).forEach(nbt -> {
                        int index = ((CompoundNBT) nbt).getInt("i");
                        lineRecord.getRecord(index).deserialize(new Dynamic<>(NBTDynamicOps.INSTANCE, nbt));
                    });
                }
            }

            if (deserializationCallback != null)
                deserializationCallback.accept(root);
        }

        private CompoundNBT writeNode(TalentRecord rec) {
            CompoundNBT recTag = (CompoundNBT) rec.serialize(NBTDynamicOps.INSTANCE);
            recTag.putInt("i", rec.getNode().getIndex());
            return recTag;
        }

        @Override
        public void serializeUpdate(CompoundNBT tag) {
            CompoundNBT root = new CompoundNBT();

            CompoundNBT updateTag = new CompoundNBT();
            updated.keySet().forEach(line -> updateTag.put(line, new ListNBT()));
            updated.entries().forEach(kv -> {
                TalentLineRecord lineRecord = treeRecord.getLineRecord(kv.getKey());
                if (lineRecord == null) {
                    return;
                }
                TalentRecord rec = lineRecord.getRecord(kv.getValue());
                CompoundNBT recTag = writeNode(rec);
                updateTag.getList(kv.getKey(), Constants.NBT.TAG_COMPOUND).add(recTag);
            });

            root.put("u", updateTag);
            tag.put(treeRecord.getTreeDefinition().getTreeId().toString(), root);

//            MKCore.LOGGER.info("TalentTreeUpdater.serializeUpdate {}", tag);

            updated.clear();
        }

        @Override
        public void serializeFull(CompoundNBT tag) {
            CompoundNBT root = new CompoundNBT();
            root.putBoolean("f", true);

            CompoundNBT updateTag = new CompoundNBT();

            treeRecord.lines.keySet().forEach(line -> updateTag.put(line, new ListNBT()));

            for (TalentLineRecord line : treeRecord.lines.values()) {
                String lineName = line.getLineDefinition().getName();
                ListNBT list = line.getRecords().stream()
                        .map(this::writeNode)
                        .collect(Collectors.toCollection(ListNBT::new));

                updateTag.put(lineName, list);
            }

            root.put("u", updateTag);
            tag.put(treeRecord.getTreeDefinition().getTreeId().toString(), root);

//            MKCore.LOGGER.info("TalentTreeUpdater.serializeFull {}", tag);

            updated.clear();
        }
    }
}
