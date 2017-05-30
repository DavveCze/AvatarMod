/* 
  This file is part of AvatarMod.
    
  AvatarMod is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  AvatarMod is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with AvatarMod. If not, see <http://www.gnu.org/licenses/>.
*/

package com.crowsofwar.avatar.common.network.packets;

import static com.crowsofwar.gorecore.util.GoreCoreByteBufUtil.readUUID;
import static com.crowsofwar.gorecore.util.GoreCoreByteBufUtil.writeUUID;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.crowsofwar.avatar.common.data.AvatarPlayerData;
import com.crowsofwar.avatar.common.data.DataCategory;
import com.crowsofwar.avatar.common.network.PacketRedirector;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.relauncher.Side;

/**
 * 
 * 
 * @author CrowsOfWar
 */
public class PacketCPlayerData extends AvatarPacket<PacketCPlayerData> {
	
	/**
	 * Used server-side to find what to write<br />
	 * Used client-side to write to
	 */
	private AvatarPlayerData data;
	private UUID playerId;
	
	private Set<DataCategory> changed;
	
	public PacketCPlayerData() {}
	
	public PacketCPlayerData(AvatarPlayerData data, UUID player, Set<DataCategory> changed) {
		this.data = data;
		this.playerId = player;
		this.changed = changed;
	}
	
	@Override
	public void avatarFromBytes(ByteBuf buf) {
		playerId = readUUID(buf);
		
		// Find what changed
		changed = new HashSet<>();
		int size = buf.readInt();
		for (int i = 0; i < size; i++) {
			changed.add(DataCategory.values()[buf.readInt()]);
		}
		
	}
	
	@Override
	public void avatarToBytes(ByteBuf buf) {
		writeUUID(buf, playerId);
		
		// Tell client what has changed
		buf.writeInt(changed.size());
		for (DataCategory category : changed) {
			buf.writeInt(category.ordinal());
		}
		
		// The "real" payload - player data
		
	}
	
	@Override
	protected Side getRecievedSide() {
		return Side.CLIENT;
	}
	
	@Override
	protected com.crowsofwar.avatar.common.network.packets.AvatarPacket.Handler<PacketCPlayerData> getPacketHandler() {
		return PacketRedirector::redirectMessage;
	}
	
	public UUID getPlayerId() {
		return playerId;
	}
	
}
