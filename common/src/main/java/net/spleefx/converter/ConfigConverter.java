/*
 * * Copyright 2020 github.com/ReflxctionDev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.spleefx.converter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class ConfigConverter implements Runnable {

    private static final String MATCHMAKING = "# MatchMaking settings.\n" +
            "#\n" +
            "# Match-making is an option to equally distribute players among teams to ensure\n" +
            "# similar levels of skill are on each team.\n" +
            "#\n" +
            "# Also distributes players on arenas in FFA games.\n" +
            "#\n" +
            "# Note that, when matchmaking is enabled, teams arenas will be unable to have per-team\n" +
            "# lobbies, since teams will be chosen only when the game is about to start, and not automatically\n" +
            "# chosen when the player joins.\n" +
            "MatchMaking:\n" +
            "\n" +
            "  # Whether is matchmaking enabled or not\n" +
            "  Enabled: true";

    private static final String PARTIES = "\n# Whether should SpleefX attempt to hook into party plugins to provide\n" +
            "# party features\n" +
            "#\n" +
            "# Supported party plugins:\n" +
            "# - FriendsPremium\n" +
            "#\n" +
            "# Default value: true\n" +
            "PartiesSupport: true\n";

    private static final String POWERUP = "\n" +
            "# Power ups settings\n" +
            "Powerups:\n" +
            "\n" +
            "  # The radius to scatter power ups around the center.\n" +
            "  #\n" +
            "  # Note that you can customize this per-arena using the following command:\n" +
            "  # /<mode> arena settings <arena> powerupsradius <value>\n" +
            "  #\n" +
            "  # Default value: 15\n" +
            "  ScatterRadius: 15\n" +
            "\n" +
            "  # The interval in seconds in which a random power up should be spawned on one of\n" +
            "  # the power blocks\n" +
            "  SpawnEvery: 45\n";

    private static final String DELAY_BETWEEN_PWUP = "\n" +
            "  # The delay (in seconds) between taking power-ups.\n" +
            "  #\n" +
            "  # This should make it harder to stack power-ups.\n" +
            "  DelayBetweenTaking: 10\n";

    private final File config;

    public ConfigConverter(File config) {
        this.config = config;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        if (!config.exists()) return;
        try {
            List<String> lines = Files.readAllLines(config.toPath());
//            if (Protocol.PROTOCOL == 8) {
//                lines.replaceAll(s -> s.replace("BLOCK_LEVER_CLICK", "CLICK"));
//            } else {
//                lines.replaceAll(s -> s.replace("\"CLICK\"", "\"BLOCK_LEVER_CLICK\""));
//            }
            if (lines.stream().noneMatch(s -> s.contains("PartiesSupport:"))) {
                lines.addAll(Arrays.asList(PARTIES.split("\n")));
            }
            if (lines.stream().noneMatch(s -> s.contains("Powerups:"))) {
                lines.addAll(Arrays.asList(POWERUP.split("\n")));
            }
            if (lines.stream().noneMatch(s -> s.contains("DelayBetweenTaking:"))) {
                lines.addAll(Arrays.asList(DELAY_BETWEEN_PWUP.split("\n")));
            }
            Files.write(config.toPath(), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
