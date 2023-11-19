package de.mrjulsen.trafficcraft.client.widgets;

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
    
    private boolean enabled = true;
    private boolean visible = true;

    public void performForEach(Predicate<? super AbstractWidget> filter, Consumer<? super AbstractWidget> consumer) {
        components.stream().filter(filter).forEach(consumer);
    }

    public void performForEach(Consumer<? super AbstractWidget> consumer) {
        performForEach(x -> true, consumer);
    }

    public <C extends AbstractWidget> void performForEachOfType(Class<C> clazz, Predicate<C> filter, Consumer<C> consumer) {
        components.stream().filter(clazz::isInstance).map(clazz::cast).filter(filter).forEach(consumer);
    }

    public <C extends AbstractWidget> void performForEachOfType(Class<C> clazz, Consumer<C> consumer) {
        performForEachOfType(clazz, x -> true, consumer);
    }

    

    public boolean isVisible() {
        return visible;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setVisible(boolean v) {
        this.visible = v;
        performForEach(x -> x.visible = v);
    }

    public void setEnabled(boolean e) {
        this.enabled = e;
        performForEach(x -> x.active = e);
    }

    public <W extends AbstractWidget> void add(W widget) {
        components.add(widget);
    }

    public void clear() {
        components.clear();
    }
}


