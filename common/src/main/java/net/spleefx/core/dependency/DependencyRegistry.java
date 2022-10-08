package net.spleefx.core.dependency;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import net.spleefx.core.data.StorageType;

import java.util.LinkedHashSet;
import java.util.Set;

public class DependencyRegistry {

    private static final ListMultimap<StorageType, Dependency> STORAGE_DEPENDENCIES = ImmutableListMultimap.<StorageType, Dependency>builder()
            .putAll(StorageType.MARIADB, Dependency.MARIADB_DRIVER, Dependency.SLF4J_API, Dependency.SLF4J_SIMPLE, Dependency.HIKARI)
            .putAll(StorageType.MYSQL, Dependency.MYSQL_DRIVER, Dependency.SLF4J_API, Dependency.SLF4J_SIMPLE, Dependency.HIKARI)
            .putAll(StorageType.POSTGRESQL, Dependency.POSTGRESQL_DRIVER, Dependency.SLF4J_API, Dependency.SLF4J_SIMPLE, Dependency.HIKARI)
            .putAll(StorageType.H2, Dependency.H2_DRIVER)
            .build();

    public Set<Dependency> resolveStorageDependencies(StorageType storageType) {
        Set<Dependency> dependencies = new LinkedHashSet<>(STORAGE_DEPENDENCIES.get(storageType));

        // don't load slf4j if it's already present
        if ((dependencies.contains(Dependency.SLF4J_API) || dependencies.contains(Dependency.SLF4J_SIMPLE)) && slf4jPresent()) {
            dependencies.remove(Dependency.SLF4J_API);
            dependencies.remove(Dependency.SLF4J_SIMPLE);
        }

        return dependencies;
    }

    public boolean shouldAutoLoad(Dependency dependency) {
        switch (dependency) {
            // all used within 'isolated' classloaders, and are therefore not
            // relocated.
            case ASM:
            case ASM_COMMONS:
            case JAR_RELOCATOR:
//            case H2_DRIVER:
            case SQLITE_DRIVER:
                return false;
            default:
                return true;
        }
    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean slf4jPresent() {
        return classExists("org.slf4j.Logger") && classExists("org.slf4j.LoggerFactory");
    }

}
