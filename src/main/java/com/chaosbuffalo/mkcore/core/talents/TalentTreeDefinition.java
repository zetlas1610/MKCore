package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.function.Function;

public class TalentTreeDefinition {

    private final Map<String, TalentLineDefinition> talentLines = new HashMap<>();
    private final ResourceLocation treeId;
    private final int version;

    public TalentTreeDefinition(ResourceLocation name, int version) {
        treeId = name;
        this.version = version;
    }

    public ResourceLocation getTreeId() {
        return treeId;
    }

    public int getVersion() {
        return version;
    }

    public Map<String, TalentLineDefinition> getTalentLines() {
        return Collections.unmodifiableMap(talentLines);
    }

    public String getName() {
        return I18n.format(String.format("%s.%s.name", treeId.getNamespace(), treeId.getPath()));
    }

    public TalentLineDefinition getLine(String name) {
        return talentLines.get(name);
    }

    public boolean hasLine(String name) {
        return talentLines.containsKey(name);
    }

    public boolean containsIndex(String lineName, int index) {
        return getNode(lineName, index) != null;
    }

    public TalentNode getNode(String lineName, int index) {
        TalentLineDefinition line = getLine(lineName);
        if (line == null)
            return null;
        return line.getNode(index);
    }

    private void addLine(TalentLineDefinition line) {
        talentLines.put(line.getName(), line);
    }

    public TalentTreeRecord createRecord() {
        return new TalentTreeRecord(this);
    }

    public static <T> TalentTreeDefinition deserialize(ResourceLocation treeId, Dynamic<T> dynamic) {
        int version = dynamic.get("version").asInt(1);

//        MKCore.LOGGER.info("TalentTree.deserialize {} {}", treeId, version);

        TalentTreeDefinition tree = new TalentTreeDefinition(treeId, version);
        dynamic.get("lines").asList(d -> TalentLineDefinition.deserialize(tree, d)).forEach(tree::addLine);

//        MKCore.LOGGER.info("TalentTree.deserialize lines {} {}", treeId, tree.talentLines.size());

        return tree;
    }

    public <T> T serialize(DynamicOps<T> ops) {
        ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        builder.put(ops.createString("version"), ops.createInt(getVersion()));
        builder.put(ops.createString("lines"), ops.createList(talentLines.values().stream().map(d -> d.serialize(ops))));
        return ops.createMap(builder.build());
    }


    public static class TalentLineDefinition {
        private final TalentTreeDefinition tree;
        private final String name;
        private final List<TalentNode> nodes;

        public TalentLineDefinition(TalentTreeDefinition tree, String name) {
            this.tree = tree;
            this.name = name;
            nodes = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public TalentTreeDefinition getTree() {
            return tree;
        }

        public int getLength() {
            return nodes.size();
        }

        public TalentNode getNode(int index) {
            if (index < nodes.size()) {
                return nodes.get(index);
            }
            return null;
        }

        private void addNode(TalentNode node) {
            node.link(this, nodes.size());
            nodes.add(node);
        }

        public List<TalentNode> getNodes() {
            return Collections.unmodifiableList(nodes);
        }

        public static <T> TalentLineDefinition deserialize(TalentTreeDefinition tree, Dynamic<T> dynamic) {

            Optional<String> nameOpt = dynamic.get("name").asString();
            if (!nameOpt.isPresent())
                return null;

            TalentLineDefinition line = new TalentLineDefinition(tree, nameOpt.get());

//            MKCore.LOGGER.info("TalentLine.deserialize {}", line.name);

            List<Dynamic<T>> rawNodes = dynamic.get("talents").asList(Function.identity());
            rawNodes.forEach(talent -> {
                TalentNode node = line.deserializeNode(talent);
                if (node == null) {
                    MKCore.LOGGER.error("Stopping parsing talent line {} at index {} because it failed to deserialize", line.getName(), line.getNodes().size());
                    return;
                }

                line.addNode(node);
            });

//            MKCore.LOGGER.info(" nodes {} valid {}", rawNodes.size(), line.getLength());

            return line;
        }

        <T> TalentNode deserializeNode(Dynamic<T> entry) {
            Optional<String> nameOpt = entry.get("name").asString();
            if (!nameOpt.isPresent()) {
                MKCore.LOGGER.error("Tried to deserialize talent without a name!");
                return null;
            }

            ResourceLocation nodeType = new ResourceLocation(nameOpt.get());
            BaseTalent talentType = MKCoreRegistry.TALENT_TYPES.getValue(nodeType);
            if (talentType == null) {
                MKCore.LOGGER.error(String.format("Tried to deserialize talent node that referenced unknown talent type %s", nodeType));
                return null;
//                throw new IllegalArgumentException(String.format("Tried to deserialize talent that referenced unknown talent type %s", nodeType));
            }

            return talentType.createNode(entry);
        }

        public <T> T serialize(DynamicOps<T> ops) {
            ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
            builder.put(ops.createString("name"), ops.createString(name));
            builder.put(ops.createString("talents"), ops.createList(nodes.stream().map(n -> n.serialize(ops))));
            return ops.createMap(builder.build());
        }
    }
}
