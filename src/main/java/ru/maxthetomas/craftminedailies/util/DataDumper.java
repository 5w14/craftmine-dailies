package ru.maxthetomas.craftminedailies.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import ru.maxthetomas.craftminedailies.CraftmineDailies;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class DataDumper {
    public static void dumpData() {
        var json = new JsonObject();

        json.add("effects", collectEffects());
        json.add("effect_sets", collectSets());
        json.add("unlocks", collectUnlocks());
        json.add("items", collectItems());
        json.addProperty("using_experimental_settings", CraftmineDailies.EXPERIMENTAL);

        try {
            Files.writeString(Path.of("./effect_json_data.json"), json.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static JsonObject collectItems() {
        var items = new JsonObject();
        BuiltInRegistries.ITEM.entrySet().forEach(entry -> {
            var obj = new JsonObject();

            obj.addProperty("default_price", entry.getValue()
                    .components().getOrDefault(DataComponents.EXCHANGE_VALUE, Item.NO_EXCHANGE).value());

            items.add(entry.getKey().location().toString(), obj);
        });
        return items;
    }

    private static JsonObject collectEffects() {
        var effects = new JsonObject();

        BuiltInRegistries.WORLD_EFFECT.entrySet().forEach(entry -> {
            var effect = entry.getValue();
            var key = entry.getKey().location().toString();
            var effectJson = new JsonObject();
            effectJson.addProperty("weight", effect.randomWeight());
            effectJson.addProperty("multiplayer_only", effect.multiplayerOnly());
            effectJson.addProperty("unlock_mode", effect.unlockMode().toString().toLowerCase());
            effectJson.addProperty("xp_multiplier", effect.experienceModifier());

            var incompat = new JsonArray();
            effect.incompatibleWith().forEach(we -> {
                var inc = BuiltInRegistries.WORLD_EFFECT.getResourceKey(we).orElse(ResourceKey.create(Registries.WORLD_EFFECT,
                        ResourceLocation.withDefaultNamespace(we.key()))).location();
                incompat.add(inc.toString());
            });
            effectJson.add("incompatible_with", incompat);

            effects.add(key, effectJson);
        });

        return effects;
    }

    private static JsonObject collectSets() {
        var sets = new JsonObject();

        BuiltInRegistries.WORLD_EFFECT_SET.entrySet().forEach(entry -> {
            var setJson = new JsonObject();
            var key = entry.getKey().location().toString();

            setJson.addProperty("exclusive", entry.getValue().exclusive());

            var effectList = new JsonArray();
            entry.getValue().effects().forEach(eff -> effectList.add("minecraft:" + eff.key()));
            setJson.add("effects", effectList);
            sets.add(key, setJson);
        });

        return sets;
    }

    private static JsonObject collectUnlocks() {
        var unlocks = new JsonObject();

        BuiltInRegistries.PLAYER_UNLOCK.entrySet().forEach(entry -> {
            var unlockJson = new JsonObject();
            var key = entry.getKey().location().toString();

            var disablesArray = new JsonArray();
            entry.getValue().disables().forEach(disable -> {
                disablesArray.add(disable.getRegisteredName());
            });


            unlockJson.add("disables", disablesArray);
            unlockJson.addProperty("price", entry.getValue().unlockPrice());

            entry.getValue().parent().ifPresentOrElse((unlock) ->
                    unlockJson.addProperty("parent", unlock.getRegisteredName()), () -> unlockJson.addProperty("parent", "minecraft:empty"));

            unlocks.add(key, unlockJson);
        });

        return unlocks;
    }
}
