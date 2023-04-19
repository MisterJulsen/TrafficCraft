package de.mrjulsen.trafficcraft.screen.widgets;

import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class ParentableScreen extends Screen {
    
    protected ParentableScreen(Component pTitle) {
        super(pTitle);
    }

    public Font getFontRenderer() {
        return this.font;
    }

    public Minecraft getMinecraftInstance()
    {
        return this.minecraft;
    }

    public abstract <T extends ObjectSelectionList.Entry<T>> void buildList(Consumer<T> modListViewConsumer, Function<IListEntryData, T> newEntry);
}
