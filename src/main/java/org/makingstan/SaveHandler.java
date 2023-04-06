package org.makingstan;

import net.runelite.client.config.ConfigManager;

import java.util.HashSet;

import static org.makingstan.ConfigElements.CONFIG_GROUP;

public class SaveHandler {

    private static ConfigManager configManager;

    public SaveHandler(ConfigManager configManager)
    {
        SaveHandler.configManager = configManager;
    }

    public static void addItemToList(String listName, int itemId)
    {
        HashSet<Integer> list = (HashSet<Integer>) getElement(listName, HashSet.class);

        list.add(itemId);

        configManager.setConfiguration(CONFIG_GROUP, listName, list);
    }

    public static void setElement(String elementName, Object value)
    {
       configManager.setConfiguration(CONFIG_GROUP, elementName, value);
    }

    public static Object getElement(String elementName, Class<?> clazz)
    {
        if(clazz == HashSet.class)
        {
            // The configmanager doesn't support storing the hashsets type, and i really wanne store in a hashmap so im just gonna accept that i have to do this
            // https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/config/ConfigManager.java
            HashSet<Integer> hashset = new HashSet<>();
            String hashSetString = configManager.getConfiguration(CONFIG_GROUP, elementName, clazz);

            if(hashSetString == null) return null;

            String bracketsRemoved = hashSetString.replaceAll("\\[", "").replaceAll("]", "");


            String[] commaSeperated = bracketsRemoved.split(",");


            for(String element : commaSeperated)
            {
                if(!element.equalsIgnoreCase(""))
                {
                    int elementInt = Integer.parseInt(element);
                    hashset.add(elementInt);
                }
            }

            return hashset;
        }
        return configManager.getConfiguration(CONFIG_GROUP, elementName, clazz);
    }
}
