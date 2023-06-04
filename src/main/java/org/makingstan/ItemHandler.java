package org.makingstan;

import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;

import static org.makingstan.ConfigElements.*;

public class ItemHandler {

    private static Client client;
    private static ItemHunterConfig config;
    private static ItemManager itemManager;
    private static ItemHunterPanel panel;
    private static ClientThread clientThread;

    public ItemHandler(Client client, ItemHunterConfig config, ItemManager itemManager, ItemHunterPanel panel, ClientThread clientThread)
    {
        ItemHandler.clientThread = clientThread;
        ItemHandler.panel = panel;
        ItemHandler.itemManager = itemManager;
        ItemHandler.config = config;
        ItemHandler.client = client;
    }


    private static int getRandomValidItemId()
    {
        boolean itemIsValid = false;
        int itemId = 0;

        while(!itemIsValid)
        {
            itemId = Utils.random(0, client.getItemCount());

            ItemComposition composition = client.getItemDefinition(itemId);

            HashSet<Integer> blockList = (HashSet<Integer>)SaveHandler.getElement(BLOCK_LIST, HashSet.class);
            HashSet<Integer> completedList = (HashSet<Integer>)SaveHandler.getElement(COMPLETED_LIST, HashSet.class);

            // convert the name to a UTF8 valid string, otherwise you'll have weird invisable characters and that could mess up the conditions for a valid item
            String itemName = new String(composition.getMembersName().getBytes(StandardCharsets.UTF_8));

            HashSet<String> completedListNames = new HashSet<>();

            for (Integer integer : completedList) {
                String name = new String(client.getItemDefinition(integer).getMembersName().getBytes(StandardCharsets.UTF_8));
                completedListNames.add(name);
            }


            if(
                    // don't generate any items that don't have a main, are named null or whatever
                    composition.getName().trim().length() > 0 &&
                            !itemName.trim().equalsIgnoreCase("null") &&

                            // don't generate duplicate clue scroll items, as there are alot of duplicate clue scroll ids
                            !(completedListNames.contains(itemName) && itemName.toLowerCase().startsWith("clue scroll")) &&

                            //We don't want to regenerate any blocked or completed items
                            !blockList.contains(itemId) &&
                            !completedList.contains(itemId)
            )
            {
                if(config.excludeNotedItems())
                {
                    if(composition.getNote() == -1) itemIsValid = true;
                }
                else
                {
                    itemIsValid = true;
                }
            }
        }

        return itemId;
    }

    // Generates a new item
    public static void generateNewItem(boolean checkIfCompleted)
    {
        if(checkIfCompleted)
        {
            if(isCurrentItemCompleted() || isCurrentItemSkipped())
            {
                int newItemConfirm = JOptionPane.showConfirmDialog(null,
                        "Are you sure you want to generate a new item?", "Item Hunter",
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null);

                if(newItemConfirm == JOptionPane.YES_OPTION)
                {
                    newItem();
                }
            }
            else
            {
                JOptionPane.showMessageDialog(null, "You have to complete or skip an item before generating another!");
            }

        }
        else
        {
            newItem();
        }
    }

    private static void newItem()
    {
        clientThread.invokeLater(new Runnable() {
            @Override
            public void run() {
                int itemId = getRandomValidItemId();

                SaveHandler.setElement(CURRENT_ITEM, itemId);
                SaveHandler.setElement(COMPLETED, false);
                SaveHandler.setElement(SKIPPED, false);

                updateItemUI();
            }
        });
    }

