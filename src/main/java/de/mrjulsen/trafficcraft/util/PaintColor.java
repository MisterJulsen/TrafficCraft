package de.mrjulsen.trafficcraft.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MaterialColor;

/*
 * EXTENDED COPY OF DyeColor.class
 */
public enum PaintColor implements StringRepresentable {
   NONE(-1, "none", 0xFFFFFFFF, MaterialColor.NONE, 0xFFFFFFFF, 0xFFFFFFFF, 'r'),
   WHITE(0, "white", 16383998, MaterialColor.SNOW, 15790320, 16777215, 'f'),
   ORANGE(1, "orange", 16351261, MaterialColor.COLOR_ORANGE, 15435844, 16738335, '6'),
   MAGENTA(2, "magenta", 13061821, MaterialColor.COLOR_MAGENTA, 12801229, 16711935, '5'),
   LIGHT_BLUE(3, "light_blue", 3847130, MaterialColor.COLOR_LIGHT_BLUE, 6719955, 10141901, 'b'),
   YELLOW(4, "yellow", 16701501, MaterialColor.COLOR_YELLOW, 14602026, 16776960, 'e'),
   LIME(5, "lime", 8439583, MaterialColor.COLOR_LIGHT_GREEN, 4312372, 12582656, 'a'),
   PINK(6, "pink", 15961002, MaterialColor.COLOR_PINK, 14188952, 16738740, 'd'),
   GRAY(7, "gray", 4673362, MaterialColor.COLOR_GRAY, 4408131, 8421504, '8'),
   LIGHT_GRAY(8, "light_gray", 10329495, MaterialColor.COLOR_LIGHT_GRAY, 11250603, 13882323, '7'),
   CYAN(9, "cyan", 1481884, MaterialColor.COLOR_CYAN, 2651799, 65535, '3'),
   PURPLE(10, "purple", 8991416, MaterialColor.COLOR_PURPLE, 8073150, 10494192, '5'),
   BLUE(11, "blue", 3949738, MaterialColor.COLOR_BLUE, 2437522, 255, '1'),
   BROWN(12, "brown", 8606770, MaterialColor.COLOR_BROWN, 5320730, 9127187, '6'),
   GREEN(13, "green", 6192150, MaterialColor.COLOR_GREEN, 3887386, 65280, '2'),
   RED(14, "red", 11546150, MaterialColor.COLOR_RED, 11743532, 16711680, 'c'),
   BLACK(15, "black", 1908001, MaterialColor.COLOR_BLACK, 1973019, 0, '0');

   private static final PaintColor[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(PaintColor::getId))
         .toArray((p_41067_) -> {
            return new PaintColor[p_41067_];
         });
   private static final Int2ObjectOpenHashMap<PaintColor> BY_FIREWORK_COLOR = new Int2ObjectOpenHashMap<>(
         Arrays.stream(values()).collect(Collectors.toMap((p_41064_) -> {
            return p_41064_.fireworkColor;
         }, (p_41056_) -> {
            return p_41056_;
         })));
   private final int id;
   private final String name;
   private final MaterialColor color;
   private final float[] textureDiffuseColors;
   private final int textureColor;
   private final int fireworkColor;
   private final net.minecraft.tags.TagKey<Item> tag;
   private final int textColor;
   private final char colorCode;

   private PaintColor(int pId, String pName, int pTextureColor, MaterialColor pColor, int pFireworkColor,
         int pTextColor, char colorCode) {
      this.id = pId;
      this.name = pName;
      this.color = pColor;
      this.textColor = pTextColor;
      this.textureColor = pTextureColor;
      int i = (pTextureColor & 16711680) >> 16;
      int j = (pTextureColor & '\uff00') >> 8;
      int k = (pTextureColor & 255) >> 0;
      this.tag = net.minecraft.tags.ItemTags
            .create(new net.minecraft.resources.ResourceLocation("forge", "dyes/" + pName));
      this.textureDiffuseColors = new float[] { (float) i / 255.0F, (float) j / 255.0F, (float) k / 255.0F };
      this.fireworkColor = pFireworkColor;
      this.colorCode = colorCode;
   }

