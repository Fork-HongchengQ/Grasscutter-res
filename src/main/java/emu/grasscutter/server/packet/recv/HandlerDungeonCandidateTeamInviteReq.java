package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketDungeonCandidateTeamCreateRsp;

@Opcodes(PacketOpcodes.DungeonCandidateTeamCreateReq)
public class HandlerDungeonCandidateTeamInviteReq extends PacketHandler {
    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
//        var req = DungeonCandidateTeamInviteReqOuterClass.DungeonCandidateTeamInviteReq.parseFrom(payload);

        session.send(new PacketDungeonCandidateTeamCreateRsp());
    }
}
