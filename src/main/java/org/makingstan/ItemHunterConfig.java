package org.makingstan;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Item Hunter")
public interface ItemHunterConfig extends Config
{

	@ConfigItem(
			keyName = "Exclude noted items",
			name = "Exclude noted items",
			description = "Excludes noted items from generating."
	)
	default boolean excludeNotedItems() {
		return true;
	}

	@ConfigItem(
			keyName = "Block skipped items",
			name = "Block skipped items",
			description = "Blocks skipped items."
	)
	default boolean blockSkippedItems()
	{
		return false;
	}

	@ConfigItem(
			keyName = "Forfeit for skipping items",
			name = "Forfeit for skipping items",
			description = "Enables the forfeiting of skipped items."
	)
	default boolean forfeitSkippedItems()
	{
		return true;
	}

	@ConfigItem(
			keyName = "Hide clear all button",
			name = "Hide clear all button",
			description = "Hides the clear all button."
	)
	default boolean hideClearCompletedItems()
	{
		return true;
	}

	@ConfigItem(
			keyName = "Dialog confirmation",
			name = "Dialog confirmation",
			description = "Enables the popping up of a dialog."
	)
	default boolean dialogConfirmation()
	{
		return true;
	}


}
