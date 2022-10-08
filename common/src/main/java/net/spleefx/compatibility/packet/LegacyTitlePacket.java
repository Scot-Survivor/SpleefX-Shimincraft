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
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.TitleAction;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

public class LegacyTitlePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.TITLE;

    public LegacyTitlePacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public LegacyTitlePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public LegacyTitlePacket setAction(TitleAction value) {
        handle.getTitleActions().write(0, value);
        return this;
    }

    public LegacyTitlePacket setTitle(WrappedChatComponent value) {
        handle.getChatComponents().write(0, value);
        return this;
    }

    public int getFadeIn() {
        return handle.getIntegers().read(0);
    }

    public LegacyTitlePacket setFadeIn(int value) {
        handle.getIntegers().write(0, value);
        return this;
    }

    public int getStay() {
        return handle.getIntegers().read(1);
    }

    public LegacyTitlePacket setStay(int value) {
        handle.getIntegers().write(1, value);
        return this;
    }

    public int getFadeOut() {
        return handle.getIntegers().read(2);
    }

    public LegacyTitlePacket setFadeOut(int value) {
        handle.getIntegers().write(2, value);
        return this;
    }
}