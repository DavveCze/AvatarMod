package com.crowsofwar.avatar.common.bending.combustion;

import com.crowsofwar.avatar.common.bending.Abilities;
import com.crowsofwar.avatar.common.bending.Ability;
import com.crowsofwar.avatar.common.bending.BendingStyle;
import com.crowsofwar.avatar.common.bending.fire.Firebending;
import com.crowsofwar.avatar.common.gui.BendingMenuInfo;
import com.crowsofwar.avatar.common.gui.MenuTheme;
import net.minecraft.nbt.NBTTagCompound;

import java.awt.*;
import java.util.UUID;

public class Combustionbending extends BendingStyle {

	public static final UUID ID = UUID.fromString("8485da8f-20d9-4354-6e47-df13446d7da5");

	private final BendingMenuInfo menu;

	public Combustionbending() {
		super(Firebending.ID);

		for (Ability ability : Abilities.getAbilitiesToRegister(ID))
			addAbility(ability.getName());
		Color light = new Color(244, 240, 187);
		Color red = new Color(173, 64, 31);
		Color gray = new Color(40, 40, 40);
		MenuTheme.ThemeColor background = new MenuTheme.ThemeColor(light, red);
		MenuTheme.ThemeColor edge = new MenuTheme.ThemeColor(red, red);
		MenuTheme.ThemeColor icon = new MenuTheme.ThemeColor(gray, light);
		menu = new BendingMenuInfo(new MenuTheme(background, edge, icon, 0xFAAA5A), this);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {

	}

	@Override
	public int getTextColour() {
		return 0x606060;
	}

	@Override
	public BendingMenuInfo getRadialMenu() {
		return menu;
	}

	@Override
	public String getName() {
		return "combustionbending";
	}

	@Override
	public UUID getId() {
		return ID;
	}

}