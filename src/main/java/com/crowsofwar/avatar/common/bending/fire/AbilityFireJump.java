package com.crowsofwar.avatar.common.bending.fire;

import com.crowsofwar.avatar.common.bending.Ability;
import com.crowsofwar.avatar.common.bending.StatusControl;
import com.crowsofwar.avatar.common.data.AbilityData;
import com.crowsofwar.avatar.common.data.Bender;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.avatar.common.data.TickHandler;
import com.crowsofwar.avatar.common.data.ctx.AbilityContext;
import com.crowsofwar.avatar.common.data.ctx.BendingContext;
import com.crowsofwar.avatar.common.util.Raytrace;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import static com.crowsofwar.avatar.common.bending.StatusControl.FIRE_JUMP;
import static com.crowsofwar.avatar.common.config.ConfigStats.STATS_CONFIG;

public class AbilityFireJump extends Ability {
	public AbilityFireJump() {
		super(Firebending.ID, "fire_jump");
	}

	@Override
	public boolean isUtility() {
		return true;
	}

	@Override
	public void execute(AbilityContext ctx) {

		BendingData data = ctx.getData();
		Bender bender = ctx.getBender();

		if (!data.hasStatusControl(FIRE_JUMP) && bender.consumeChi(STATS_CONFIG.chiAirJump)) {

			data.addStatusControl(FIRE_JUMP);
			if (data.hasTickHandler(TickHandler.FIRE_PARTICLE_SPAWNER)) {
				StatusControl sc = FIRE_JUMP;
				Raytrace.Result raytrace = Raytrace.getTargetBlock(ctx.getBenderEntity(), -1);
				if (FIRE_JUMP.execute(
						new BendingContext(data, ctx.getBenderEntity(), ctx.getBender(), raytrace))) {
					data.removeStatusControl(FIRE_JUMP);
				}
			}

		}
	}

	@Override
	public int getCooldown(AbilityContext ctx) {
		EntityLivingBase entity = ctx.getBenderEntity();

		int coolDown = 120;

		if (entity instanceof EntityPlayer && ((EntityPlayer) entity).isCreative()) {
			coolDown = 0;
		}

		if (ctx.getLevel() == 1) {
			coolDown = 110;
		}
		if (ctx.getLevel() == 2) {
			coolDown = 100;
		}
		if (ctx.isMasterLevel(AbilityData.AbilityTreePath.FIRST)) {
			coolDown = 80;
		}
		if (ctx.isMasterLevel(AbilityData.AbilityTreePath.SECOND)) {
			coolDown = 15;
		}
		return coolDown;
	}

}

