/*
 * * Copyright 2019 github.com/ReflxctionDev
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
package net.spleefx.util.paginate;

import net.spleefx.compatibility.chat.ChatComponent;
import net.spleefx.compatibility.chat.ChatEvents.ClickEvent;
import net.spleefx.compatibility.chat.ChatEvents.HoverEvent;
import net.spleefx.compatibility.chat.ComponentJSON;
import net.spleefx.util.paginate.ListPaginator.Footer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class PageFooter implements Footer {

    private String command;
    private ComponentJSON json;

    public PageFooter(Command parent, String command, ComponentJSON json) {
        this.command = StringUtils.normalizeSpace(parent.getName() + " help " + command);
        this.json = json;
    }

    /**
     * The dashes surrounding the back/next buttons
     */
    private static final ChatComponent DASHES = new ChatComponent()
            .setText("&c&m---------&r ");

    /**
     * The << arrows
     */
    private static final ChatComponent BACK = new ChatComponent()
            .setText("&7&l<<")
            .setHoverAction(HoverEvent.SHOW_TEXT, "This is the first page.");

    /**
     * The >> arrows
     */
    private static final ChatComponent NEXT = new ChatComponent()
            .setText("&7&l>>")
            .setHoverAction(HoverEvent.SHOW_TEXT, "This is the last page");

    /**
     * The separator between the back/next buttons
     */
    private static final ChatComponent SEPARATOR = new ChatComponent()
            .setText("§r &eI ");

    @Override
    public void sendFooter(CommandSender sender, int pageIndex, int pageCount) {
        if (pageIndex == 1 && pageCount == 1) { // First and only page
            json.clear().append(DASHES).append(BACK).append(SEPARATOR).append(NEXT).space().append(DASHES).send(sender);
        } else if (pageIndex == 1) { // First page (not last)
            ChatComponent next = new ChatComponent()
                    .setText("&a&l>>")
                    .setHoverAction(HoverEvent.SHOW_TEXT, "Next")
                    .setClickAction(ClickEvent.RUN_COMMAND, "/" + command + " " + (pageIndex + 1));
            json.clear().append(DASHES).append(BACK).append(SEPARATOR).append(next).space().append(DASHES).send(sender);
        } else if (pageIndex == pageCount) { // Last page (not first)
            ChatComponent back = new ChatComponent()
                    .setText("&a&l<<")
                    .setHoverAction(HoverEvent.SHOW_TEXT, "Back")
                    .setClickAction(ClickEvent.RUN_COMMAND, "/" + command + " " + (pageIndex - 1));
            json.clear().append(DASHES).append(back).append(SEPARATOR).append(NEXT).space().append(DASHES).send(sender);
        } else { // A page in the middle
            ChatComponent back = new ChatComponent()
                    .setText("&a&l<<")
                    .setHoverAction(HoverEvent.SHOW_TEXT, "Back")
                    .setClickAction(ClickEvent.RUN_COMMAND, "/" + command + " " + (pageIndex - 1));
            ChatComponent next = new ChatComponent()
                    .setText("&a&l>>")
                    .setHoverAction(HoverEvent.SHOW_TEXT, "Next")
                    .setClickAction(ClickEvent.RUN_COMMAND, "/" + command + " " + (pageIndex + 1));
            json.clear().append(DASHES).append(back).append(SEPARATOR).append(next).space().append(DASHES).send(sender);
        }
    }
}