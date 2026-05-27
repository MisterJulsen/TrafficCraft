package de.mrjulsen.trafficcraft.client.tooltip;

import de.mrjulsen.trafficcraft.data.AgingManager;
import de.mrjulsen.trafficcraft.data.IAgeable;
import de.mrjulsen.trafficcraft.data.NamedTextureKey;
import de.mrjulsen.trafficcraft.data.textures.LocalTextureCache;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class TrafficSignTooltip implements TooltipComponent, IAgeable {

	private final NamedTextureKey selected;
	private final NonNullList<NamedTextureKey> patterns;
	private final Runnable onClear;
	private final LocalTextureCache textureCache = new LocalTextureCache();
	private final int selectedIndex;

	public TrafficSignTooltip(NonNullList<NamedTextureKey> patterns, NamedTextureKey selected, int selectedIndex, Runnable onClear) {
		this.patterns = patterns;
		this.selected = selected;
		this.selectedIndex = selectedIndex;
		this.onClear = onClear;
		//DLUtils.doIfNotNull(selected, a -> textures.computeIfAbsent(a, x -> TrafficSignClientTexture.load(x.getTextureId(), false, null)));
	}

	public NonNullList<NamedTextureKey> getPatterns() {
		return this.patterns;
	}

	public NamedTextureKey getSelected() {
		return selected;
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public LocalTextureCache getTextures() {
		return textureCache;
	}

	@Override
	public void onAging(int age) {
		if (age > 2) {
			AgingManager.remove(this);
			textureCache.releaseAll();
			onClear.run();
		}
	}

	@Override
	public AgingType getAgingType() {
		return AgingType.RENDER;
	}
}