   public int getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public char getColorCode() {
      return this.colorCode;
   }

   /**
    * Gets an array containing 3 floats ranging from 0.0 to 1.0: the red, green,
    * and blue components of the
    * corresponding color.
    */
   public float[] getTextureDiffuseColors() {
      return this.textureDiffuseColors;
   }

   public int getTextureColor() {
      return this.textureColor;
   }

   public MaterialColor getMaterialColor() {
      return this.color;
   }

   public int getFireworkColor() {
      return this.fireworkColor;
   }

   public int getTextColor() {
      return this.textColor;
   }

   public static PaintColor byId(int pColorId) {
      if (pColorId < -1 || pColorId > BY_ID.length - 2) {
         pColorId = 0;
      }

      return BY_ID[pColorId + 1];
   }

   public static PaintColor byName(String pTranslationKey, PaintColor pFallback) {
      for (PaintColor PaintColor : values()) {
         if (PaintColor.name.equals(pTranslationKey)) {
            return PaintColor;
         }
      }

      return pFallback;
   }

   @Nullable
   public static PaintColor byFireworkColor(int pFireworkColor) {
      return BY_FIREWORK_COLOR.get(pFireworkColor);
   }

   public String toString() {
      return this.name;
   }

   public String getSerializedName() {
      return this.name;
   }

   public net.minecraft.tags.TagKey<Item> getTag() {
      return tag;
   }

   @Nullable
   public static PaintColor getColor(ItemStack stack) {
      if (stack.getItem() instanceof DyeItem item)
         return PaintColor.byId(item.getDyeColor().getId());

      for (PaintColor color : BY_ID) {
         if (stack.is(color.getTag()))
            return color;
      }

      return null;
   }

   public boolean equals(DyeColor dye) {
      return dye.getId() == this.getId();
   }

   public static PaintColor getFromDye(DyeColor dye) {
      return PaintColor.byId(dye.getId());
   }

   public String getTranslatableString() {
      return String.format("color.trafficcraft.%s", this.getSerializedName());
   }

   public static Block getConcreteByColor(PaintColor color) {
      switch (color) {
         case BLACK:
            return Blocks.BLACK_CONCRETE;
         case BLUE:
            return Blocks.BLUE_CONCRETE;
         case BROWN:
            return Blocks.BROWN_CONCRETE;
         case CYAN:
            return Blocks.CYAN_CONCRETE;
         case GRAY:
            return Blocks.GRAY_CONCRETE;
         case GREEN:
            return Blocks.GREEN_CONCRETE;
         case LIGHT_BLUE:
            return Blocks.LIGHT_BLUE_CONCRETE;
         case LIGHT_GRAY:
            return Blocks.LIGHT_GRAY_CONCRETE;
         case LIME:
            return Blocks.LIME_CONCRETE;
         case MAGENTA:
            return Blocks.MAGENTA_CONCRETE;
         case ORANGE:
            return Blocks.ORANGE_CONCRETE;
         case PINK:
            return Blocks.PINK_CONCRETE;
         case PURPLE:
            return Blocks.PURPLE_CONCRETE;
         case RED:
            return Blocks.RED_CONCRETE;
         case WHITE:
            return Blocks.WHITE_CONCRETE;            
         case YELLOW:
            return Blocks.YELLOW_CONCRETE;
         default:
            return null;
      }
   }

