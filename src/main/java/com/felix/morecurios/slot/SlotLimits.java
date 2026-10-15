package com.felix.morecurios.slot;

import com.felix.morecurios.config.FelixMoreCuriosConfig;
import com.felix.morecurios.config.FelixMoreCuriosConfig.LimitMode;

import java.util.EnumMap;
import java.util.Map;

/**
 * Per-player, per-slot-type crystal use limits.
 *
 * Resolution order for a crystal (first match wins):
 *   1. limits.per_slot.<slot> = a number          -> that exact limit
 *   2. limits.per_slot.<slot> = "default"         -> {@link #DEFAULT_LIMIT} (fixed, 2)
 *      limits.per_slot.<slot> = "global"          -> the limits.global value
 *   3. limits.per_slot.<slot> = "auto" (the initial value) -> decided by limits.mode
 *      (see {@link #getModeLimit}):
 *        BALANCED        : value from {@link #BUILT_IN_LIMITS}, else DEFAULT_LIMIT
 *        BALANCED_GLOBAL : value from {@link #BUILT_IN_LIMITS}, else limits.global
 *        ALL_GLOBAL      : limits.global      (BUILT_IN_LIMITS is ignored)
 *        ALL_DEFAULT     : DEFAULT_LIMIT      (BUILT_IN_LIMITS and limits.global are ignored)
 *
 * Lowering a limit never removes slots a player already unlocked and never touches
 * the player's counter; it only blocks further use while counter >= limit.
 */
public final class SlotLimits {

    /** Fixed value of the "default" keyword, and of the non-balanced slots in BALANCED / ALL_DEFAULT. */
    public static final int DEFAULT_LIMIT = 2;

    /** Initial value of limits.global, and the global limit used whenever the config is not loaded yet. */
    public static final int DEFAULT_GLOBAL_LIMIT = 2;

    // =====================================================================================
    //  >>> EDITE AQUI <<<
    //
    //  Limite BALANCEADO de cristais por jogador para cada tipo de slot (o balanceamento do autor).
    //
    //  Esta tabela só vale nos modos BALANCED e BALANCED_GLOBAL (limits.mode) e para slots que
    //  estão em "auto" no config. Nos modos ALL_GLOBAL e ALL_DEFAULT ela é ignorada por completo
    //  (inclusive os 0).
    //
    //  Slots que NÃO estão listados aqui usam:
    //    - BALANCED        -> DEFAULT_LIMIT (2)
    //    - BALANCED_GLOBAL -> o valor de limits.global
    //
    //  Para mudar a quantidade de um slot, altere o número. Para adicionar outro slot, copie
    //  uma linha e troque o tipo (os nomes são os do enum CurioSlotType). 0 = cristal desativado.
    //
    //  Estes valores são só o PADRÃO: quem escrever um valor em [limits.per_slot] do arquivo de
    //  config (número, "default" ou "global") continua tendo prioridade.
    // =====================================================================================
    private static final Map<CurioSlotType, Integer> BUILT_IN_LIMITS = new EnumMap<>(CurioSlotType.class);

    static {
        BUILT_IN_LIMITS.put(CurioSlotType.NECKLACE, 3);
        BUILT_IN_LIMITS.put(CurioSlotType.HANDS, 1);
        BUILT_IN_LIMITS.put(CurioSlotType.BELT, 1);
        BUILT_IN_LIMITS.put(CurioSlotType.CHARM, 3);
        BUILT_IN_LIMITS.put(CurioSlotType.DEEP_LEARNER, 1);
        BUILT_IN_LIMITS.put(CurioSlotType.ADV_PATTERN_ENCODER, 0);
        BUILT_IN_LIMITS.put(CurioSlotType.HEART_AMULET, 0);
        BUILT_IN_LIMITS.put(CurioSlotType.PSD, 1);
        //BUILT_IN_LIMITS.put(CurioSlotType.REFINED_STORAGE, 0);

    }

    private SlotLimits() {
    }

    /** The limits.global value (0 or more). Falls back to {@link #DEFAULT_GLOBAL_LIMIT} if the config is not loaded. */
    public static int getGlobalLimit() {
        try {
            return Math.max(0, FelixMoreCuriosConfig.GLOBAL_LIMIT.get());
        } catch (IllegalStateException notLoaded) {
            return DEFAULT_GLOBAL_LIMIT;
        }
    }

    /** The active limits.mode. Falls back to BALANCED if the config is not loaded. */
    public static LimitMode getMode() {
        try {
            return FelixMoreCuriosConfig.LIMIT_MODE.get();
        } catch (IllegalStateException notLoaded) {
            return LimitMode.BALANCED;
        }
    }

    /**
     * The limit for this slot according to limits.mode alone, i.e. what a slot set to "auto" gets.
     * (Replaces the old getDefaultLimit; "default" is now a fixed value, see {@link #DEFAULT_LIMIT}.)
     */
    public static int getModeLimit(CurioSlotType type) {
        Integer balanced = BUILT_IN_LIMITS.get(type);
        return switch (getMode()) {
            case ALL_DEFAULT -> DEFAULT_LIMIT;
            case ALL_GLOBAL -> getGlobalLimit();
            case BALANCED_GLOBAL -> balanced != null ? Math.max(0, balanced) : getGlobalLimit();
            case BALANCED -> balanced != null ? Math.max(0, balanced) : DEFAULT_LIMIT;
        };
    }

    /** Effective limit for this crystal. 0 means the crystal is disabled. */
    public static int getLimit(CurioSlotType type) {
        String raw;
        try {
            raw = FelixMoreCuriosConfig.SLOT_LIMITS.get(type).get().trim();
        } catch (IllegalStateException notLoaded) {
            return getModeLimit(type);
        }
        if (FelixMoreCuriosConfig.isDefaultKeyword(raw)) {
            return DEFAULT_LIMIT;
        }
        if (FelixMoreCuriosConfig.isGlobalKeyword(raw)) {
            return getGlobalLimit();
        }
        if (!FelixMoreCuriosConfig.isAutoKeyword(raw)) {
            try {
                return Math.max(0, Integer.parseInt(raw));
            } catch (NumberFormatException ignored) {
                // validator should prevent this; fall through to the mode
            }
        }
        return getModeLimit(type);
    }
}