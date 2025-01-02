package de.mrjulsen.trafficcraft.client.tooltip;

import java.util.HashMap;
import java.util.Map;

import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.data.AgingManager;
import de.mrjulsen.trafficcraft.data.IAgeable;
import de.mrjulsen.trafficcraft.data.NamedTrafficSignTextureReference;
import de.mrjulsen.trafficcraft.data.TrafficSignClientTexture;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class TrafficSignTooltip implements TooltipComponent, IAgeable {

	private final NamedTrafficSignTextureReference selected;
	private final NonNullList<NamedTrafficSignTextureReference> patterns;
	private final Runnable onClear;
	private final Map<NamedTrafficSignTextureReference, TrafficSignClientTexture> textures = new HashMap<>();
	private final int selectedIndex;

	public TrafficSignTooltip(NonNullList<NamedTrafficSignTextureReference> patterns, NamedTrafficSignTextureReference selected, int selectedIndex, Runnable onClear) {
		this.patterns = patterns;
		this.selected = selected;
		this.selectedIndex = selectedIndex;
		this.onClear = onClear;
		
		this.textures.clear();
		patterns.stream().forEach(x -> {
			textures.put(x, TrafficSignClientTexture.load(x.getTextureId(), false));
		});
		DLUtils.doIfNotNull(selected, a -> textures.computeIfAbsent(a, x -> TrafficSignClientTexture.load(x.getTextureId(), false)));
	}

	public NonNullList<NamedTrafficSignTextureReference> getPatterns() {
		return this.patterns;
	}

	public NamedTrafficSignTextureReference getSelected() {
		return selected;
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public Map<NamedTrafficSignTextureReference, TrafficSignClientTexture> getTextures() {
		return textures;
	}

	@Override
	public void onAging(int age) {
		if (age > 2) {
			AgingManager.remove(this);
			textures.values().stream().forEach(x -> x.close());
			textures.clear();
			onClear.run();
		}
	}

	@Override
	public AgingType getAgingType() {
		return AgingType.RENDER;
	}
}
