package club.dlstudio.mical.diedpunishment;

import HamsterYDS.UntilTheEnd.UntilTheEnd;
import HamsterYDS.UntilTheEnd.player.death.DeathCause;
import io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public final class DiedPunishment extends JavaPlugin implements Listener {
    private ArrayList<Player> users = new ArrayList<>();
    private UntilTheEnd ute;
    private Economy eco;
    private int punishmentTicks = 0;
    private String coldnessCause;
    private String hotnessCause;
    private String darknessCause;
    private String beeMineCause;
    private String toothTrapCause;
    private String blowArrowCause;
    private String invalidSleepnessCause;
    private String dropMoneyMessage;
    private String getMoneyFromBatMessage;
    private String voidCause;
    private String fallCause;
    private String fallingBlockCause;
    private String suicideCause;
    private TitleManagerAPI tmApi;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        punishmentTicks = getConfig().getInt("pubnishment-Ticks", 12000);
        coldnessCause = getConfig().getString("coldnessCause");
        hotnessCause = getConfig().getString("hotnessCause");
        darknessCause = getConfig().getString("darknessCause");
        beeMineCause = getConfig().getString("beeMineCause");
        toothTrapCause = getConfig().getString("toothTrapCause");
        blowArrowCause = getConfig().getString("blowArrowCause");
        invalidSleepnessCause = getConfig().getString("invaildSleepnessCause");
        dropMoneyMessage = getConfig().getString("dropMoneyMessage");
        getMoneyFromBatMessage = getConfig().getString("getMoneyFromBatMessage");
        voidCause = getConfig().getString("voidCause");
        fallCause = getConfig().getString("fallCause");
        fallingBlockCause = getConfig().getString("fallingBlockCause");
        suicideCause = getConfig().getString("suicideCause");
        RegisteredServiceProvider<Economy> economyRegisteredServiceProvider = getServer().getServicesManager().getRegistration(Economy.class);
        eco = economyRegisteredServiceProvider.getProvider();
        Plugin utePlugin = Bukkit.getPluginManager().getPlugin("UntilTheEnd");
        if (utePlugin == null) {
            getLogger().severe("Must have UntilTheEnd to run plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        ute = UntilTheEnd.getInstance();
        Plugin tmApiPlugin = Bukkit.getPluginManager().getPlugin("TitleManager");
        if (tmApiPlugin == null) {
            getLogger().severe("Must have TitleManager to run plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        tmApi = (TitleManagerAPI) tmApiPlugin;
        Bukkit.getPluginManager().registerEvents(this, this);
        new BukkitRunnable() {
            @Override
            public void run() {
                ((ArrayList<Player>) users.clone()).forEach(player -> {
                    if (!tmApi.hasScoreboard(player)) {
                        tmApi.giveScoreboard(player);
                    }
                    int ticks = punishmentTicks - player.getStatistic(Statistic.TIME_SINCE_DEATH);
                    tmApi.setScoreboardTitle(player, color("&c死亡惩罚"));
                    tmApi.setScoreboardValue(player, 2, "");
                    tmApi.setScoreboardValue(player, 3, color("&6收到的伤害双倍"));
                    tmApi.setScoreboardValue(player, 4, "");
                    tmApi.setScoreboardValue(player, 5, "&e剩余 &c" + (ticks / 20) / 60 + " &e分钟");
                    tmApi.setScoreboardValue(player, 6, "");
                    if (ticks < 1) {
                        tmApi.removeScoreboard(player);
                        users.remove(player);
                    }
                });
            }
        }.runTaskTimerAsynchronously(this, 0, 20);
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.getStatistic(Statistic.TIME_SINCE_DEATH) < punishmentTicks) {
                users.add(player);
            }
        });
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        HashMap<String, DeathCause> causes = new HashMap<>();
        Player user = event.getEntity();
        if (causes.containsKey(user.getName())) {
            DeathCause cause = causes.get(user.getName());
            switch (cause) {
                case COLDNESS:
                    user.sendMessage(color(coldnessCause));
                    break;
                case HOTNESS:
                    user.sendMessage(color(hotnessCause));
                    break;
                case DARKNESS:
                    user.sendMessage(color(darknessCause));
                    break;
                case BEEMINE:
                    user.sendMessage(color(beeMineCause));
                    break;
                case TOOTHTRAP:
                    user.sendMessage(color(toothTrapCause));
                    break;
                case BLOWARROW:
                    user.sendMessage(color(blowArrowCause));
                    break;
                case INVALIDSLEEPNESS:
                    user.sendMessage(color(invalidSleepnessCause));
                    break;
            }
            causes.remove(user.getName());
            users.add(user);
        }
        if (users.contains(user)) {
            event.setKeepInventory(true);
            event.setKeepLevel(false);
        }
        double money = eco.getBalance(user) * 0.1;
        double currentMoney = eco.getBalance(user);
        if (currentMoney > 0) {
            eco.withdrawPlayer(user, money);
            World world = user.getWorld();
            Bat bat = (Bat) world.spawnEntity(user.getLocation(), EntityType.BAT);
            bat.setCustomNameVisible(true);
            bat.setCustomName(ChatColor.GOLD + String.valueOf(money));
            user.sendMessage(color(dropMoneyMessage.replace("{money}", String.valueOf(money)).replace("{location}", formatLocation(user.getLocation()))));
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player user = event.getEntity();
        EntityDamageEvent.DamageCause cause = user.getLastDamageCause().getCause();
        switch (cause) {
            case VOID:
                user.sendMessage(color(voidCause));
                users.add(user);
                break;
            case FALL:
                user.sendMessage(color(fallCause));
                users.add(user);
                break;
            case FALLING_BLOCK:
                user.sendMessage(color(fallingBlockCause));
                users.add(user);
                break;
            case SUICIDE:
                user.sendMessage(color(suicideCause));
                users.add(user);
                break;
        }
        if (users.contains(user)) {
            event.setKeepInventory(true);
            event.setKeepLevel(false);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Player user = (Player) event.getDamager();
        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.BAT) {
            return;
        } else {
            String customName = entity.getCustomName();
            double money = Double.parseDouble(customName);
            if (entity.isDead()) {
                eco.depositPlayer(user, money);
                user.sendMessage(color(getMoneyFromBatMessage));
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player user = (Player) event.getEntity();
        if (users.contains(user)) {
            double finalDamage = event.getFinalDamage();
            if (finalDamage == 0)
                return;
            event.setDamage(finalDamage * 2);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String formatLocation(Location location) {
        String result = "&c" + location.getBlockX() + "&7, &c" + location.getBlockY() + "&7, &c" + location.getBlockZ() + "&7";
        return color(result);
    }
}
