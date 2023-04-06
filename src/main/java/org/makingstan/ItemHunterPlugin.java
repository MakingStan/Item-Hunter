package org.makingstan;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.util.HashSet;

import static org.makingstan.ConfigElements.*;

@Slf4j
@PluginDescriptor(
	name = "Item Hunter"
)
public class ItemHunterPlugin extends Plugin
{
	@Inject
	public Client client;

	@Inject
	public ItemHunterConfig config;

	@Inject
	public ClientToolbar clientToolbar;

	@Inject
	public ClientThread clientThread;

	@Inject
	public ItemManager itemManager;

	@Inject
	public ConfigManager configManager;

	private NavigationButton navButton;
	private ItemHunterPanel panel;

	BufferedImage panelIcon = ImageUtil.loadImageResource(getClass(), "/itemhuntericon48x48.png");

	@Override
	protected void startUp()
	{
		new SaveHandler(configManager);
		this.panel = injector.getInstance(ItemHunterPanel.class);
		new ItemHandler(client, config, itemManager, panel, clientThread);


		navButton = NavigationButton.builder()
			.tooltip("Item Hunter")
			.icon(panelIcon)
			.priority(3)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);

		Integer itemId = (Integer) SaveHandler.getElement(CURRENT_ITEM, int.class);
		if(SaveHandler.getElement(BLOCK_LIST, HashSet.class) == null)
		{
			configManager.setConfiguration(CONFIG_GROUP, BLOCK_LIST, new HashSet<Integer>());
		}
		if(SaveHandler.getElement(COMPLETED_LIST, HashSet.class) == null)
		{
			configManager.setConfiguration(CONFIG_GROUP, COMPLETED_LIST, new HashSet<Integer>());
		}
		if(SaveHandler.getElement(COMPLETED, boolean.class) == null)
		{
			configManager.setConfiguration(CONFIG_GROUP, COMPLETED, false);
		}
		if(SaveHandler.getElement(SKIPPED, boolean.class) == null)
		{
			configManager.setConfiguration(CONFIG_GROUP, SKIPPED, false);
		}

		if(itemId == null)
		{
			ItemHandler.generateNewItem(false);
		}
		else
		{
			ItemHandler.updateItemUI();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if(
				event.getGroup().equalsIgnoreCase("Item Hunter") &&
				(event.getNewValue().equalsIgnoreCase("true") || event.getNewValue().equalsIgnoreCase("false")) &&
				(event.getKey().equalsIgnoreCase("Forfeit for skipping items") || event.getKey().equalsIgnoreCase("Hide clear all button"))
		)
		{
			ItemHandler.updateItemUI();
		}
	}

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(navButton);
	}

	@Provides
	ItemHunterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ItemHunterConfig.class);
	}
}
