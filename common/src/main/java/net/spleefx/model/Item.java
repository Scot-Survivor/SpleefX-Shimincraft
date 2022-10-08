package net.spleefx.model;

import com.cryptomorin.xseries.SkullUtils;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.AccessLevel;
import lombok.Getter;
import net.spleefx.arena.player.MatchPlayer;
import net.spleefx.arena.team.MatchTeam;
import net.spleefx.compatibility.PluginCompatibility;
import net.spleefx.json.GsonHook;
import net.spleefx.json.GsonHook.AfterDeserialization;
import net.spleefx.util.Placeholders;
import net.spleefx.util.game.Chat;
import net.spleefx.util.plugin.Protocol;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static net.spleefx.util.Util.coerce;
import static net.spleefx.util.Util.n;
import static net.spleefx.util.game.Chat.colorize;

/**
 * An immutable, fast, and thread-safe wrapper for {@link ItemStack}s.
 */
@Getter
@GsonHook
@SuppressWarnings("unused")
public class Item extends SkullUtils {

    private final XMaterial type;
    private @Nullable final String displayName;
    private @Nullable final List<String> lore;
    private final int count;
    private @Nullable final Map<Enchantment, Integer> enchantments;
    private @Nullable final ItemFlag[] itemFlags;
    private @Nullable final UUID skull;
    private @Nullable final String textureValue;
    private final boolean unbreakable;
    private final boolean teamColor;

    @Getter(AccessLevel.NONE)
    protected transient ItemStack itemStack;

    protected Item(XMaterial type,
                   @Nullable String displayName,
                   List<String> lore,
                   int count,
                   @Nullable Map<Enchantment, Integer> enchantments,
                   ItemFlag[] itemFlags,
                   @Nullable UUID skull,
                   @Nullable String textureValue, boolean unbreakable, boolean teamColor) {
        this.type = type;
        this.displayName = colorize(displayName);
        this.lore = lore == null ? null : lore.stream().map(Chat::colorize).collect(Collectors.toList());
        this.count = count;
        this.enchantments = enchantments;
        this.itemFlags = itemFlags;
        this.skull = skull;
        this.textureValue = textureValue;
        this.unbreakable = unbreakable;
        this.teamColor = teamColor;
        createItem0();
    }

    public void give(Player... players) {
        for (Player player : players)
            giveItem(player);
    }

    public void give(int slot, Player... players) {
        for (Player player : players)
            player.getInventory().setItem(slot, itemStack);
    }

    public void give(Iterable<Player> players) {
        for (Player player : players)
            giveItem(player);
    }

    protected void giveItem(Player player) {
        player.getInventory().addItem(itemStack);
    }

    public Builder asBuilder() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Item team(@NotNull MatchTeam team) {
        return Item.builder().type(team.getItemOnSelectionGUI()).name(team.getDisplayName()).build();
    }

