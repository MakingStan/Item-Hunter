package org.makingstan;

import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ItemTemplate extends JPanel {
    JLabel itemName = new JLabel();

    public ItemTemplate(ItemComposition itemComposition, BufferedImage iconImage)
    {
        this.setLayout(new GridBagLayout());
        this.setBackground(ColorScheme.DARK_GRAY_COLOR.darker());

        Dimension itemNameDimension = new Dimension(215, iconImage.getHeight());

        this.itemName.setHorizontalTextPosition(SwingConstants.RIGHT);
        this.itemName.setFont(FontManager.getRunescapeFont());
        this.itemName.setText(itemComposition.getMembersName());
        this.itemName.setIcon(new ImageIcon(iconImage));
        this.itemName.setMaximumSize(itemNameDimension);
        this.itemName.setMinimumSize(itemNameDimension);
        this.itemName.setPreferredSize(itemNameDimension);

        this.setMaximumSize(itemNameDimension);
        this.setMinimumSize(itemNameDimension);
        this.setPreferredSize(itemNameDimension);


        this.add(this.itemName);
    }
}
