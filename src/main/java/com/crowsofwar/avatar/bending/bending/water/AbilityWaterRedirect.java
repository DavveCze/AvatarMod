package com.crowsofwar.avatar.bending.bending.water;

import com.crowsofwar.avatar.bending.bending.Ability;

public class AbilityWaterRedirect extends Ability {

	public AbilityWaterRedirect() {
		super(Waterbending.ID, "water_redirect");
	}

	@Override
	public int getBaseTier() {
		return 3;
	}

	@Override
	public boolean isVisibleInRadial() {
		return false;
	}

	@Override
	public boolean isUtility() {
		return true;
	}
}