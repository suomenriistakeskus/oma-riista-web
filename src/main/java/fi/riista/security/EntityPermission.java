package fi.riista.security;

import java.util.EnumSet;

public enum EntityPermission {

    NONE,
    CREATE,
    READ,
    UPDATE,
    DELETE;

    public static EnumSet<EntityPermission> crud() {
        return EnumSet.of(CREATE, READ, UPDATE, DELETE);
    }

    public static EnumSet<EntityPermission> none() {
        return EnumSet.noneOf(EntityPermission.class);
    }
}