    public static void unobtainableItem()
    {
        // We don't want to flag an item as unobtainable if it's already completed or skipped
        if(isCurrentItemCompleted() || isCurrentItemSkipped())
        {
            JOptionPane.showConfirmDialog(null,
                    "You can't flag an item as unobtainable if you've completed or skipped it!", "Item Hunter",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
            return;
        }

        int confirmUnobtainable = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to set this item to unobtainable? It will be added to your block list and a new item will be generated.", "Item Hunter",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null);

        // If the user didn't click the yes option, we don't let the unobtainable item event go through the current item.
        if(confirmUnobtainable != JOptionPane.YES_OPTION) return;


        SaveHandler.addItemToList(BLOCK_LIST, (Integer) SaveHandler.getElement(CURRENT_ITEM, int.class));
        ItemHandler.generateNewItem(false);
    }

    public static void skipItem()
    {
        /* Don't skip an item if you have completed it */
        if(isCurrentItemCompleted() || isCurrentItemSkipped()) {
            JOptionPane.showConfirmDialog(null,
                    "You can't skip an item if you've completed or skipped it!", "Item Hunter",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
            return;
        }


        int confirmSkip = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to skip this item?", "Item Hunter",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null);

        // If the user didn't click the yes option, we don't skip the current item.
        if(confirmSkip != JOptionPane.YES_OPTION) return;



        // Forfeit skipped items if the user wishes to do so
        if(config.forfeitSkippedItems())
        {
            HashSet<Integer> completedList = (HashSet<Integer>)SaveHandler.getElement(COMPLETED_LIST, HashSet.class);
            boolean shouldContinue = true;

            if(completedList.size() == 0)
            {
                JOptionPane.showConfirmDialog(null,
                        "You can't forfeit an item if you don't have any completed items!", "Item Hunter",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
                shouldContinue = false;
            }

            if(shouldContinue)
            {
                int randomIndex = Utils.random(0, completedList.size()-1);
                int itemIdToBeForfeited = 0;

                int i = 0;
                for(int itemId : completedList)
                {
                    if(i == randomIndex)
                    {
                        itemIdToBeForfeited = itemId;

                        completedList.remove(itemIdToBeForfeited);
                        int finalItemIdToBeForfeited = itemIdToBeForfeited;
                        clientThread.invoke(() -> {
                            try {
                                ItemComposition composition = client.getItemDefinition(finalItemIdToBeForfeited);
;
                                SwingUtilities.invokeAndWait(() -> {
                                    // Create an item popup of what item you forfeited
                                    JOptionPane.showConfirmDialog(null,
                                            "Your forfeited your "+composition.getMembersName()+"!", "Item Hunter",
                                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
                                });
                            } catch (InterruptedException | InvocationTargetException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        break;
                    }
                    i++;
                }

                // update out completed list to the completed list without our forfeited item
                SaveHandler.setElement(COMPLETED_LIST, completedList);
            }
        }


        // If the user wants to block skipped items, let's do that
        if(config.blockSkippedItems())
        {
            SaveHandler.addItemToList(BLOCK_LIST, (Integer) SaveHandler.getElement(CURRENT_ITEM, int.class));
        }

        // Set our skipped variable to true, because this item has been skipped
        SaveHandler.setElement(SKIPPED, true);

        // Update our ui to be able to see the skipped change
        updateItemUI();
    }

    public static void clearCompletedItems()
    {
        String confirmClearAll = JOptionPane.showInputDialog(null,
                "Are you really sure you want to clear all of your completed items? Type \"yes\" to confirm.");


        // Don't continue if they didn't type "yes"
        // Do the null check to avoid a NPE
        if(confirmClearAll != null)
        {
            if(!confirmClearAll.equalsIgnoreCase("yes")) return;
        }
        else
        {
            return;
        }

        SaveHandler.setElement(COMPLETED_LIST, new HashSet<Integer>());

        updateItemUI();
    }

    public static void completeItem()
    {
        /* Don't complete an item if you have skipped it */
        if(isCurrentItemSkipped() || isCurrentItemCompleted()) {
            JOptionPane.showConfirmDialog(null,
                    "You can't complete an item if you've skipped or completed it!", "Item Hunter",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
            return;
        }

        int confirmComplete = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to complete this item?", "Item Hunter",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null);

        // Don't continue if they didn't click YES
        if(confirmComplete != JOptionPane.YES_OPTION) return;

        // We now know that the user can and wants to complete this item,
        // set our completed variable to true and add our item to the completed list so it won't be generated again
        SaveHandler.addItemToList(COMPLETED_LIST, (Integer) SaveHandler.getElement(CURRENT_ITEM, int.class));
        SaveHandler.setElement(COMPLETED, true);

        // Update our ui to show the new completed value
        updateItemUI();
    }

    public static boolean isCurrentItemCompleted()
    {
        return (boolean)SaveHandler.getElement(COMPLETED, boolean.class);
    }

    public static boolean isCurrentItemSkipped()
    {
        return (boolean)SaveHandler.getElement(SKIPPED, boolean.class);
    }

    public static void updateItemUI()
    {
        int itemId = (int)SaveHandler.getElement(CURRENT_ITEM, int.class);

        clientThread.invoke(() -> {
            ItemComposition itemComposition = client.getItemDefinition(itemId);
            BufferedImage iconImage = itemManager.getImage(itemId);

            panel.updateItemUI(itemComposition, iconImage);
        });
    }
}
