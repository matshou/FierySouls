package com.yooksi.fierysouls.common;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CustomSoundEvents 
{
	public static SoundEvent item_match_strike;

	/**
	 * Register the {@link SoundEvent}s.
	 */
	public static void registerSounds() 
	{
		item_match_strike = registerSound("item.match_strike");
	}

	private static SoundEvent registerSound(String soundName) 
	{
		final ResourceLocation soundID = new ResourceLocation(FierySouls.MODID, soundName);
		return GameRegistry.register(new SoundEvent(soundID).setRegistryName(soundID));
	}
}