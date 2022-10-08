package net.spleefx.core.dependency;

import com.google.common.collect.ImmutableSet;
import lombok.SneakyThrows;
import net.spleefx.classpath.URLClassLoaderAccess;
import net.spleefx.core.data.StorageType;
import net.spleefx.core.dependency.classloader.IsolatedClassLoader;
import net.spleefx.core.dependency.relocation.Relocation;
import net.spleefx.core.dependency.relocation.RelocationHandler;
import net.spleefx.util.FileManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

;

/**
 * Loads and manages runtime dependencies for the plugin.
 */
public class DependencyManager {

    private final JavaPlugin plugin;
    private final URLClassLoaderAccess loaderAccess;

    private final DependencyRegistry registry;
    private final Path cacheDirectory;
    private final EnumMap<Dependency, Path> loaded = new EnumMap<>(Dependency.class);
    private final Map<ImmutableSet<Dependency>, IsolatedClassLoader> loaders = new HashMap<>();
    private @MonotonicNonNull RelocationHandler relocationHandler = null;

    public DependencyManager(JavaPlugin plugin, URLClassLoaderAccess loaderAccess) {
        this.plugin = plugin;
        this.loaderAccess = loaderAccess;
        this.registry = new DependencyRegistry();
        this.cacheDirectory = setupCacheDirectory(plugin);
    }

    private synchronized RelocationHandler getRelocationHandler() {
        if (this.relocationHandler == null) {
            this.relocationHandler = new RelocationHandler(this);
        }
        return this.relocationHandler;
    }

    public IsolatedClassLoader obtainClassLoaderWith(Set<Dependency> dependencies) {
        ImmutableSet<Dependency> set = ImmutableSet.copyOf(dependencies);
        for (Dependency dependency : dependencies) {
            if (!this.loaded.containsKey(dependency)) {
                throw new IllegalStateException("Dependency " + dependency + " is not loaded.");
            }
        }

        synchronized (this.loaders) {
            IsolatedClassLoader classLoader = this.loaders.get(set);
            if (classLoader != null) {
                return classLoader;
            }

            URL[] urls = set.stream()
                    .map(this.loaded::get)
                    .map(file -> {
                        try {
                            return file.toUri().toURL();
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toArray(URL[]::new);

            classLoader = new IsolatedClassLoader(urls);
            this.loaders.put(set, classLoader);
            return classLoader;
        }
    }

    public void loadStorageDependencies(StorageType storageType) {
        loadDependencies(this.registry.resolveStorageDependencies(storageType));
    }

    public void loadDependencies(Set<Dependency> dependencies) {
        for (Dependency dependency : dependencies) {
            try {
                loadDependency(dependency);
            } catch (Throwable e) {
                this.plugin.getLogger().severe("Unable to load dependency " + dependency.name() + ".");
                e.printStackTrace();
            }
        }
    }

    private void loadDependency(Dependency dependency) throws Exception {
        if (this.loaded.containsKey(dependency)) {
            return;
        }

        Path file = remapDependency(dependency, downloadDependency(dependency));

        this.loaded.put(dependency, file);

        if (this.registry.shouldAutoLoad(dependency)) {
            loaderAccess.addURL(file.toUri().toURL());
        }
    }

    @SneakyThrows private Path downloadDependency(Dependency dependency) throws DependencyDownloadException {
        Path file = this.cacheDirectory.resolve(dependency.getFileName() + ".jar");

        // if the file already exists, don't attempt to re-download it.
        if (Files.exists(file)) {
            return file;
        }

        DependencyDownloadException lastError;

        try {
            file.toFile().createNewFile();
            dependency.download(file);
            return file;
        } catch (DependencyDownloadException e) {
            lastError = e;
        }

        throw Objects.requireNonNull(lastError);
    }

    private Path remapDependency(Dependency dependency, Path normalFile) throws Exception {
        List<Relocation> rules = new ArrayList<>(dependency.getRelocations());

        if (rules.isEmpty()) {
            return normalFile;
        }

        Path remappedFile = this.cacheDirectory.resolve(dependency.getFileName() + "-remapped.jar");

        // if the remapped source exists already, just use that.
        if (Files.exists(remappedFile)) {
            return remappedFile;
        }

        getRelocationHandler().remap(normalFile, remappedFile, rules);
        return remappedFile;
    }

    @SneakyThrows private static Path setupCacheDirectory(JavaPlugin plugin) {
        File cacheDirectory = plugin.getDataFolder().toPath().resolve("libs").toFile();
        FileManager.forceMkdir(cacheDirectory);
        File oldCacheDirectory = plugin.getDataFolder().toPath().resolve("lib").toFile();
        if (oldCacheDirectory.exists()) {
            oldCacheDirectory.delete();
        }

        return cacheDirectory.toPath();
    }

}
