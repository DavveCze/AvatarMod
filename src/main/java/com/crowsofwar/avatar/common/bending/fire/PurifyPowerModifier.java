package com.crowsofwar.avatar.common.bending.fire;

import com.crowsofwar.avatar.common.bending.BuffPowerModifier;
import com.crowsofwar.avatar.common.data.AbilityData;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.avatar.common.data.Vision;
import com.crowsofwar.avatar.common.data.ctx.BendingContext;

public class PurifyPowerModifier extends BuffPowerModifier {

	@Override
	public double get(BendingContext ctx) {

		BendingData data = ctx.getData();
		AbilityData abilityData = data.getAbilityData("purify");

		double modifier = 20;
		if (abilityData.getLevel() >= 1) {
			modifier = 25;
		}
		if (abilityData.getLevel() == 3) {
			modifier = 40;
		}
		if (abilityData.isMasterPath(AbilityData.AbilityTreePath.SECOND)) {
			modifier = 60;
		}
		return modifier;

	}

	@Override
	public boolean onUpdate(BendingContext ctx) {

		// Intermittently light on fire
		if (ctx.getBenderEntity().ticksExisted % 20 == 0) {

			AbilityData abilityData = AbilityData.get(ctx.getBenderEntity(), "purify");

			double chance = 0.3;
			if (abilityData.isMasterPath(AbilityData.AbilityTreePath.SECOND)) {
				chance = 0.6;
			}

			// 30% chance per second to be lit on fire
			if (Math.random() < chance) {
				ctx.getBenderEntity().setFire(2);
			}
		}

		return super.onUpdate(ctx);
	}

	@Override
	protected Vision[] getVisions() {
		return new Vision[]{Vision.PURIFY_WEAK, Vision.PURIFY_MEDIUM, Vision.PURIFY_POWERFUL};
	}

	@Override
	protected String getAbilityName() {
		return "purify";
	}

}