   public static Block getWoolByColor(PaintColor color) {
      switch (color) {
         case BLACK:
            return Blocks.BLACK_WOOL;
         case BLUE:
            return Blocks.BLUE_WOOL;
         case BROWN:
            return Blocks.BROWN_WOOL;
         case CYAN:
            return Blocks.CYAN_WOOL;
         case GRAY:
            return Blocks.GRAY_WOOL;
         case GREEN:
            return Blocks.GREEN_WOOL;
         case LIGHT_BLUE:
            return Blocks.LIGHT_BLUE_WOOL;
         case LIGHT_GRAY:
            return Blocks.LIGHT_GRAY_WOOL;
         case LIME:
            return Blocks.LIME_WOOL;
         case MAGENTA:
            return Blocks.MAGENTA_WOOL;
         case ORANGE:
            return Blocks.ORANGE_WOOL;
         case PINK:
            return Blocks.PINK_WOOL;
         case PURPLE:
            return Blocks.PURPLE_WOOL;
         case RED:
            return Blocks.RED_WOOL;
         case WHITE:
            return Blocks.WHITE_WOOL;            
         case YELLOW:
            return Blocks.YELLOW_WOOL;
         default:
            return null;
      }
   }

   public static Block getStainedGlassByColor(PaintColor color) {
      switch (color) {
         case BLACK:
            return Blocks.BLACK_STAINED_GLASS;
         case BLUE:
            return Blocks.BLUE_STAINED_GLASS;
         case BROWN:
            return Blocks.BROWN_STAINED_GLASS;
         case CYAN:
            return Blocks.CYAN_STAINED_GLASS;
         case GRAY:
            return Blocks.GRAY_STAINED_GLASS;
         case GREEN:
            return Blocks.GREEN_STAINED_GLASS;
         case LIGHT_BLUE:
            return Blocks.LIGHT_BLUE_STAINED_GLASS;
         case LIGHT_GRAY:
            return Blocks.LIGHT_GRAY_STAINED_GLASS;
         case LIME:
            return Blocks.LIME_STAINED_GLASS;
         case MAGENTA:
            return Blocks.MAGENTA_STAINED_GLASS;
         case ORANGE:
            return Blocks.ORANGE_STAINED_GLASS;
         case PINK:
            return Blocks.PINK_STAINED_GLASS;
         case PURPLE:
            return Blocks.PURPLE_STAINED_GLASS;
         case RED:
            return Blocks.RED_STAINED_GLASS;
         case WHITE:
            return Blocks.WHITE_STAINED_GLASS;            
         case YELLOW:
            return Blocks.YELLOW_STAINED_GLASS;
         default:
            return null;
      }
   }

   public static Block getStainedGlassPaneByColor(PaintColor color) {
      switch (color) {
         case BLACK:
            return Blocks.BLACK_STAINED_GLASS_PANE;
         case BLUE:
            return Blocks.BLUE_STAINED_GLASS_PANE;
         case BROWN:
            return Blocks.BROWN_STAINED_GLASS_PANE;
         case CYAN:
            return Blocks.CYAN_STAINED_GLASS_PANE;
         case GRAY:
            return Blocks.GRAY_STAINED_GLASS_PANE;
         case GREEN:
            return Blocks.GREEN_STAINED_GLASS_PANE;
         case LIGHT_BLUE:
            return Blocks.LIGHT_BLUE_STAINED_GLASS_PANE;
         case LIGHT_GRAY:
            return Blocks.LIGHT_GRAY_STAINED_GLASS_PANE;
         case LIME:
            return Blocks.LIME_STAINED_GLASS_PANE;
         case MAGENTA:
            return Blocks.MAGENTA_STAINED_GLASS_PANE;
         case ORANGE:
            return Blocks.ORANGE_STAINED_GLASS_PANE;
         case PINK:
            return Blocks.PINK_STAINED_GLASS_PANE;
         case PURPLE:
            return Blocks.PURPLE_STAINED_GLASS_PANE;
         case RED:
            return Blocks.RED_STAINED_GLASS_PANE;
         case WHITE:
            return Blocks.WHITE_STAINED_GLASS_PANE;            
         case YELLOW:
            return Blocks.YELLOW_STAINED_GLASS_PANE;
         default:
            return null;
      }
   }

