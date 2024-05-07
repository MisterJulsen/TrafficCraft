package de.mrjulsen.legacydragonlib.common;

public interface ITranslatableEnum {
    /**
     * Name of the enum class.
     */
    String getEnumName();

    /**
     * Name of each enum value.
     */
    String getEnumValueName();

    /**
     * Format: enum.examplemod.myenum
     */
    default String getEnumTranslationKey(String modid) {
        return String.format("enum.%s.%s", modid, this.getEnumName());
    }
    /**
     * Format: enum.examplemod.myenum.examplevalue
     */
    default String getValueTranslationKey(String modid) {
        return String.format("enum.%s.%s.%s", modid, this.getEnumName(), this.getEnumValueName());
    }
    /**
     * Format: enum.examplemod.myenum.description
     */
    default String getEnumDescriptionTranslationKey(String modid) {
        return String.format("enum.%s.%s.description", modid, this.getEnumName());
    }
    /**
     * Format: enum.examplemod.myenum.info.examplevalue
     */
    default String getValueInfoTranslationKey(String modid) {
        return String.format("enum.%s.%s.info.%s", modid, this.getEnumName(), this.getEnumValueName());
    }
}
