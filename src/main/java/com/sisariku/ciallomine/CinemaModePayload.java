package com.sisariku.ciallomine;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CinemaModePayload(float horizontal, float vertical, float speed, boolean hideHand, boolean enabled)
        implements CustomPayload {

    public static final CustomPayload.Id<CinemaModePayload> ID =
            new CustomPayload.Id<>(Identifier.of(CialloMineCamera.MOD_ID, "cinema_mode"));

    public static final PacketCodec<RegistryByteBuf, CinemaModePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.FLOAT, CinemaModePayload::horizontal,
            PacketCodecs.FLOAT, CinemaModePayload::vertical,
            PacketCodecs.FLOAT, CinemaModePayload::speed,
            PacketCodecs.BOOL,  CinemaModePayload::hideHand,
            PacketCodecs.BOOL,  CinemaModePayload::enabled,
            CinemaModePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
