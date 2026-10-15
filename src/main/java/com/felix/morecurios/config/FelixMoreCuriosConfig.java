package com.felix.morecurios.config;

import com.felix.morecurios.slot.CurioSlotType;
import com.felix.morecurios.slot.SlotLimits;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.EnumMap;
import java.util.Map;

public class FelixMoreCuriosConfig {

    /** O que fazer com os contadores de uso quando o /curios reset é executado. */
    public enum ResetBehavior {
        KEEP_COUNTERS,      //Mantém o contador de usos
        RESTORE_COUNTERS    //Reinicia os usos, para que após usar o comando reinicie a contagem de usos do cristal (padrao)
    }

    /**
     * Como o limite de cada cristal é decidido quando o valor do slot em [limits.per_slot] é "auto".
     * Um valor escrito no slot (número, "default" ou "global") sempre vence o modo.
     */
    public enum LimitMode {
        /** Limites balanceados pelo autor; slots sem balanceamento usam o "default" (2). A variável global é ignorada. (padrão) */
        BALANCED,
        /** Limites balanceados pelo autor; slots sem balanceamento usam a variável global. */
        BALANCED_GLOBAL,
        /** Todos os cristais usam a variável global (ignora o balanceamento do autor). */
        ALL_GLOBAL,
        /** Todos os cristais usam o "default" (2), ignorando o balanceamento e a variável global. */
        ALL_DEFAULT
    }

    /** Regra para decidir se um cristal está disponível no ambiente instalado. */
    public enum AvailabilityMode {
        /** Sempre é possível usar os cristais */
        ALWAYS,
        /** Só exige o mod (quando o cristal tem um mod associado). */
        MOD_REGISTERED,
        /** O tipo de slot precisa estar registrado no Curios. */
        SLOT_REGISTERED,
        /** Registrado no Curios E pelo menos um item na tag curios:<slot>. */
        SLOT_REGISTERED_AND_USED
    }


    /** INDISPONIVEL DEVIDO A LIMITAÇÕES DO CURIOSAPI NA 1.20.1
     // Uso dos cristais mesmo sem um mod
     public enum SlotMode {
     // Sempre disponivel para uso
     ALWAYS,
     // Disponivel para uso apenas se algum mod que use o slot, estiver presente /
     ONLY_IF_MOD_LOADED,
     /** Slot personalizados de mod desativado
     DISABLED
     }
     */

    /** Valores de texto aceitos em [limits.per_slot] além de um número. */
    /** "default": limite fixo do mod (SlotLimits.DEFAULT_LIMIT = 2), independente do modo. */
    public static final String DEFAULT = "default";
    /** "global": usa a variável global (limits.global). */
    public static final String GLOBAL = "global";
    /** "auto": segue o limits.mode (valor inicial de todos os slots). */
    public static final String AUTO = "auto";

    /** True se o valor significa "default" (limite fixo de 2). */
    public static boolean isDefaultKeyword(String value) {
        return value.equalsIgnoreCase(DEFAULT);
    }

    /** True se o valor significa "usar a variável global". */
    public static boolean isGlobalKeyword(String value) {
        return value.equalsIgnoreCase(GLOBAL);
    }

    /** True se o valor significa "seguir o limits.mode". */
    public static boolean isAutoKeyword(String value) {
        return value.equalsIgnoreCase(AUTO);
    }

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue REQUIRE_EXISTING_SLOT;

    public static final ForgeConfigSpec.EnumValue<LimitMode> LIMIT_MODE;
    public static final ForgeConfigSpec.IntValue GLOBAL_LIMIT;

    public static final Map<CurioSlotType, ForgeConfigSpec.ConfigValue<String>> SLOT_LIMITS =
            new EnumMap<>(CurioSlotType.class);

    public static final ForgeConfigSpec.EnumValue<ResetBehavior> RESET_BEHAVIOR;
    public static final ForgeConfigSpec.EnumValue<AvailabilityMode> AVAILABILITY_MODE;
    //INFISPONIVEL DEVIDO A LIMITAÇÕES DO CURIOSAPI NA 1.20.1
    //public static final ForgeConfigSpec.EnumValue<SlotMode> SLOT_MODE;

