package de.mrjulsen.trafficcraft.client.tooltip;

import de.mrjulsen.trafficcraft.data.TrafficSignData;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class TrafficSignTooltip implements TooltipComponent {
   private final NonNullList<TrafficSignData> data;
   private final int selectedIndex;

   public TrafficSignTooltip(NonNullList<TrafficSignData> data, int selectedIndex) {
      this.data = data;
      this.selectedIndex = selectedIndex;
   }

   public NonNullList<TrafficSignData> getData() {
      return this.data;
   }

   public int getSelectedIndex() {
      return selectedIndex;
   }
}