    @AfterDeserialization
    protected ItemStack createItem0() {
        ItemStack item;
        if (skull != null) {
            item = SkullUtils.getSkull(skull);
        } else if (textureValue != null) {
            item = n(XMaterial.PLAYER_HEAD.parseItem());
            SkullMeta meta = n((SkullMeta) item.getItemMeta());
            addTexture(meta, textureValue);
            item.setItemMeta(meta);
        } else {
            item = type.parseItem();
            if (item == null && type == XMaterial.SPLASH_POTION) {
                item = XMaterial.POTION.parseItem();
                try {
                    Potion pot = new Potion(PotionType.WATER);
                    pot.setSplash(true);
                    pot.apply(item);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
        Objects.requireNonNull(item, "Invalid material: " + type)
                .setAmount(coerce(count, 1, 64));
        ItemMeta m = item.getItemMeta();
        if (m == null) return item;
        if (displayName != null && !displayName.equals("{}")) m.setDisplayName(colorize(displayName));
        if (lore != null) m.setLore(lore.stream().map(Chat::colorize).collect(Collectors.toList()));
        if (itemFlags != null) m.addItemFlags(itemFlags);
        if (unbreakable) PluginCompatibility.setUnbreakable(m);
        item.setItemMeta(m);
        if (enchantments != null) item.addUnsafeEnchantments(enchantments);
        return itemStack = item;
    }

    public ItemStack withPlaceholders(Object... placeholders) {
        return setPlaceholders(new ItemStack(itemStack), placeholders);
    }

    public ItemStack respectTeam(MatchPlayer player, Object... placeholders) {
        if (teamColor) {
            DyeColor color = null;
            if (player.getArena().isTeams()) {
                color = player.getArena().getEngine().getTeams().get(player).team.getColor();
            }
            if (color != null) {
                ItemStack item = setPlaceholders(new ItemStack(itemStack), placeholders);
                setColor(item, color);
                return item;
            }
        }
        return setPlaceholders(new ItemStack(itemStack), placeholders);
    }

    public ItemStack withPlaceholdersAndAmount(int value, Object... placeholders) {
        ItemStack item = new ItemStack(itemStack);
        item.setAmount(coerce(value, 1, 64));
        return setPlaceholders(item, placeholders);
    }

    public ItemStack createItem() {
        return new ItemStack(itemStack);
    }

    public boolean isSimilar(@Nullable ItemStack other) {
        return itemStack.isSimilar(other);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof ItemStack) return isSimilar(((ItemStack) o));
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return itemStack.isSimilar(item.itemStack);
    }

    @Override public int hashCode() {
        return Objects.hash(itemStack);
    }

    @Getter
    public static class SlotItem extends Item {

        private final int slot;

        protected SlotItem(XMaterial type, @Nullable String displayName, List<String> lore, int count, Map<Enchantment, Integer> enchantments, ItemFlag[] itemFlags, @Nullable UUID skull, @Nullable String texture, boolean unbreakable, boolean teamColor, int slot) {
            super(type, displayName, lore, count, enchantments, itemFlags, skull, texture, unbreakable, teamColor);
            this.slot = slot;
        }

        @Override protected void giveItem(Player player) {
            player.getInventory().setItem(slot, itemStack);
        }
    }

    public static Builder fromItemStack(@NotNull ItemStack item) {
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        Builder b = Item.builder().type(XMaterial.matchXMaterial(item))
                .amount(item.getAmount())
                .name(meta.getDisplayName())
                .lore(meta.getLore())
                .itemFlags(meta.getItemFlags())
                .enchant(meta.getEnchants());
        if (meta instanceof SkullMeta) {
            b.textureValue(getSkinValue(meta));
        }
        return b;
    }

    public static ItemStack setPlaceholders(ItemStack item, Object... placeholders) {
        ItemMeta meta = Builder.n(item.getItemMeta(), "meta is null!");
        if (meta.hasDisplayName())
            meta.setDisplayName(Placeholders.on(meta.getDisplayName(), placeholders));
        if (meta.hasLore())
            meta.setLore(meta.getLore().stream().map(t -> Placeholders.on(t, placeholders)).collect(Collectors.toList()));
        item.setItemMeta(meta);
        return item;
    }

    public static class Builder {

        private XMaterial type = XMaterial.STONE;
        @Nullable private String displayName = null;
        private List<String> lore = new ArrayList<>();
        private int count = 1;
        private int slot = -1;
        private Map<Enchantment, Integer> enchantments = new HashMap<>();
        private Set<ItemFlag> itemFlags = new HashSet<>();
        @Nullable private UUID skull;
        private boolean unbreakable = false;
        private boolean teamColor = false;
        private String texture = null;


        public Builder() {
        }

        public Builder(Item item) {
            type = item.type;
            displayName = item.displayName;
            if (item.lore != null) lore = item.lore;
            count = coerce(item.count, 1, 64);
            if (item.enchantments != null) enchantments = item.enchantments;
            skull = item.skull;
            texture = item.textureValue;
            if (item.itemFlags != null) Collections.addAll(itemFlags, item.itemFlags);
            unbreakable = item.unbreakable;
            teamColor = item.teamColor;
        }

        public Builder type(@NotNull Material material) {
            this.type = XMaterial.matchXMaterial(n(material, "material"));
            return this;
        }

        public Builder type(@NotNull XMaterial material) {
            this.type = n(material, "material");
            return this;
        }

        public Builder slot(int slot) {
            Preconditions.checkArgument(slot >= 0, "slot cannot be less than 0!");
            this.slot = slot;
            return this;
        }

        public Builder name(@Nullable String name) {
            if (name == null || name.equals("{}")) return this;
            displayName = name;
            return this;
        }

        public Builder amount(int amount) {
            this.count = amount;
            return this;
        }

        public Builder loreLine(@NotNull String lore) {
            this.lore.add(n(lore, "lore"));
            return this;
        }

        public Builder lore(@Nullable Collection<String> lore) {
            if (lore == null) return this;
            this.lore.addAll(n(lore, "lore"));
            return this;
        }


        public Builder lore(@NotNull String... lore) {
            Collections.addAll(this.lore, n(lore, "lore"));
            return this;
        }

        public Builder loreV(@NotNull String... lore) {
            Collections.addAll(this.lore, n(lore, "lore"));
            this.lore.add(" ");
            this.lore.add("&fCurrent value: &d{value}");
            return this;
        }

        public Builder loreE(@NotNull String... lore) {
            Collections.addAll(this.lore, n(lore, "lore"));
            this.lore.add(" ");
            this.lore.add("{activated}");
            this.lore.add("{enabled}");
            return this;
        }

        public Builder lore(@NotNull String lore, int index) {
            this.lore.set(index, n(lore, "lore"));
            return this;
        }

        public Builder removeLore(@NotNull String lore) {
            this.lore.remove(lore);
            return this;
        }

        public Builder skull(@Nullable UUID skull) {
            this.skull = skull;
            return this;
        }

        public Builder textureValue(@Nullable String texture) {
            this.texture = texture;
            return this;
        }

        public Builder skull(@Nullable String playerName) {
            if (playerName == null) {
                this.skull = null;
                return this;
            }
            this.skull = Bukkit.getOfflinePlayer(playerName).getUniqueId();
            return this;
        }

        public Builder itemFlag(@NotNull ItemFlag itemFlag) {
            this.itemFlags.add(n(itemFlag, "itemFlag"));
            return this;
        }

        public Builder itemFlags(@NotNull Collection<ItemFlag> itemFlags) {
            this.itemFlags.addAll(n(itemFlags, "itemFlags"));
            return this;
        }

        public Builder unbreakable(boolean v) {
            unbreakable = v;
            return this;
        }

        public Builder teamColor(boolean v) {
            teamColor = v;
            return this;
        }

        public Builder enchant(@NotNull Enchantment enchantment, int level) {
            this.enchantments.put(n(enchantment, "enchantment"), level);
            return this;
        }

        public Builder enchant(@NotNull XEnchantment enchantment, int level) {
            this.enchantments.put(n(enchantment.getEnchant(), "enchantment"), level);
            return this;
        }

        public Builder enchant(@NotNull Map<Enchantment, Integer> enchantments) {
            this.enchantments.putAll(n(enchantments, "enchantments"));
            return this;
        }

        public Item build() {
            if (slot == -1)
                return new Item(type, displayName, lore, count, enchantments, itemFlags.toArray(new ItemFlag[0]), skull, texture, unbreakable, teamColor);
            else
                return new SlotItem(type, displayName, lore, count, enchantments, itemFlags.toArray(new ItemFlag[0]), skull, texture, unbreakable, teamColor, slot);
        }

        private static <T> T n(T t, String m) {
            return Objects.requireNonNull(t, m);
        }
    }

    @Getter
    public static class CommandItem extends Item {

        private final List<CommandExecution> commands;
        private final Set<Action> triggers;

        protected CommandItem(XMaterial type,
                              @Nullable String displayName,
                              List<String> lore,
                              int count,
                              Map<Enchantment, Integer> enchantments,
                              ItemFlag[] itemFlags,
                              @Nullable UUID skull,
                              @Nullable String texture,
                              boolean unbreakable, boolean teamColor,
                              List<CommandExecution> commands,
                              Set<Action> triggers) {
            super(type, displayName, lore, count, enchantments, itemFlags, skull, texture, unbreakable, teamColor);
            this.commands = commands;
            this.triggers = triggers;
        }
    }

    @NotNull
    private static SkullMeta addTexture(@NotNull SkullMeta head, @NotNull String value) {
        UUID uuid = UUID.randomUUID();
        GameProfile profile = new GameProfile(uuid, uuid.toString().substring(0, 8));
        profile.getProperties().put("textures", new Property("textures", value));

        try {
            PROFILE_SETTER.invoke(head, profile);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        return head;
    }

    @Nullable
    public static String getSkinValue(@NotNull ItemMeta skull) {
        Objects.requireNonNull(skull, "Skull ItemStack cannot be null");
        SkullMeta meta = (SkullMeta) skull;
        GameProfile profile = null;

        try {
            profile = (GameProfile) PROFILE_GETTER.invokeWithArguments(meta);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        if (profile != null && !profile.getProperties().get("textures").isEmpty()) {
            for (Property property : profile.getProperties().get("textures")) {
                if (!property.getValue().isEmpty()) return property.getValue();
            }
        }

        return null;
    }

    public static void setColor(ItemStack item, DyeColor color) {
        if (item.getType().name().contains("LEATHER_")) {
            LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
            n(meta).setColor(color.getColor());
            item.setItemMeta(meta);
            return;
        }
        if (Protocol.supports(13)) {
            String type = item.getType().name();
            int index = type.indexOf('_');
            if (index == -1) return;

            String realType = type.substring(index);
            Material material = Material.getMaterial(color.name() + '_' + realType);
            if (material == null) return;
            item.setType(material);
            return;
        }
        item.setDurability(color.getWoolData());
    }
}
