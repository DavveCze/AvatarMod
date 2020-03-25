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

import com.crowsofwar.avatar.common.network.PacketRedirector;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;

/**
 * @author CrowsOfWar
 */
public class PacketSWallJump extends AvatarPacket<PacketSWallJump> {

	private KeyBinding key;

	public PacketSWallJump(KeyBinding key) {
		this.key = key;
	}

	public PacketSWallJump() {
		super();
	}

	@Override
	public void avatarFromBytes(ByteBuf buf) {
		PacketBuffer buffer = new PacketBuffer(buf);
		//key = KeyBinding.getKeybinds().stream().filter(key1 -> KeyBinding.) buf.readInt();
	}

	@Override
	public void avatarToBytes(ByteBuf buf) {
//		buf.writeInt(key.getKeyCode());
	}

	@Override
	protected Side getReceivedSide() {
		return Side.SERVER;
	}

	@Override
	protected com.crowsofwar.avatar.common.network.packets.AvatarPacket.Handler<PacketSWallJump> getPacketHandler() {
		return PacketRedirector::redirectMessage;
	}

	public KeyBinding getKey() {
		return this.key;
	}

}
