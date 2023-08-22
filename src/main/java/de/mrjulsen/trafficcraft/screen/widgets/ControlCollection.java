package de.mrjulsen.trafficcraft.screen.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ControlCollection {
    public final List<AbstractWidget> components = new ArrayList<>(); 

    public void performForEach(Predicate<? super AbstractWidget> filter, Consumer<? super AbstractWidget> consumer) {
        components.stream().filter(filter).forEach(consumer);
    }

    public void performForEach(Consumer<? super AbstractWidget> consumer) {
        components.stream().forEach(consumer);
    }
}
