package com.almostreliable.summoningrituals.recipe.output;

import com.almostreliable.summoningrituals.SummoningRituals;
import com.almostreliable.summoningrituals.core.Config;
import com.almostreliable.summoningrituals.core.Constants;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record CommandOutput(List<String> commands, List<Component> tooltip, boolean requiresPlayer) {

    public static final Codec<CommandOutput> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.STRING.listOf().fieldOf(Constants.COMMANDS).forGetter(CommandOutput::commands),
        ComponentSerialization.CODEC.listOf().optionalFieldOf(Constants.TOOLTIP, List.of()).forGetter(CommandOutput::tooltip),
        Codec.BOOL.optionalFieldOf(Constants.REQUIRES_PLAYER, true).forGetter(CommandOutput::requiresPlayer)
    ).apply(i, CommandOutput::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, CommandOutput> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), CommandOutput::commands,
        ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list()), CommandOutput::tooltip,
        ByteBufCodecs.BOOL, CommandOutput::requiresPlayer,
        CommandOutput::new
    );
    public static final CommandOutput EMPTY = new CommandOutput(List.of(), List.of(), true);

    public CommandOutput(List<String> commands, List<Component> tooltip) {
        this(commands, tooltip, true);
    }

    public CommandOutput(List<String> commands) {
        this(commands, List.of(), true);
    }

    public CommandOutput(String command) {
        this(List.of(command), List.of(), true);
    }

    public void invoke(ServerLevel level, @Nullable ServerPlayer player) {
        var commandSourceStack = level.getServer().createCommandSourceStack();
        if (requiresPlayer) {
            if (player == null) return;
            commandSourceStack = commandSourceStack.withEntity(player);
        }
        for (var command : commands) {
            if (Config.COMMON.announceCommand.get()) {
                SummoningRituals.LOGGER.info("Executing ritual output command: {}", command);
            }
            level.getServer().getCommands().performPrefixedCommand(commandSourceStack, command);
        }
    }

    public List<Component> getTooltip() {
        var tooltips = new ArrayList<Component>();
        if (tooltip.isEmpty()) {
            for (var command : commands) {
                tooltips.add(Component.literal("- ").append(command));
            }
        } else {
            tooltips.addAll(tooltip);
        }
        return tooltips;
    }
}
