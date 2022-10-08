package net.spleefx.util.watcher;//package net.spleefx.util.watcher;
//
//import net.spleefx.SpleefX;
//import org.bukkit.Bukkit;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.*;
//import java.nio.file.attribute.BasicFileAttributes;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.function.Consumer;
//
//public class FileWatcher {
//
//    private static final ScheduledExecutorService WATCHER_SERVICE = Executors.newSingleThreadScheduledExecutor();
//    private static final Map<String, FileWatcher> WATCHERS = Collections.synchronizedMap(new HashMap<>());
//    private static final WatchService SERVICE;
//
//    private Consumer<Path> task;
//    private final AtomicInteger eventTrigger = new AtomicInteger();
//    private final String name;
//    private final File file;
//
//    private FileWatcher(File file) {
//        this.file = file;
//        WATCHERS.put(name = file.getName(), this);
//    }
//
//    public FileWatcher onChange(Consumer<Path> task) {
//        this.task = path -> Bukkit.getScheduler().runTask(SpleefX.getPlugin(), () -> task.accept(path));
//        return this;
//    }
//
//    public File getFile() {
//        return file;
//    }
//
//    public static FileWatcher watch(File file) {
//        return new FileWatcher(file);
//    }
//
//    @Override public String toString() {
//        return name;
//    }
//
//    public static void pollDirectory(Path directory) {
//        //if (true) return;
//        try {
//            registerAll(directory);
//            WATCHER_SERVICE.submit(() -> {
//                while (true) {
//                    try {
//                        WatchKey watchKey = SERVICE.take();
//                        for (WatchEvent<?> event : watchKey.pollEvents()) {
//                            Path modified = (Path) event.context();
//                            FileWatcher watcher = WATCHERS.get(modified.getFileName().toString());
//                            if (watcher == null) {
//                                continue;
//                            }
//                            if (watcher.eventTrigger.getAndIncrement() % 2 == 0) {
//                                SpleefX.logger().info("Detected changes in file " + watcher + ". Reloading.");
//                                Bukkit.getScheduler().runTask(SpleefX.getPlugin(), () -> {
//                                    try {
//                                        watcher.task.accept(modified);
//                                    } catch (Throwable e) {
//                                        e.printStackTrace();
//                                    }
//                                });
//                            }
//                        }
//                        watchKey.reset();
//                    } catch (InterruptedException ignored) {
//                        // concurrency issues when the server shuts down. nothing happens really soo
//                    }
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void registerAll(final Path start) throws IOException {
//        // register directory and sub-directories
//        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
//            @Override
//            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//                dir.register(SERVICE, StandardWatchEventKinds.ENTRY_MODIFY);
//                return FileVisitResult.CONTINUE;
//            }
//        });
//
//    }
//
//    public static ScheduledExecutorService getPool() {
//        return WATCHER_SERVICE;
//    }
//
//    public static WatchService getService() {
//        return SERVICE;
//    }
//
//    static {
//        try {
//            SERVICE = FileSystems.getDefault().newWatchService();
//        } catch (IOException e) {
//            throw new IllegalStateException(e);
//        }
//    }
//}