    static {


        BUILDER.comment("Regras para desbloquear slots usando os cristais.")
                .push("unlocking");
        REQUIRE_EXISTING_SLOT = BUILDER
                .comment("TRUE: o jogador só pode usar o cristal se JÁ tiver pelo menos 1 slot desse tipo.",
                        "FALSE: o cristal também funciona com 0 slots do tipo, desde que o slot esteja na lista",
                        "do jogador. O mod já adiciona à lista do jogador os slots padrão do Curios (curio, charm,",
                        "head, back, necklace, ring, bracelet, belt, body, hands) com tamanho 0 inicial, via",
                        "datapack. Slots de OUTROS mods precisam ser atribuídos ao jogador por esses mods.",
                        "Em ambos os casos o cristal só é consumido quando o uso tem sucesso.")
                .define("require_existing_slot", false);
        BUILDER.pop();

        BUILDER.comment("Quantas vezes cada jogador pode usar cada cristal (modo, variável global e limites por slot).")
                .push("limits");
        LIMIT_MODE = BUILDER
                .comment("Como o limite de cada cristal é decidido quando o valor do slot em [limits.per_slot] é \"auto\":",
                        "BALANCED        = limites balanceados pelo autor; slots sem balanceamento usam o default (2).",
                        "BALANCED_GLOBAL = limites balanceados pelo autor; slots sem balanceamento usam o valor de 'global'.",
                        "ALL_GLOBAL      = todos os cristais usam o valor de 'global' (ignora o balanceamento do autor).",
                        "ALL_DEFAULT     = todos os cristais usam 2 (ignora o balanceamento e o valor de 'global').",
                        "Um valor escrito em [limits.per_slot] (número, \"default\" ou \"global\") sempre vence este modo.")
                .defineEnum("mode", LimitMode.BALANCED);

        GLOBAL_LIMIT = BUILDER
                .comment("Variável global: limite de usos de cristal por jogador. É usada quando:",
                        "- mode = ALL_GLOBAL (todos os cristais);",
                        "- mode = BALANCED_GLOBAL (cristais sem balanceamento do autor);",
                        "- um slot em [limits.per_slot] está como \"global\".",
                        "0 = cristal desativado para uso.")
                .defineInRange("global", SlotLimits.DEFAULT_GLOBAL_LIMIT, 0, 70);

        BUILDER.comment("Limite individual por tipo de slot. Valores aceitos:",
                "um número >= 0 = esse limite exato; vence tudo. 0 desativa o cristal daquele slot.",
                "\"default\"      = 2, fixo, independente do modo.",
                "\"global\"       = o valor da variável 'global' acima.",
                "\"auto\"         = segue o 'mode' acima (valor inicial).",
                "Reduzir um limite NÃO remove slots já desbloqueados nem apaga o contador do jogador;",
                "apenas impede novos usos enquanto contador >= limite.");
        BUILDER.push("per_slot");
        for (CurioSlotType type : CurioSlotType.values()) {
            SLOT_LIMITS.put(type, BUILDER
                    .comment("Limite do cristal do slot '" + type.getSlotId() + "': número >= 0 (0 desativa),",
                            "\"default\" (2), \"global\" (variável global) ou \"auto\" (segue o 'mode').")
                    .define(type.getSlotId(), AUTO, FelixMoreCuriosConfig::isValidLimitValue));
        }
        BUILDER.pop();
        BUILDER.pop();

        BUILDER.comment("O que acontece com os contadores de uso dos cristais quando os slots do Curios são resetados.")
                .push("reset");
        RESET_BEHAVIOR = BUILDER
                .comment("Reação ao comando '/curios reset <jogador>':",
                        "KEEP_COUNTERS    = os slots extras são removidos, mas os usos dos cristais continuam registrados.",
                        "RESTORE_COUNTERS = os contadores do MoreCurios também são zerados (cristais utilizáveis de novo).",
                        "O comando '/morecurios reset' sempre faz as duas coisas.")
                .defineEnum("behavior", ResetBehavior.RESTORE_COUNTERS);
        BUILDER.pop();


        BUILDER.comment("Quando os cristais ficam disponíveis para uso e na aba criativa.")
                .push("availability");
        AVAILABILITY_MODE = BUILDER
                .comment("Quando um cristal está disponível (uso e aba criativa):",
                        "ALWAYS                   = nada é exigido para usar os cristais.",
                        "MOD_REGISTERED           = apenas exige o mod associado, se houver.",
                        "SLOT_REGISTERED          = o tipo de slot precisa existir no Curios.",
                        "SLOT_REGISTERED_AND_USED = além disso, algum item precisa estar na tag curios:<slot>.",
                        "Obs.: itens que usam o slot só via capability (sem tag) não são detectados no último modo.")
                .defineEnum("availability mode", AvailabilityMode.ALWAYS);
        /*
        SLOT_MODE = BUILDER
                .comment("Sempre disponivel para uso",
                        "ALWAYS,",
                        "Disponivel para uso apenas se algum mod que use o slot, estiver presente",
                        "ONLY_IF_MOD_LOADED,",
                        "Slot personalizados de mod desativado",
                        "DISABLED")
                .defineEnum("slot mode", SlotMode.ALWAYS); */
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private static boolean isValidLimitValue(Object o) {
        if (!(o instanceof String s)) {
            return false;
        }
        s = s.trim();
        if (isDefaultKeyword(s) || isGlobalKeyword(s) || isAutoKeyword(s)) {
            return true;
        }
        try {
            return Integer.parseInt(s) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}