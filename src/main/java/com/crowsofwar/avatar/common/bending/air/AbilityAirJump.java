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

package com.crowsofwar.avatar.common.bending.air;

import com.crowsofwar.avatar.common.bending.Ability;
import com.crowsofwar.avatar.common.data.AbilityData;
import com.crowsofwar.avatar.common.data.Bender;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.avatar.common.data.TickHandler;
import com.crowsofwar.avatar.common.data.ctx.AbilityContext;
import com.crowsofwar.avatar.common.data.ctx.BendingContext;
import com.crowsofwar.avatar.common.util.Raytrace;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.swing.text.html.parser.Entity;

import static com.crowsofwar.avatar.common.bending.StatusControl.AIR_JUMP;
import static com.crowsofwar.avatar.common.config.ConfigStats.STATS_CONFIG;

/**
 * @author CrowsOfWar
 */
public class AbilityAirJump extends Ability {

	public AbilityAirJump() {
		super(Airbending.ID, "air_jump");
	}

	@Override
	public boolean isUtility() {
		return true;
	}

	@Override
	public void execute(AbilityContext ctx) {

		BendingData data = ctx.getData();
		Bender bender = ctx.getBender();

		if (!data.hasStatusControl(AIR_JUMP) && bender.consumeChi(STATS_CONFIG.chiAirJump)) {

			data.addStatusControl(AIR_JUMP);
			if (data.hasTickHandler(TickHandler.AIR_PARTICLE_SPAWNER)) {
				Raytrace.Result raytrace = Raytrace.getTargetBlock(ctx.getBenderEntity(), -1);
				if (AIR_JUMP.execute(
						new BendingContext(data, ctx.getBenderEntity(), ctx.getBender(), raytrace))) {
					data.removeStatusControl(AIR_JUMP);
				}
			}

		}

	}

	@Override
	public int getCooldown(AbilityContext ctx) {
		EntityLivingBase entity = ctx.getBenderEntity();
		int coolDown = 60;

		if (entity instanceof EntityPlayer && ((EntityPlayer) entity).isCreative()) {
			coolDown = 0;
		}

		if (ctx.getLevel() == 1) {
			coolDown = 50;
		}
		if (ctx.getLevel() == 2) {
			coolDown = 40;
		}
		if (ctx.isMasterLevel(AbilityData.AbilityTreePath.FIRST)) {
			coolDown = 10;
		}
		if (ctx.isMasterLevel(AbilityData.AbilityTreePath.SECOND)) {
			coolDown = 40;
		}
		return coolDown;
	}


}
