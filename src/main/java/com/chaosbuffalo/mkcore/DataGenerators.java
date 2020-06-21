package com.chaosbuffalo.mkcore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        if (event.includeServer()) {
            generator.addProvider(new AbilityDataGenerator(generator));
        }
    }

    static class AbilityDataGenerator implements IDataProvider {
        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        private final DataGenerator generator;

        public AbilityDataGenerator(DataGenerator generator) {
            this.generator = generator;
        }

        @Override
        public void act(@Nonnull DirectoryCache cache) {
            Path outputFolder = this.generator.getOutputFolder();
            MKCoreRegistry.ABILITIES.forEach(ability -> {
                ResourceLocation key = ability.getAbilityId();
                MKCore.LOGGER.info("Dumping ability {}", key);
                if (!key.getPath().startsWith("ability.")) {
                    MKCore.LOGGER.warn("Skipping {} because it did not have the 'ability.' prefix", key);
                    return;
                }
                String name = key.getPath().substring(8); // skip ability.
                Path path = outputFolder.resolve("data/" + key.getNamespace() + "/player_abilities/" + name + ".json");
                try {
                    JsonElement element = new Dynamic<>(NBTDynamicOps.INSTANCE, ability.serialize()).convert(JsonOps.INSTANCE).getValue();
                    IDataProvider.save(GSON, cache, element, path);
                } catch (IOException e) {
                    MKCore.LOGGER.error("Couldn't write ability {}", path, e);
                }
            });
        }

        @Nonnull
        @Override
        public String getName() {
            return "MKCore Abilities";
        }
    }
}
