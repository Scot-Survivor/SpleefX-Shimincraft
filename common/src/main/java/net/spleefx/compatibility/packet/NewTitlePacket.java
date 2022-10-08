/*
 * PacketWrapper - ProtocolLib wrappers for Minecraft packets
 * Copyright (C) dmulloy2 <http://dmulloy2.net>
 * Copyright (C) Kristian S. Strangeland
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.spleefx.compatibility.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

public class NewTitlePacket {

    public static final PacketType TITLE = Server.SET_TITLE_TEXT;
    public static final PacketType SUBTITLE = Server.SET_SUBTITLE_TEXT;
    public static final PacketType DURATION = Server.SET_TITLES_ANIMATION;
    public static final PacketType RESET = Server.CLEAR_TITLES;

    public static AbstractPacket title(WrappedChatComponent chatComponent) {
        return new AbstractPacket(new PacketContainer(TITLE), TITLE) {
            {
                getHandle().getChatComponents().write(0, chatComponent);
            }
        };
    }

    public static AbstractPacket subtitle(WrappedChatComponent chatComponent) {
        return new AbstractPacket(new PacketContainer(SUBTITLE), SUBTITLE) {
            {
                getHandle().getChatComponents().write(0, chatComponent);
            }
        };
    }

    public static AbstractPacket duration(int fadeIn, int stay, int fadeOut) {
        return new AbstractPacket(new PacketContainer(DURATION), DURATION) {
            {
                getHandle().getIntegers().write(0, fadeIn);
                getHandle().getIntegers().write(1, stay);
                getHandle().getIntegers().write(2, fadeOut);
            }
        };
    }

    public static AbstractPacket reset() {
        return new AbstractPacket(new PacketContainer(RESET), RESET) {};
    }

}