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
package com.crowsofwar.avatar.common.item;

import com.crowsofwar.avatar.AvatarMod;
import com.crowsofwar.avatar.common.item.scroll.*;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author CrowsOfWar
 */
public class AvatarItems {

	public static List<Item> allItems;
	public static ItemWaterPouch itemWaterPouch;
	public static ItemBisonWhistle itemBisonWhistle;
	public static ItemBisonSaddle itemBisonSaddle;
	public static ItemBisonArmor itemBisonArmor;
	public static ItemOstrichEquipment itemOstrichEquipment;
	public static ItemStack stackScroll;
	public static ItemAirbenderStaff airbenderStaff;

	public static CreativeTabs tabItems = new CreativeTabs("avatar.items") {
		@Nonnull
		@Override
		public ItemStack createIcon() {
			return AvatarItems.stackScroll;
		}
	};

	private AvatarItems() {
	}

	public static void init() {
		allItems = new ArrayList<>();
		addItem(Scrolls.ALL = new ItemScrollAll());
		addItem(Scrolls.AIR = new ItemScrollAir());
		addItem(Scrolls.EARTH = new ItemScrollEarth());
		addItem(Scrolls.FIRE = new ItemScrollFire());
		addItem(Scrolls.WATER = new ItemScrollWater());
		addItem(Scrolls.COMBUSTION = new ItemScrollCombustion());
		addItem(Scrolls.SAND = new ItemScrollSand());
		addItem(Scrolls.ICE = new ItemScrollIce());
		addItem(Scrolls.LIGHTNING = new ItemScrollLightning());
		addItem(itemWaterPouch = new ItemWaterPouch());
		addItem(itemBisonWhistle = new ItemBisonWhistle());
		addItem(itemBisonArmor = new ItemBisonArmor());
		addItem(itemBisonSaddle = new ItemBisonSaddle());
		addItem(itemOstrichEquipment = new ItemOstrichEquipment());
		addItem(airbenderStaff = new ItemAirbenderStaff(Item.ToolMaterial.WOOD));

		stackScroll = new ItemStack(Scrolls.ALL);
		MinecraftForge.EVENT_BUS.register(new AvatarItems());

	}

	private static void addItem(Item item) {
		item.setRegistryName("avatarmod", item.getTranslationKey().substring(5));
		item.setTranslationKey("avatarmod:" + item.getTranslationKey().substring(5));
		allItems.add(item);
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> e) {
		Item[] itemsArr = allItems.toArray(new Item[allItems.size()]);
		e.getRegistry().registerAll(itemsArr);
		AvatarMod.proxy.registerItemModels();
	}

}
