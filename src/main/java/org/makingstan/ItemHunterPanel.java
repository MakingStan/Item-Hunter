package org.makingstan;

import lombok.SneakyThrows;
import net.runelite.api.ItemComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class ItemHunterPanel extends PluginPanel {
    JLabel itemStatus = new JLabel();
    JLabel itemIconLabel = new JLabel();
    JLabel itemName = new JLabel();
    JLabel itemId = new JLabel();

    JButton skipButton = new JButton( );
    JButton completedButton = new JButton();
    JButton newItemButton = new JButton();
    JButton unobtainableItemButton = new JButton();

    JPanel itemInformation = new JPanel();
    JPanel itemShowCasePanel = new JPanel();
    JPanel controlButtonsPanel = new JPanel();

    JPanel itemPanel = new JPanel();
    JPanel completedItemsPanel = new JPanel();
    JPanel columnHeader = new JPanel();

    JScrollPane completedItemsPane = new JScrollPane();
    JButton clearAllButton = new JButton("Clear All");


    BufferedImage skipImage = ImageUtil.loadImageResource(getClass(), "/skip.png");
    BufferedImage completedIcon = ImageUtil.loadImageResource(getClass(), "/complete.png");
    BufferedImage newItemIcon = ImageUtil.loadImageResource(getClass(), "/newitem.png");
    BufferedImage unobtainableIcon = ImageUtil.loadImageResource(getClass(), "/unobtainable.png");

    ItemHunterPlugin itemGeneratorPlugin;

    @Inject
    public ItemHunterPanel(ItemHunterPlugin itemGeneratorPlugin) {
        this.itemGeneratorPlugin = itemGeneratorPlugin;

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        itemStatus.setFont(FontManager.getRunescapeBoldFont());
        itemName.setFont(FontManager.getRunescapeBoldFont());
        itemId.setFont(FontManager.getRunescapeFont());

        itemName.setHorizontalAlignment(JLabel.CENTER);
        itemId.setHorizontalAlignment(JLabel.CENTER);

        itemInformation.setLayout(new GridBagLayout());

        GridBagConstraints itemInformationConstraints = new GridBagConstraints();
        itemInformationConstraints.gridy = 0;
        itemInformation.add(itemName, itemInformationConstraints);
        itemInformationConstraints.gridy = 1;
        itemInformation.add(itemId, itemInformationConstraints);

        skipButton.setIcon(new ImageIcon(skipImage));
        skipButton.setFocusPainted(false);
        skipButton.setToolTipText("Skip Item");
        skipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ItemHandler.skipItem();
            }
        });


        completedButton.setIcon(new ImageIcon(completedIcon));
        completedButton.setFocusPainted(false);
        completedButton.setToolTipText("Complete Item");
        completedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ItemHandler.completeItem();
            }
        });

        newItemButton.setIcon(new ImageIcon(newItemIcon));
        newItemButton.setFocusPainted(false);
        newItemButton.setToolTipText("New Item");
        newItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ItemHandler.generateNewItem(true);
            }
        });

        unobtainableItemButton.setIcon(new ImageIcon(unobtainableIcon));
        unobtainableItemButton.setFocusPainted(false);
        unobtainableItemButton.setToolTipText("Unobtainable Item");
        unobtainableItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ItemHandler.unobtainableItem();
            }
        });

        clearAllButton.setFocusPainted(false);
        clearAllButton.setToolTipText("Clear Items");
        clearAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ItemHandler.clearCompletedItems();
            }
        });

        itemShowCasePanel.setLayout(new GridBagLayout());
        GridBagConstraints itemShowCasePanelConstraints = new GridBagConstraints();
        itemShowCasePanelConstraints.gridy = 0;
        itemShowCasePanel.add(itemIconLabel, itemShowCasePanelConstraints);
        itemShowCasePanelConstraints.gridy = 1;
        itemShowCasePanel.add(itemInformation, itemShowCasePanelConstraints);

        controlButtonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        controlButtonsPanel.add(completedButton);
        controlButtonsPanel.add(newItemButton);
        controlButtonsPanel.add(skipButton);
        controlButtonsPanel.add(unobtainableItemButton);

        itemPanel.setLayout(new BorderLayout());
        itemPanel.add(itemStatus, BorderLayout.NORTH);
        itemPanel.add(itemShowCasePanel, BorderLayout.CENTER);
        itemPanel.add(controlButtonsPanel, BorderLayout.SOUTH);

        completedItemsPanel.setLayout(new BoxLayout(completedItemsPanel, BoxLayout.Y_AXIS));

        columnHeader.add(new JLabel("Completed Items: ", SwingConstants.LEFT));

        JViewport viewPort = new JViewport();
        viewPort.setView(columnHeader);
        viewPort.setFont(FontManager.getRunescapeBoldFont());

        completedItemsPane.setColumnHeader(viewPort);
        completedItemsPane.setViewportView(completedItemsPanel);
        completedItemsPane.getViewport().setPreferredSize(new Dimension(this.getWidth(), 300));
        completedItemsPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        completedItemsPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        
        add(itemPanel, BorderLayout.NORTH);
        add(completedItemsPane, BorderLayout.SOUTH);
    }

    @SneakyThrows
    public void updateItemUI(ItemComposition itemComposition, BufferedImage itemIcon)
    {
        itemStatus.setText("Current Item to Obtain: ");

        if(ItemHandler.isCurrentItemSkipped())
        {
            itemStatus.setText("Item Skipped");
        }
        else if(ItemHandler.isCurrentItemCompleted())
        {
            itemStatus.setText("Item Completed");
        }


        itemIconLabel.setIcon(new ImageIcon(resize(itemIcon, itemIcon.getWidth()*3, itemIcon.getHeight()*3)));

        itemName.setText(itemComposition.getMembersName());
        itemId.setText("Id: "+itemComposition.getId());



        completedItemsPanel.removeAll();
        completedItemsPane.getViewport().remove(completedItemsPane.getViewport().getView());

        SwingUtilities.invokeAndWait(() -> {
            boolean containsClearAllButton = false;

            for(Component component: columnHeader.getComponents())
            {
                if(component instanceof JButton)
                {
                    containsClearAllButton = true;
                    break;
                }
            }

            if(containsClearAllButton && itemGeneratorPlugin.config.hideClearCompletedItems())
            {
                columnHeader.remove(clearAllButton);
            }
            else if(!containsClearAllButton && !itemGeneratorPlugin.config.hideClearCompletedItems())
            {
                columnHeader.add(clearAllButton);
            }
        });


        for(int completedItem : (HashSet<Integer>)SaveHandler.getElement(ConfigElements.COMPLETED_LIST, HashSet.class))
        {
            itemGeneratorPlugin.clientThread.invoke(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    ItemComposition composition = itemGeneratorPlugin.client.getItemDefinition(completedItem);
                    BufferedImage iconImage = itemGeneratorPlugin.itemManager.getImage(completedItem);

                    SwingUtilities.invokeAndWait(() -> completedItemsPanel.add(new ItemTemplate(composition, iconImage)));
                }
            });
        }

        completedItemsPane.setViewportView(completedItemsPanel);

        this.revalidate();
    }

    // I "borrowed" this piece of code
    /* https://stackoverflow.com/questions/9417356/bufferedimage-resize */
    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }
}