   public static Block getShulkerBoxByColor(PaintColor color) {
      switch (color) {
         case BLACK:
            return Blocks.BLACK_SHULKER_BOX;
         case BLUE:
            return Blocks.BLUE_SHULKER_BOX;
         case BROWN:
            return Blocks.BROWN_SHULKER_BOX;
         case CYAN:
            return Blocks.CYAN_SHULKER_BOX;
         case GRAY:
            return Blocks.GRAY_SHULKER_BOX;
         case GREEN:
            return Blocks.GREEN_SHULKER_BOX;
         case LIGHT_BLUE:
            return Blocks.LIGHT_BLUE_SHULKER_BOX;
         case LIGHT_GRAY:
            return Blocks.LIGHT_GRAY_SHULKER_BOX;
         case LIME:
            return Blocks.LIME_SHULKER_BOX;
         case MAGENTA:
            return Blocks.MAGENTA_SHULKER_BOX;
         case ORANGE:
            return Blocks.ORANGE_SHULKER_BOX;
         case PINK:
            return Blocks.PINK_SHULKER_BOX;
         case PURPLE:
            return Blocks.PURPLE_SHULKER_BOX;
         case RED:
            return Blocks.RED_SHULKER_BOX;
         case WHITE:
            return Blocks.WHITE_SHULKER_BOX;            
         case YELLOW:
            return Blocks.YELLOW_SHULKER_BOX;
         default:
            return null;
      }
   }

   public static Block getCarpetByColor(PaintColor color) {
      switch (color) {
         case BLACK:
            return Blocks.BLACK_CARPET;
         case BLUE:
            return Blocks.BLUE_CARPET;
         case BROWN:
            return Blocks.BROWN_CARPET;
         case CYAN:
            return Blocks.CYAN_CARPET;
         case GRAY:
            return Blocks.GRAY_CARPET;
         case GREEN:
            return Blocks.GREEN_CARPET;
         case LIGHT_BLUE:
            return Blocks.LIGHT_BLUE_CARPET;
         case LIGHT_GRAY:
            return Blocks.LIGHT_GRAY_CARPET;
         case LIME:
            return Blocks.LIME_CARPET;
         case MAGENTA:
            return Blocks.MAGENTA_CARPET;
         case ORANGE:
            return Blocks.ORANGE_CARPET;
         case PINK:
            return Blocks.PINK_CARPET;
         case PURPLE:
            return Blocks.PURPLE_CARPET;
         case RED:
            return Blocks.RED_CARPET;
         case WHITE:
            return Blocks.WHITE_CARPET;            
         case YELLOW:
            return Blocks.YELLOW_CARPET;
         default:
            return null;
      }
   }

   public static Block getBedByColor(PaintColor color) {
      switch (color) {
         case BLACK:
            return Blocks.BLACK_BED;
         case BLUE:
            return Blocks.BLUE_BED;
         case BROWN:
            return Blocks.BROWN_BED;
         case CYAN:
            return Blocks.CYAN_BED;
         case GRAY:
            return Blocks.GRAY_BED;
         case GREEN:
            return Blocks.GREEN_BED;
         case LIGHT_BLUE:
            return Blocks.LIGHT_BLUE_BED;
         case LIGHT_GRAY:
            return Blocks.LIGHT_GRAY_BED;
         case LIME:
            return Blocks.LIME_BED;
         case MAGENTA:
            return Blocks.MAGENTA_BED;
         case ORANGE:
            return Blocks.ORANGE_BED;
         case PINK:
            return Blocks.PINK_BED;
         case PURPLE:
            return Blocks.PURPLE_BED;
         case RED:
            return Blocks.RED_BED;
         case WHITE:
            return Blocks.WHITE_BED;            
         case YELLOW:
            return Blocks.YELLOW_BED;
         default:
            return null;
      }
   }

