package com.crowsofwar.avatar.common.bending.air;

import com.crowsofwar.avatar.common.data.PowerRatingModifier;
import com.crowsofwar.avatar.common.data.ctx.BendingContext;

public class StaffPowerModifier extends PowerRatingModifier {
	@Override
	public double get(BendingContext ctx) {
		return ctx.getBender().calcPowerRating(Airbending.ID) + 40;
	}
}
