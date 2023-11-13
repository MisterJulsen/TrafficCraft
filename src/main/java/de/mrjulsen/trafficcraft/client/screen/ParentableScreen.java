package de.mrjulsen.trafficcraft.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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

}
