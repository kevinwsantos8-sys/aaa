package com.shadowkatana.network;

import com.shadowkatana.ShadowKatanaMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public final class Payloads {

    /** Cliente -> servidor. 0 = sacar/guardar, 1 = teleporte, 2 = invisibilidade, 3/4/5 = upgrades. */
    public record Action(int id) implements CustomPayload {
        public static final CustomPayload.Id<Action> ID = new CustomPayload.Id<>(Identifier.of(ShadowKatanaMod.MOD_ID, "action"));
        public static final PacketCodec<RegistryByteBuf, Action> CODEC =
                PacketCodec.tuple(PacketCodecs.VAR_INT, Action::id, Action::new);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    /** Servidor -> clientes: fase da katana de um jogador. */
    public record Phase(int entityId, int phase) implements CustomPayload {
        public static final CustomPayload.Id<Phase> ID = new CustomPayload.Id<>(Identifier.of(ShadowKatanaMod.MOD_ID, "phase"));
        public static final PacketCodec<RegistryByteBuf, Phase> CODEC =
                PacketCodec.tuple(PacketCodecs.VAR_INT, Phase::entityId, PacketCodecs.VAR_INT, Phase::phase, Phase::new);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    /** Servidor -> dono: recargas restantes em ticks. */
    public record Cooldown(int teleport, int shadow) implements CustomPayload {
        public static final CustomPayload.Id<Cooldown> ID = new CustomPayload.Id<>(Identifier.of(ShadowKatanaMod.MOD_ID, "cooldown"));
        public static final PacketCodec<RegistryByteBuf, Cooldown> CODEC =
                PacketCodec.tuple(PacketCodecs.VAR_INT, Cooldown::teleport, PacketCodecs.VAR_INT, Cooldown::shadow, Cooldown::new);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    private Payloads() {}
}
