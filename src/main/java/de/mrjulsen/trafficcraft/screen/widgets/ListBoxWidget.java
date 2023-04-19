package de.mrjulsen.trafficcraft.screen.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.ObjectSelectionList;

public abstract class ListBoxWidget<E extends ObjectSelectionList.Entry<E>> extends ObjectSelectionList<E> {
    
    private ParentableScreen parent;
    private int listWidth;
    private int listHeight;

    public ListBoxWidget(ParentableScreen parent, int listWidth, int listHeight, int startTop, int endBottom, int itemHeight) {
        super(parent.getMinecraftInstance(), listWidth, listHeight, startTop, endBottom, itemHeight);
        this.parent = parent;
        this.listWidth = listWidth;
        this.listHeight = listHeight;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.listWidth;
    }

    @Override
    public int getRowWidth() {
        return this.listWidth;
    }

    public int getListWidth() {
        return listWidth;
    }

    public int getListHeight() {
        return listHeight;
    }

    public ParentableScreen getParent() {
        return this.parent;
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {        
        this.renderBackground(pPoseStack);
        
        int j1 = this.getRowLeft();
        int k = this.y0 - (int)this.getScrollAmount();

        this.renderList(pPoseStack, j1, k, pMouseX, pMouseY, pPartialTick);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
    
    @Override
    protected void renderBackground(PoseStack pPoseStack) {
        fill(pPoseStack, this.getLeft() - 1, this.getTop() - 1, this.getLeft() + this.width + 1, this.getTop() + this.height + 1, -1);
        fill(pPoseStack, this.getLeft(), this.getTop(), this.getLeft() + this.width, this.getTop() + this.height, -16777216);
    }                                                                                                              




    public static abstract class ListBoxItem<E extends ObjectSelectionList.Entry<E>> extends Entry<E> {
        
        private IListEntryData data = null;
        private ParentableScreen parent = null;

        public ListBoxItem(IListEntryData data, ParentableScreen parent) {
            this.parent = parent;
            this.data = data;
        }

        public IListEntryData getData() {
            return this.data;
        }

        public ParentableScreen getParentScreen() {
            return this.parent;
        }
    }

}
