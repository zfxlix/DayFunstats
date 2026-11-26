package eu.dayfun.stats;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class DayFunStats extends JavaPlugin implements Listener {
    private static final String MENU_TITLE = "§8DayFun §7┃ §bSpielerprofil";
    private static final int[] BORDER = {
            0,1,2,3,4,5,6,7,8, 9,17, 18,26, 27,35, 36,37,38,39,40,41,42,43,44
    };

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("DayFunStats 2.0 aktiviert.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("stats")) {
            if (args.length == 0) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(color("&cDieser Befehl ist nur ingame verfügbar."));
                    return true;
                }
                openStats(player, player);
                return true;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("dayfunstats.admin")) return noPermission(sender);
                reloadConfig();
                sender.sendMessage(color("&aDayFunStats wurde neu geladen."));
                return true;
            }
            if (args[0].equalsIgnoreCase("reset")) {
                if (!sender.hasPermission("dayfunstats.admin")) return noPermission(sender);
                Player target = args.length >= 2 ? Bukkit.getPlayerExact(args[1]) : (sender instanceof Player p ? p : null);
                if (target == null) {
                    sender.sendMessage(color("&cBenutzung: /stats reset <Spieler> (oder als Spieler /stats reset)"));
                    return true;
                }
                resetStats(target);
                sender.sendMessage(color("&aStatistiken von &f" + target.getName() + " &awurden zurückgesetzt."));
                if (target != sender) target.sendMessage(color("&eDeine Statistiken wurden vom Team zurückgesetzt."));
                return true;
            }
            if (!(sender instanceof Player viewer)) {
                sender.sendMessage(color("&cDieser Befehl ist nur ingame verfügbar."));
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                viewer.sendMessage(color("&cDieser Spieler ist nicht online."));
                return true;
            }
            openStats(viewer, target);
            return true;
        }
        return false;
    }

    private boolean noPermission(CommandSender sender) {
        sender.sendMessage(color("&cDafür hast du keine Berechtigung."));
        return true;
    }

    private void openStats(Player viewer, Player target) {
        Inventory inv = Bukkit.createInventory(null, 45, MENU_TITLE);
        ItemStack filler = item(Material.BLACK_STAINED_GLASS_PANE, "&8 ", List.of());
        for (int slot : BORDER) inv.setItem(slot, filler);

        int kills = safeStat(target, Statistic.PLAYER_KILLS) + safeStat(target, Statistic.MOB_KILLS);
        int deaths = safeStat(target, Statistic.DEATHS);
        int ticks = safeStat(target, Statistic.PLAY_ONE_MINUTE);
        double kd = deaths == 0 ? kills : (double) kills / deaths;

        inv.setItem(20, item(Material.GOLD_INGOT, "&6&lKontostand", List.of(
                "&7Dein aktuelles Guthaben:", "&8» &e" + getMoney(target), "", "&8Economy: &7EssentialsX"
        )));
        inv.setItem(22, item(Material.DIAMOND_SWORD, "&b&lKills", List.of(
                "&7Spieler- und Mob-Kills", "&8» &b" + formatNumber(kills)
        )));
        inv.setItem(24, item(Material.SKELETON_SKULL, "&c&lTode", List.of(
                "&7Gesamte Tode", "&8» &c" + formatNumber(deaths)
        )));
        inv.setItem(30, item(Material.CLOCK, "&d&lSpielzeit", List.of(
                "&7Zeit auf DayFun", "&8» &d" + formatPlayTime(ticks)
        )));
        inv.setItem(32, item(Material.NETHER_STAR, "&a&lKill-/Death-Ratio", List.of(
                "&7Dein K/D-Verhältnis", "&8» &a" + new DecimalFormat("0.00").format(kd).replace('.', ',')
        )));
        inv.setItem(13, item(Material.PLAYER_HEAD, "&f&l" + target.getName(), List.of(
                "&7Spielerprofil auf &bDayFun", "", "&7Rang: &f" + getRank(target),
                "&7Status: &aOnline"
        )));
        viewer.openInventory(inv);
    }

    private String getMoney(Player player) {
        // EssentialsX Economy stellt seinen Kontostand über Vault bereit.
        // Die Vault-Expansion von PlaceholderAPI liest genau diesen Provider aus.
        return placeholder(player, getConfig().getString("placeholders.money", "%vault_eco_balance_formatted%"));
    }

    private String getRank(Player player) {
        return placeholder(player, getConfig().getString("placeholders.rank", "%luckperms_primary_group_name%"));
    }

    private String placeholder(Player player, String input) {
        String fallback = getConfig().getString("placeholders.unavailable-text", "Nicht verfügbar");
        if (input == null || input.isBlank()) return color(fallback);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) return color(fallback);
        try {
            Class<?> papi = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            Object result = papi.getMethod("setPlaceholders", Player.class, String.class).invoke(null, player, input);
            String value = String.valueOf(result);
            if (value.equals(input) || value.isBlank()) return color(fallback);
            return color(value);
        } catch (ReflectiveOperationException ex) {
            getLogger().warning("PlaceholderAPI konnte einen Wert nicht auflösen: " + ex.getMessage());
            return color(fallback);
        }
    }

    private void resetStats(Player player) {
        player.setStatistic(Statistic.PLAYER_KILLS, 0);
        player.setStatistic(Statistic.MOB_KILLS, 0);
        player.setStatistic(Statistic.DEATHS, 0);
        player.setStatistic(Statistic.PLAY_ONE_MINUTE, 0);
    }

    private int safeStat(Player player, Statistic statistic) {
        try { return Math.max(0, player.getStatistic(statistic)); }
        catch (IllegalArgumentException ex) { return 0; }
    }

    private String formatPlayTime(int ticks) {
        long seconds = Math.max(0L, ticks) / 20L;
        long days = seconds / 86400; seconds %= 86400;
        long hours = seconds / 3600; seconds %= 3600;
        long minutes = seconds / 60;
        if (days > 0) return days + " T. " + hours + " Std.";
        if (hours > 0) return hours + " Std. " + minutes + " Min.";
        return minutes + " Minuten";
    }

    private String formatNumber(int number) {
        return String.format(Locale.GERMANY, "%,d", number).replace('.', '.');
    }

    private ItemStack item(Material material, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color(name));
            List<String> colored = new ArrayList<>();
            for (String line : lore) colored.add(color(line));
            meta.setLore(colored);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private String color(String text) { return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text); }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(MENU_TITLE)) event.setCancelled(true);
    }
}
