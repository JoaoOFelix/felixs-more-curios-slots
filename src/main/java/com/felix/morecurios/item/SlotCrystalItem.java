package com.felix.morecurios.item;

import com.felix.morecurios.client.ClientUsedCache;
import com.felix.morecurios.config.FelixMoreCuriosConfig;
import com.felix.morecurios.network.ModNetwork;
import com.felix.morecurios.slot.CrystalAvailability;
import com.felix.morecurios.slot.CurioSlotType;
import com.felix.morecurios.slot.CuriosSlots;
import com.felix.morecurios.slot.PlayerSlotData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Right-click to permanently unlock +1 Curios slot of a given type (limited per player). */
public class SlotCrystalItem extends Item {

    private final CurioSlotType type;

    public SlotCrystalItem(CurioSlotType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public CurioSlotType getSlotType() {
        return type;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // All the logic (and the per-player data) lives on the server.
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.consume(stack);
        }

        Component slotName = type.getDisplayName();

        // 1) Crystal not available in this environment (mod / slot not registered / no item uses it).
        if (!CrystalAvailability.isAvailable(type, false)) {
            return fail(serverPlayer, stack, "crystal_unavailable", slotName);
        }

        int max = type.getMaxExtraSlots();
        int used = PlayerSlotData.getUsed(serverPlayer, type);

        // 2) Limit 0 -> crystal disabled by the config.
        if (max <= 0) {
            return fail(serverPlayer, stack, "crystal_disabled", slotName);
        }

        // 3) Limit reached (also covers a limit lowered below the player's counter).
        if (used >= max) {
            return fail(serverPlayer, stack, "limit_reached", slotName, max);
        }

        // 4) Grow the slot. Nothing is consumed unless this succeeds.
        boolean requireExisting = FelixMoreCuriosConfig.REQUIRE_EXISTING_SLOT.get();
        CuriosSlots.GrowResult result = CuriosSlots.growSlot(serverPlayer, type.getSlotId(), 1, requireExisting);
        switch (result) {
            case SUCCESS -> { /* continue below */ }
            case SLOT_MISSING -> {
                return fail(serverPlayer, stack, "slot_missing", slotName);
            }
            case SLOT_NOT_REGISTERED -> {
                return fail(serverPlayer, stack, "crystal_unavailable", slotName);
            }
            case NOT_ASSIGNED -> {
                return fail(serverPlayer, stack, "slot_not_assigned", slotName);
            }
            default -> {
                return fail(serverPlayer, stack, "slot_unavailable", slotName);
            }
        }

        int newUsed = used + 1;
        PlayerSlotData.setUsed(serverPlayer, type, newUsed);
        ModNetwork.sync(serverPlayer);

        if (!serverPlayer.getAbilities().instabuild) {
            stack.shrink(1);
        }
        serverPlayer.awardStat(Stats.ITEM_USED.get(this));
        serverPlayer.getCooldowns().addCooldown(this, 10);

        level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7F, 1.4F);
        ((ServerLevel) level).sendParticles(ParticleTypes.ENCHANT,
                serverPlayer.getX(), serverPlayer.getY() + 1.0D, serverPlayer.getZ(),
                40, 0.5D, 0.7D, 0.5D, 0.5D);

        serverPlayer.displayClientMessage(
                Component.translatable("message.felixmorecurios.slot_unlocked", slotName, newUsed, max)
                        .withStyle(ChatFormatting.GREEN),
                true);

        return InteractionResultHolder.success(stack);
    }

    /** Sends a red error message and returns FAIL (the stack is left untouched). */
    private static InteractionResultHolder<ItemStack> fail(ServerPlayer player, ItemStack stack,
                                                           String key, Object... args) {
        player.displayClientMessage(
                Component.translatable("message.felixmorecurios." + key, args).withStyle(ChatFormatting.RED),
                false);
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.felixmorecurios.use", type.getDisplayName())
                .withStyle(ChatFormatting.GRAY));

        int max = type.getMaxExtraSlots();


        if (!CrystalAvailability.isAvailable(type, true)) {
            tooltip.add(Component.translatable("tooltip.felixmorecurios.unavailable")
                    .withStyle(ChatFormatting.RED));
        } else {
            if (max <= 0) {
                tooltip.add(Component.translatable("tooltip.felixmorecurios.disabled")
                        .withStyle(ChatFormatting.RED));
            } else {
                tooltip.add(Component.translatable("tooltip.felixmorecurios.limit", max)
                        .withStyle(ChatFormatting.DARK_AQUA));

                tooltip.add(Component.translatable("tooltip.felixmorecurios.used",
                                ClientUsedCache.get(type), max)
                        .withStyle((ClientUsedCache.get(type) >= max) ? ChatFormatting.RED : ChatFormatting.AQUA));

            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}