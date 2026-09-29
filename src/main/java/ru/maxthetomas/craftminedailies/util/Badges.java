package ru.maxthetomas.craftminedailies.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.maxthetomas.craftminedailies.CraftmineDailies;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

public final class Badges {
    private static final HashSet<BadgeRecord> REGISTRY = new HashSet<>();

    public static BadgeRecord CONTRIBUTOR = registerBadge(0, "contributor");
    public static BadgeRecord TRANSLATOR = registerBadge(1, "translator");

    private static final Hashtable<Integer, Set<BadgeRecord>> badgesCache = new Hashtable<>();
    public static Set<BadgeRecord> getBadges(int bitField) {
        if (badgesCache.containsKey(bitField))
            return badgesCache.get(bitField);

        var set = new HashSet<BadgeRecord>();

        for (var r : REGISTRY) {
            if (r.matchesBitfield(bitField))
                set.add(r);
        }

        badgesCache.put(bitField, set);
        return set;
    }

    private static BadgeRecord registerBadge(int bitIdx, String name) {
        var badge = BadgeRecord.create(bitIdx, name);
        REGISTRY.add(badge);
        return badge;
    };

    public record BadgeRecord(int bitIdx, String name, ResourceLocation texture) {
        private static BadgeRecord create(int bitIdx, String name) {
            return new BadgeRecord(bitIdx, name, ResourceLocation.fromNamespaceAndPath(CraftmineDailies.MOD_ID, "badges/" + name));
        }

        public boolean matchesBitfield(int bitfield) {
            return (bitfield & (1 << bitIdx)) == (1 << bitIdx);
        }

        public List<Component> getTooltipLines() {
            return List.of(
                Component.translatable("craftminedailies.badge." + name + ".title").withStyle(ChatFormatting.BOLD),
                Component.translatable("craftminedailies.badge." + name + ".description")
            );
        }
    }
}
