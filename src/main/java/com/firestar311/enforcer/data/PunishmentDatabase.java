package com.firestar311.enforcer.data;

import com.firestar311.lib.Database;

public class PunishmentDatabase extends Database {
    public PunishmentDatabase(String user, String database, String password, int port, String hostname) {
        super(user, database, password, port, hostname);
    }
    
    public void createTables() {
    
    }
}
