package com.felix.morecurios.event;

import com.felix.morecurios.FelixMoreCurios;
import com.felix.morecurios.config.FelixMoreCuriosConfig;
import com.felix.morecurios.network.ModNetwork;
import com.felix.morecurios.slot.CuriosSlots;
import com.felix.morecurios.slot.PlayerSlotData;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Integrates MoreCurios with resets.
 *
 * - '/curios reset <players>' : when config reset.behavior = RESTORE_COUNTERS the counters of the
 *   targeted players are zeroed too. It listens ONLY to that command, so death, respawn,
 *   dimension change or any data refresh never touch the counters.
 * - '/morecurios reset <players>'         : removes the extra slots AND zeroes the counters (one operation).
 * - '/morecurios resetcounters <players>' : zeroes only the counters.
 *
 * Registered on the Forge event bus (default of @Mod.EventBusSubscriber).
 */
@Mod.EventBusSubscriber(modid = FelixMoreCurios.MODID)
public final class CuriosResetHandler {

    private static final Pattern CURIOS_RESET =
            Pattern.compile("^/?curios\\s+reset(\\s.*)?$", Pattern.CASE_INSENSITIVE);

    private CuriosResetHandler() {
    }

    @SubscribeEvent
    public static void onCommand(CommandEvent event) {
        if (FelixMoreCuriosConfig.RESET_BEHAVIOR.get() != FelixMoreCuriosConfig.ResetBehavior.RESTORE_COUNTERS) {
            return;
        }
        ParseResults<CommandSourceStack> parse = event.getParseResults();
        // Ignore commands that failed to parse or that the sender is not allowed to run.
        if (!parse.getExceptions().isEmpty() || parse.getReader().canRead()) {
            return;
        }
        if (!CURIOS_RESET.matcher(parse.getReader().getString().trim()).matches()) {
            return;
        }

        CommandContextBuilder<CommandSourceStack> ctx = parse.getContext().getLastChild();
        for (ParsedArgument<CommandSourceStack, ?> arg : ctx.getArguments().values()) {
            if (arg.getResult() instanceof EntitySelector selector) {
                try {
                    for (ServerPlayer target : selector.findPlayers(ctx.getSource())) {
                        PlayerSlotData.clearAll(target);
                        ModNetwork.sync(target);
                    }
                } catch (CommandSyntaxException ignored) {
                    // The original command will report the same error to the sender.
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("morecurios")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reset")
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes(ctx -> run(ctx, true))))
                .then(Commands.literal("resetcounters")
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes(ctx -> run(ctx, false)))));
    }

    private static int run(CommandContext<CommandSourceStack> ctx, boolean resetSlots) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");
        for (ServerPlayer player : players) {
            if (resetSlots) {
                CuriosSlots.resetInventory(player);
            }
            PlayerSlotData.clearAll(player);
            ModNetwork.sync(player);
        }
        int count = players.size();
        ctx.getSource().sendSuccess(() -> Component.translatable(
                resetSlots ? "commands.felixmorecurios.reset" : "commands.felixmorecurios.reset_counters", count), true);
        return count;
    }
}
