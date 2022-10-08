///*
// * * Copyright 2020 github.com/ReflxctionDev
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package net.spleefx.converter;
//
//import net.spleefx.config.json.MappedConfiguration;
//import net.spleefx.json.SpleefXGson;
//import net.spleefx.model.GracePeriod;
//
//import java.io.File;
//import java.util.*;
//
//import static net.spleefx.util.FileManager.getExtension;
//
///**
// * A task that converts files from the old extensions format to the newer one. Useful when updating to avoid breaking
// * backwards compatibility
// */
//public class LegacyExtensionConverter implements Runnable {
//
//    private final File extensionsDirectory;
//
//    public LegacyExtensionConverter(File extensionsDirectory) {
//        this.extensionsDirectory = extensionsDirectory;
//    }
//
//    /**
//     * When an object implementing interface <code>Runnable</code> is used
//     * to create a thread, starting the thread causes the object's
//     * <code>run</code> method to be called in that separately executing
//     * thread.
//     * <p>
//     * The general contract of the method <code>run</code> is that it may
//     * take any action whatsoever.
//     *
//     * @see Thread#run()
//     */
//    @Override
//    public void run() {
//        if (!extensionsDirectory.exists()) return; // There are no files to convert
//        Arrays.stream(Objects.requireNonNull(extensionsDirectory.listFiles())).forEach(this::convert);
//    }
//
//    private void convert(File file) {
//        if (file.isFile()) {
//            if (!getExtension(file).equals("yml"))
//                return; // Ignored file
//            boolean changed = false;
//            MappedConfiguration d = new MappedConfiguration(file);
//            if (!d.contains("RemoveBlocksWhenPunched")) {
//                List<String> list = new ArrayList<>();
//                list.add("SNOW_BLOCK");
//                d.set("RemoveBlocksWhenPunched", list);
//                changed = true;
//            }
//            if (!d.contains("RunCommandsOnTeamWin")) {
//                d.set("RunCommandsOnTeamWin", new ArrayList<>());
//                changed = true;
//            }
//            if (!d.contains("RunCommandsOnTeamLose")) {
//                d.set("RunCommandsOnTeamLose", new ArrayList<>());
//                changed = true;
//            }
//            if (!d.contains("GracePeriod")) {
//                d.set("GracePeriod", SpleefXGson.toMap(new GracePeriod()));
//                changed = true;
//            }
//            if (!d.contains("PlayersBlockProjectiles")) {
//                d.set("PlayersBlockProjectiles", true);
//                changed = true;
//            }
//            if (!d.contains("CommandItemsToAddInWaiting")) {
//                d.set("CommandItemsToAddInWaiting", new HashMap<>());
//                d.set("CommandItemsToAddInGame", new HashMap<>());
//                changed = true;
//            }
//            if (!d.contains("DenyOpeningContainers")) {
//                d.set("DenyOpeningContainers", true);
//                d.set("DenyCrafting", true);
//                changed = true;
//            }
//            if (changed) {
//                d.save();
//            }
//        } else { // it is a directory not a file, so convert recursively
//            File[] files = file.listFiles();
//            if (files == null) return;
//            for (File old : files) {
//                convert(old);
//            }
//        }
//    }
//}