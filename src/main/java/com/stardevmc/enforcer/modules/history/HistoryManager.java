package com.stardevmc.enforcer.modules.history;

import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.base.Manager;
import com.stardevmc.enforcer.modules.punishments.actor.PlayerActor;
import com.stardevmc.enforcer.modules.punishments.type.abstraction.Punishment;
import com.stardevmc.enforcer.util.EnforcerUtils;
import com.firestar311.lib.pagination.Paginator;
import com.firestar311.lib.player.PlayerInfo;

import java.util.*;

public class HistoryManager extends Manager {
    
    private Map<UUID, Paginator<Punishment>> historyPaginators = new HashMap<>();
    private Map<UUID, Paginator<Punishment>> staffHistoryPaginators = new HashMap<>();
    
    public HistoryManager(Enforcer plugin) {
        super(plugin, "history", false);
    }
    
    public Paginator<Punishment> generateHistoryPaginator(UUID requester, String t) {
        PlayerInfo info = plugin.getPlayerManager().getPlayerInfo(t);
        if (info == null) return null;
        UUID target = info.getUuid();
        List<Punishment> playerPunishments = new LinkedList<>(plugin.getPunishmentModule().getManager().getPunishments(target));
        Paginator<Punishment> paginator = EnforcerUtils.generatePaginatedPunishmentList(playerPunishments, "&7-=History of " + info.getLastName() + "=- &e({pagenumber}/{totalpages})", "&7Type /staffhistory page {nextpage} for more");
        this.historyPaginators.put(requester, paginator);
        return paginator;
    }
    
    public boolean hasLookupRegularHistory(UUID uuid) {
        return this.historyPaginators.containsKey(uuid);
    }
    
    public Paginator<Punishment> getRegularResults(UUID uuid) {
        return this.historyPaginators.get(uuid);
    }
    
    public Paginator<Punishment> generateStaffHistoryPaginator(UUID requester) {
        PlayerInfo info = plugin.getPlayerManager().getPlayerInfo(requester);
        if (info == null) return null;
    
        List<Punishment> allPunishments = new ArrayList<>(plugin.getPunishmentModule().getManager().getPunishments());
        List<Punishment> staffPunishments = new LinkedList<>();
        allPunishments.forEach(punishment -> {
            if (punishment.getPunisher() instanceof PlayerActor) {
                PlayerActor playerActor = ((PlayerActor) punishment.getPunisher());
                if (playerActor.getUniqueId().equals(info.getUuid())) {
                    staffPunishments.add(punishment);
                }
            }
        });
    
        Paginator<Punishment> paginator = EnforcerUtils.generatePaginatedPunishmentList(staffPunishments, "&7-=Staff History of " + info.getLastName() + "=- &e({pagenumber}/{totalpages})", "&7Type /staffhistory page {nextpage} for more");
        this.staffHistoryPaginators.put(requester, paginator);
        return paginator;
    }
    
    public boolean hasLookupStaffHistory(UUID uuid) {
        return this.staffHistoryPaginators.containsKey(uuid);
    }
    
    public Paginator<Punishment> getStaffResults(UUID uuid) {
        return this.staffHistoryPaginators.get(uuid);
    }
    
    public void saveData() {
    
    }
    
    public void loadData() {
    
    }
}