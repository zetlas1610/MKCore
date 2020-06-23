package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.talents.*;
import com.chaosbuffalo.mkcore.utils.TextUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TalentCommand {
    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("talent")
                .then(Commands.literal("points")
                        .then(Commands.literal("give")
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(TalentCommand::givePoints)))
                        .then(Commands.literal("take")
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(TalentCommand::takePoints)))
                        .executes(TalentCommand::showPoints)
                )
                .then(Commands.literal("learn")
                        .then(Commands.argument("tree", TalentTreeIdArgument.talent())
                                .then(Commands.argument("line", TalentLineIdArgument.talentLine())
                                        .then(Commands.argument("index", IntegerArgumentType.integer())
                                                .executes(TalentCommand::learnTalent))))
                )
                .then(Commands.literal("unlearn")
                        .then(Commands.argument("tree", TalentTreeIdArgument.talent())
                                .then(Commands.argument("line", TalentLineIdArgument.talentLine())
                                        .then(Commands.argument("index", IntegerArgumentType.integer())
                                                .executes(TalentCommand::unlearnTalent))))
                )
                .then(Commands.literal("tree")
                        .then(Commands.literal("list")
                                .executes(TalentCommand::listTrees))
                        .then(Commands.literal("unlock")
                                .then(Commands.argument("tree", TalentTreeIdArgument.talent())
                                        .executes(TalentCommand::unlockTree)))
                        .then(Commands.literal("line")
                                .then(Commands.argument("tree", TalentTreeIdArgument.talent())
                                        .then(Commands.argument("line", TalentLineIdArgument.talentLine())
                                                .executes(TalentCommand::listLine))))
                )
                .then(Commands.literal("passive")
                        .then(Commands.literal("list")
                                .then(Commands.literal("active")
                                        .executes(TalentCommand::listActivePassives))
                                .then(Commands.literal("known")
                                        .executes(TalentCommand::listKnownPassives))
                        )
                        .then(Commands.literal("set")
                                .then(Commands.argument("slot", IntegerArgumentType.integer(0, GameConstants.MAX_PASSIVES))
                                        .then(Commands.argument("talentId", makePassiveTalentId())
                                                .suggests(TalentCommand::suggestKnownPassives)
                                                .executes(TalentCommand::setPassive)))
                        )
                        .then(Commands.literal("clear")
                                .then(Commands.argument("slot", IntegerArgumentType.integer(0, GameConstants.MAX_PASSIVES))
                                        .executes(TalentCommand::clearPassive)))
                )
                .then(Commands.literal("ultimate")
                        .then(Commands.literal("list")
                                .then(Commands.literal("active")
                                        .executes(TalentCommand::listActiveUltimates))
                                .then(Commands.literal("known")
                                        .executes(TalentCommand::listKnownUltimates)))
                        .then(Commands.literal("set")
                                .then(Commands.argument("slot", IntegerArgumentType.integer(0, GameConstants.MAX_ULTIMATES))
                                        .then(Commands.argument("talentId", makeUltimateTalentId())
                                                .suggests(TalentCommand::suggestKnownUltimates)
                                                .executes(TalentCommand::setUltimate))))
                        .then(Commands.literal("clear")
                                .then(Commands.argument("slot", IntegerArgumentType.integer(0, GameConstants.MAX_ULTIMATES))
                                        .executes(TalentCommand::clearUltimate)))
                        .then(Commands.literal("info")
                                .executes(TalentCommand::ultimateInfo))
                        .then(Commands.literal("execute")
                                .then(Commands.argument("slot", IntegerArgumentType.integer(0, GameConstants.MAX_ULTIMATES))
                                        .executes(TalentCommand::executeUltimate)))
                )
                .then(Commands.literal("list")
                        .executes(TalentCommand::listTalents)
                );
    }

    static int listKnownTalents(CommandContext<CommandSource> ctx, TalentType<?> type) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        MKCore.getPlayer(player).ifPresent(cap -> {
            Set<ResourceLocation> knownTalents = cap.getKnowledge().getTalentKnowledge().getKnownTalentIds(type);
            if (knownTalents.size() > 0) {
                TextUtils.sendPlayerChatMessage(player, String.format("Known %s Talents", type.getName()));
                knownTalents.forEach(abilityId -> TextUtils.sendChatMessage(player, String.format("%s", abilityId)));
            } else {
                TextUtils.sendChatMessage(player, String.format("No known %s talents", type.getName()));
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int listActiveTalents(CommandContext<CommandSource> ctx, TalentType<?> type,
                                 Function<PlayerTalentKnowledge, List<ResourceLocation>> mapper) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talentKnowledge = cap.getKnowledge().getTalentKnowledge();
            List<ResourceLocation> knownTalents = mapper.apply(talentKnowledge);
            if (knownTalents.size() > 0) {
                TextUtils.sendPlayerChatMessage(player, String.format("Active %s Talent Abilities", type.getName()));
                for (int i = 0; i < knownTalents.size(); i++) {
                    ResourceLocation talent = knownTalents.get(i);
                    String msg = String.format("%d: %s", i, talent);
                    TextUtils.sendChatMessage(player, msg);
                }
            } else {
                TextUtils.sendChatMessage(player, String.format("No active %s talent abilities", type.getName()));
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int setTalent(CommandContext<CommandSource> ctx,
                         ToIntFunction<PlayerTalentKnowledge> limit,
                         Function<PlayerTalentKnowledge, BiConsumer<Integer, ResourceLocation>> setAction) throws CommandSyntaxException {
        ResourceLocation talentId = ctx.getArgument("talentId", ResourceLocation.class);
        return setTalentInternal(ctx, limit, setAction, talentId);
    }

    static int setTalentInternal(CommandContext<CommandSource> ctx,
                                 ToIntFunction<PlayerTalentKnowledge> slotLimitGetter,
                                 Function<PlayerTalentKnowledge, BiConsumer<Integer, ResourceLocation>> setAction,
                                 ResourceLocation talentId) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        int slot = IntegerArgumentType.getInteger(ctx, "slot");

        MKCore.getPlayer(player).ifPresent(playerData -> {
            PlayerTalentKnowledge talentKnowledge = playerData.getKnowledge().getTalentKnowledge();

            int limit = slotLimitGetter.applyAsInt(talentKnowledge);
            if (slot >= limit) {
                TextUtils.sendChatMessage(player, "Invalid slot");
                return;
            }

            setAction.apply(talentKnowledge).accept(slot, talentId);
        });

        return Command.SINGLE_SUCCESS;
    }

    static int clearTalent(CommandContext<CommandSource> ctx, ToIntFunction<PlayerTalentKnowledge> limit,
                           Function<PlayerTalentKnowledge, BiConsumer<Integer, ResourceLocation>> setAction) throws CommandSyntaxException {
        return setTalentInternal(ctx, limit, setAction, MKCoreRegistry.INVALID_TALENT);
    }

    static int listActivePassives(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        return listActiveTalents(ctx, TalentType.PASSIVE, PlayerTalentKnowledge::getActivePassives);
    }

    static int listKnownPassives(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        return listKnownTalents(ctx, TalentType.PASSIVE);
    }

    static int setPassive(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        return setTalent(ctx, PlayerTalentKnowledge::getAllowedActivePassiveCount, t -> t::setActivePassive);
    }

    static int clearPassive(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        return clearTalent(ctx, PlayerTalentKnowledge::getAllowedActivePassiveCount, t -> t::setActivePassive);
    }

    static int listActiveUltimates(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        return listActiveTalents(ctx, TalentType.ULTIMATE, PlayerTalentKnowledge::getActiveUltimates);
    }

    static int listKnownUltimates(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        return listKnownTalents(ctx, TalentType.ULTIMATE);
    }

    static int setUltimate(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        return setTalent(ctx, PlayerTalentKnowledge::getAllowedActiveUltimateCount, t -> t::setActiveUltimate);
    }

    static int clearUltimate(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        return clearTalent(ctx, PlayerTalentKnowledge::getAllowedActiveUltimateCount, t -> t::setActiveUltimate);
    }

    static int ultimateInfo(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talentKnowledge = cap.getKnowledge().getTalentKnowledge();
            TextUtils.sendPlayerChatMessage(player, "Ultimate Talent Info");

            TextUtils.sendChatMessage(player, String.format("Limit: %d", talentKnowledge.getAllowedActiveUltimateCount()));
        });

        return Command.SINGLE_SUCCESS;
    }

    static int executeUltimate(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        int slot = IntegerArgumentType.getInteger(ctx, "slot");

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talentKnowledge = cap.getKnowledge().getTalentKnowledge();

            if (slot >= talentKnowledge.getAllowedActiveUltimateCount()) {
                TextUtils.sendChatMessage(player, "Invalid slot");
                return;
            }

            ResourceLocation abilityId = talentKnowledge.getActiveUltimates().get(slot);
            cap.getAbilityExecutor().executeAbility(abilityId);
        });

        return Command.SINGLE_SUCCESS;
    }

    static int takePoints(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talentKnowledge = cap.getKnowledge().getTalentKnowledge();
            if (talentKnowledge.removeTalentPoints(amount)) {
                TextUtils.sendChatMessage(player, String.format("Removed %d points", amount));
            } else {
                TextUtils.sendChatMessage(player, String.format("Failed to remove %d points", amount));
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int givePoints(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talentKnowledge = cap.getKnowledge().getTalentKnowledge();
            if (talentKnowledge.grantTalentPoints(amount)) {
                TextUtils.sendChatMessage(player, String.format("Granted %d points", amount));
            } else {
                TextUtils.sendChatMessage(player, String.format("Failed to give %d points", amount));
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int showPoints(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talentKnowledge = cap.getKnowledge().getTalentKnowledge();
            int unspent = talentKnowledge.getUnspentTalentPoints();
            int total = talentKnowledge.getTotalTalentPoints();
            String msg = String.format("Talent Points: %d (%d unspent)", total, unspent);
            TextUtils.sendChatMessage(player, msg);
        });

        return Command.SINGLE_SUCCESS;
    }

    static int learnTalent(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        MKCore.LOGGER.info("TalentCommand in learnTalent");
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        ResourceLocation talentId = ctx.getArgument("tree", ResourceLocation.class);
        String line = StringArgumentType.getString(ctx, "line");
        int index = IntegerArgumentType.getInteger(ctx, "index");

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talentKnowledge = cap.getKnowledge().getTalentKnowledge();
            if (talentKnowledge.spendTalentPoint(talentId, line, index)) {
                TextUtils.sendChatMessage(player, String.format("Spent point in (%s, %s, %d)", talentId, line, index));
            } else {
                TextUtils.sendChatMessage(player, String.format("Failed to spend point in (%s, %s, %d)", talentId, line, index));
            }

        });

        return Command.SINGLE_SUCCESS;
    }

    static int unlearnTalent(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        ResourceLocation talentId = ctx.getArgument("tree", ResourceLocation.class);
        String line = StringArgumentType.getString(ctx, "line");
        int index = IntegerArgumentType.getInteger(ctx, "index");

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talentKnowledge = cap.getKnowledge().getTalentKnowledge();
            if (talentKnowledge.refundTalentPoint(talentId, line, index)) {
                TextUtils.sendChatMessage(player, String.format("Refund point in (%s, %s, %d)", talentId, line, index));
            } else {
                TextUtils.sendChatMessage(player, String.format("Failed to refund point in (%s, %s, %d)", talentId, line, index));
            }

        });

        return Command.SINGLE_SUCCESS;
    }

    static int unlockTree(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        ResourceLocation talentId = ctx.getArgument("tree", ResourceLocation.class);

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talentKnowledge = cap.getKnowledge().getTalentKnowledge();
            if (talentKnowledge.knowsTree(talentId)) {
                TextUtils.sendChatMessage(player, String.format("Tree %s already known", talentId));
                return;
            }

            if (talentKnowledge.unlockTree(talentId)) {
                TextUtils.sendChatMessage(player, String.format("Tree %s unlocked", talentId));
            } else {
                TextUtils.sendChatMessage(player, String.format("Failed to unlock tree %s", talentId));
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int listTrees(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talents = cap.getKnowledge().getTalentKnowledge();
            Collection<ResourceLocation> knownTalents = talents.getKnownTrees();
            if (knownTalents.size() > 0) {
                TextUtils.sendPlayerChatMessage(player, "Known Talent Trees");
                knownTalents.forEach(info -> TextUtils.sendChatMessage(player, String.format("%s", info)));
            } else {
                TextUtils.sendChatMessage(player, "You do not know any talent trees");
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int listLine(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        ResourceLocation treeId = ctx.getArgument("tree", ResourceLocation.class);
        String line = StringArgumentType.getString(ctx, "line");

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talentKnowledge = cap.getKnowledge().getTalentKnowledge();

            TalentTreeDefinition treeDefinition = MKCore.getTalentManager().getTalentTree(treeId);
            if (treeDefinition == null) {
                TextUtils.sendPlayerChatMessage(player, String.format("Tree %s does not exist", treeId));
                return;
            }

            TalentTreeDefinition.TalentLineDefinition lineDefinition = treeDefinition.getLine(line);
            if (lineDefinition == null) {
                TextUtils.sendPlayerChatMessage(player, String.format("Tree %s does not have line %s", treeId, line));
                return;
            }


            TextUtils.sendPlayerChatMessage(player, String.format("%s - %s", treeId, line));
            lineDefinition.getNodes().stream()
                    .sorted(Comparator.comparing(TalentNode::getPositionString))
                    .forEach(node -> {
                        String msg = describeNode(node, talentKnowledge.getRecord(treeId, line, node.getIndex()));
                        TextUtils.sendChatMessage(player, msg);
                    });
        });

        return Command.SINGLE_SUCCESS;
    }

    private static String describeNode(TalentNode node, TalentRecord record) {
        int rank = record != null ? record.getRank() : 0;
        return String.format("%d/%d %s - %s", rank, node.getMaxRanks(), node.getPositionString(), node.getTalent().getRegistryName());
    }

    static int listTalents(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talents = cap.getKnowledge().getTalentKnowledge();
            Collection<TalentRecord> knownTalents = talents.getKnownTalentsStream()
                    .sorted(Comparator.comparing(r -> r.getNode().getPositionString()))
                    .collect(Collectors.toList());
            if (knownTalents.size() > 0) {
                TextUtils.sendPlayerChatMessage(player, "Known Talents");
                knownTalents.forEach(info -> {
                    String msg = describeNode(info.getNode(), info);
                    TextUtils.sendChatMessage(player, msg);
                });
            } else {
                TextUtils.sendChatMessage(player, "No known talents");
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    public static class TalentTreeIdArgument implements ArgumentType<ResourceLocation> {

        public static TalentTreeIdArgument talent() {
            return new TalentTreeIdArgument();
        }

        @Override
        public ResourceLocation parse(final StringReader reader) throws CommandSyntaxException {
            return ResourceLocation.read(reader);
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context,
                                                                  final SuggestionsBuilder builder) {
            return ISuggestionProvider.suggest(MKCore.getTalentManager().getTreeNames().stream().map(ResourceLocation::toString), builder);
        }
    }

    public static class TalentLineIdArgument implements ArgumentType<String> {

        public static TalentLineIdArgument talentLine() {
            return new TalentLineIdArgument();
        }

        @Override
        public String parse(final StringReader reader) throws CommandSyntaxException {
            return reader.readString();
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context,
                                                                  final SuggestionsBuilder builder) {
            ResourceLocation treeId = context.getArgument("tree", ResourceLocation.class);

            TalentTreeDefinition treeDef = MKCore.getTalentManager().getTalentTree(treeId);
            if (treeDef != null) {
                return ISuggestionProvider.suggest(treeDef.getTalentLines().keySet(), builder);
            }

            return ISuggestionProvider.suggest(Collections.emptyList(), builder);
        }
    }

    static CompletableFuture<Suggestions> suggestKnownPassives(final CommandContext<CommandSource> context,
                                                               final SuggestionsBuilder builder) throws CommandSyntaxException {
        return suggestKnownTalents(context, builder, TalentType.PASSIVE);
    }

    static CompletableFuture<Suggestions> suggestKnownUltimates(final CommandContext<CommandSource> context,
                                                                final SuggestionsBuilder builder) throws CommandSyntaxException {
        return suggestKnownTalents(context, builder, TalentType.ULTIMATE);
    }


    public static TalentIdArgument makePassiveTalentId() {
        return new TalentIdArgument(TalentType.PASSIVE);
    }

    public static TalentIdArgument makeUltimateTalentId() {
        return new TalentIdArgument(TalentType.ULTIMATE);
    }

    public static class TalentIdArgument extends AbilityIdArgument {
        private final TalentType<?> type;

        public TalentIdArgument(TalentType<?> type) {
            this.type = type;
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
            Stream<String> all = MKCoreRegistry.TALENT_TYPES.getValues()
                    .stream()
                    .filter(talent -> talent.getTalentType() == type)
                    .map(ForgeRegistryEntry::getRegistryName)
                    .filter(Objects::nonNull)
                    .map(ResourceLocation::toString);

            return ISuggestionProvider.suggest(all, builder);
        }
    }

    static CompletableFuture<Suggestions> suggestKnownTalents(final CommandContext<CommandSource> context,
                                                              final SuggestionsBuilder builder, TalentType<?> type) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        return ISuggestionProvider.suggest(MKCore.getPlayer(player)
                        .map(playerData -> playerData.getKnowledge()
                                .getTalentKnowledge()
                                .getKnownTalentIds(type)
                                .stream()
                                .map(ResourceLocation::toString))
                        .orElse(Stream.empty()),
                builder);
    }
}