   public static Block getTerracottaByColor(PaintColor color) {
      switch (color) {
         case BLACK:
            return Blocks.BLACK_TERRACOTTA;
         case BLUE:
            return Blocks.BLUE_TERRACOTTA;
         case BROWN:
            return Blocks.BROWN_TERRACOTTA;
         case CYAN:
            return Blocks.CYAN_TERRACOTTA;
         case GRAY:
            return Blocks.GRAY_TERRACOTTA;
         case GREEN:
            return Blocks.GREEN_TERRACOTTA;
         case LIGHT_BLUE:
            return Blocks.LIGHT_BLUE_TERRACOTTA;
         case LIGHT_GRAY:
            return Blocks.LIGHT_GRAY_TERRACOTTA;
         case LIME:
            return Blocks.LIME_TERRACOTTA;
         case MAGENTA:
            return Blocks.MAGENTA_TERRACOTTA;
         case ORANGE:
            return Blocks.ORANGE_TERRACOTTA;
         case PINK:
            return Blocks.PINK_TERRACOTTA;
         case PURPLE:
            return Blocks.PURPLE_TERRACOTTA;
         case RED:
            return Blocks.RED_TERRACOTTA;
         case WHITE:
            return Blocks.WHITE_TERRACOTTA;            
         case YELLOW:
            return Blocks.YELLOW_TERRACOTTA;
         default:
            return null;
      }
   }

   public static Block getCandleByColor(PaintColor color) {
      switch (color) {
         case BLACK:
            return Blocks.BLACK_CANDLE;
         case BLUE:
            return Blocks.BLUE_CANDLE;
         case BROWN:
            return Blocks.BROWN_CANDLE;
         case CYAN:
            return Blocks.CYAN_CANDLE;
         case GRAY:
            return Blocks.GRAY_CANDLE;
         case GREEN:
            return Blocks.GREEN_CANDLE;
         case LIGHT_BLUE:
            return Blocks.LIGHT_BLUE_CANDLE;
         case LIGHT_GRAY:
            return Blocks.LIGHT_GRAY_CANDLE;
         case LIME:
            return Blocks.LIME_CANDLE;
         case MAGENTA:
            return Blocks.MAGENTA_CANDLE;
         case ORANGE:
            return Blocks.ORANGE_CANDLE;
         case PINK:
            return Blocks.PINK_CANDLE;
         case PURPLE:
            return Blocks.PURPLE_CANDLE;
         case RED:
            return Blocks.RED_CANDLE;
         case WHITE:
            return Blocks.WHITE_CANDLE;            
         case YELLOW:
            return Blocks.YELLOW_CANDLE;
         default:
            return null;
      }
   }

   public static Block getBannerByColor(PaintColor color) {
      switch (color) {
         case BLACK:
            return Blocks.BLACK_BANNER;
         case BLUE:
            return Blocks.BLUE_BANNER;
         case BROWN:
            return Blocks.BROWN_BANNER;
         case CYAN:
            return Blocks.CYAN_BANNER;
         case GRAY:
            return Blocks.GRAY_BANNER;
         case GREEN:
            return Blocks.GREEN_BANNER;
         case LIGHT_BLUE:
            return Blocks.LIGHT_BLUE_BANNER;
         case LIGHT_GRAY:
            return Blocks.LIGHT_GRAY_BANNER;
         case LIME:
            return Blocks.LIME_BANNER;
         case MAGENTA:
            return Blocks.MAGENTA_BANNER;
         case ORANGE:
            return Blocks.ORANGE_BANNER;
         case PINK:
            return Blocks.PINK_BANNER;
         case PURPLE:
            return Blocks.PURPLE_BANNER;
         case RED:
            return Blocks.RED_BANNER;
         case WHITE:
            return Blocks.WHITE_BANNER;            
         case YELLOW:
            return Blocks.YELLOW_BANNER;
         default:
            return null;
      }
   }
}
