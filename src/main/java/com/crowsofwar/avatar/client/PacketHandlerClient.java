package com.crowsofwar.avatar.client;

import com.crowsofwar.avatar.AvatarLog;
import com.crowsofwar.avatar.common.bending.BendingManager;
import com.crowsofwar.avatar.common.bending.IBendingState;
import com.crowsofwar.avatar.common.data.AvatarPlayerData;
import com.crowsofwar.avatar.common.network.IPacketHandler;
import com.crowsofwar.avatar.common.network.packets.PacketCPlayerData;
import com.crowsofwar.avatar.common.util.Raytrace;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Handles packets addressed to the client. Packets like this have a C in their name.
 *
 */
@SideOnly(Side.CLIENT)
public class PacketHandlerClient implements IPacketHandler {
	
	private final Minecraft mc;
	
	public PacketHandlerClient() {
		this.mc = Minecraft.getMinecraft();
	}
	
	@Override
	public IMessage onPacketReceived(IMessage packet, MessageContext ctx) {
		
		if (packet instanceof PacketCPlayerData) return handlePacketPlayerData((PacketCPlayerData) packet, ctx);
		
		AvatarLog.warn("Client recieved unknown packet from server:" + packet);
		
		return null;
	}
	
	@Override
	public Side getSide() {
		return Side.CLIENT;
	}
	
	private IMessage handlePacketPlayerData(PacketCPlayerData packet, MessageContext ctx) {
		EntityPlayer player = mc.thePlayer;
		AvatarPlayerData data = AvatarPlayerData.fetcher().fetch(player, "Error while processing player data packet");
		if (data != null) {
			System.out.println("recieved info about player");
			// Add bending controllers & bending states
			data.takeBending();
			for (int i = 0; i < packet.getAllControllersID().length; i++) {
				data.addBending(packet.getAllControllersID()[i]);
				data.getState().update(player, Raytrace.getTargetBlock(player, -1));
			}
			for (int i = 0; i < packet.getAllControllersID().length; i++) {
				IBendingState state = data.getBendingState(packet.getBuf().readInt());
				state.fromBytes(packet.getBuf());
			}
			
			data.setActiveBendingController(BendingManager.getBending(packet.getCurrentBendingControllerID()));
		}
		return null;
	}
	
}
