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
package com.crowsofwar.avatar.common.bending.fire;

import com.crowsofwar.avatar.common.bending.Ability;
import com.crowsofwar.avatar.common.bending.BendingAi;
import com.crowsofwar.avatar.common.data.AbilityData;
import com.crowsofwar.avatar.common.data.Bender;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.avatar.common.data.ctx.BendingContext;
import com.crowsofwar.avatar.common.util.Raytrace;
import com.crowsofwar.gorecore.util.Vector;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;

import static com.crowsofwar.avatar.common.data.StatusControlController.START_FLAMETHROW;
import static com.crowsofwar.avatar.common.data.StatusControlController.STOP_FLAMETHROW;
import static com.crowsofwar.avatar.common.data.TickHandlerController.FLAMETHROWER;
import static com.crowsofwar.gorecore.util.Vector.getEntityPos;
import static com.crowsofwar.gorecore.util.Vector.getRotationTo;
import static java.lang.Math.toDegrees;

/**
 * @author CrowsOfWar
 */
public class AiFlamethrower extends BendingAi {

	protected AiFlamethrower(Ability ability, EntityLiving entity, Bender bender) {
		super(ability, entity, bender);
		setMutexBits(2);
	}

	@Override
	public void resetTask() {
		super.resetTask();
		bender.getData().removeStatusControl(START_FLAMETHROW);
		bender.getData().removeTickHandler(FLAMETHROWER);
		bender.getData().removeStatusControl(STOP_FLAMETHROW);
	}

	@Override
	public boolean shouldContinueExecuting() {


		if (entity.getAttackTarget() == null || AbilityData.get(bender.getEntity(), "flamethrower").getLevel() < 0) return false;

		Vector rotations = getRotationTo(getEntityPos(entity), getEntityPos(entity.getAttackTarget()));
		BendingData data = BendingData.getFromEntity(entity);
		entity.rotationYaw = (float) toDegrees(rotations.y());
		entity.rotationPitch = (float) toDegrees(rotations.x());

		if (timeExecuting == 5) {
			if (!entity.world.isRemote) {
				execAbility();
				execStatusControl(START_FLAMETHROW);
			}
			if (data != null)
				data.addTickHandler(FLAMETHROWER);
		}

		if (timeExecuting > 20 && timeExecuting < 100) {
			BendingContext ctx = new BendingContext(bender.getData(), entity, bender, new Raytrace.Result());
			execStatusControl(START_FLAMETHROW);
			FLAMETHROWER.tick(ctx);
		}
		if (timeExecuting >= 120) {
			bender.getData().removeStatusControl(START_FLAMETHROW);
			bender.getData().removeTickHandler(FLAMETHROWER);
			execStatusControl(STOP_FLAMETHROW);

			return false;
		}

		return true;

	}

	@Override
	protected boolean shouldExec() {
		int amount = Math.max(bender.getData().getAbilityData(new AbilityFlamethrower()).getLevel(), 0) + 7;
		EntityLivingBase target = entity.getAttackTarget();
		return target != null && entity.getDistance(target) < amount;
	}

	@Override
	protected void startExec() {
		bender.getData().addStatusControl(START_FLAMETHROW);
		execAbility();
	}

}
