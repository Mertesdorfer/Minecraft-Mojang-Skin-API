package com.mojang.api;

import java.io.IOException;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import me.arvnar.system.plugin.Main;
import me.arvnar.system.util.Prefixes;

public class PacketHandler {

	public static void SkinChange(CraftPlayer cp, String NamefromPlayer) {

		GameProfile Skingp = cp.getProfile();

		try {

			Skingp = GameProfileBuilder.fetch(UUIDFetscher.getUUID(NamefromPlayer));

		} catch (IOException e) {

			cp.sendMessage("§cDer Skin konnte nicht geladen werden!");
			e.printStackTrace();

			return;
		}

		Collection<Property> props = Skingp.getProperties().get("textures");

		cp.getProfile().getProperties().removeAll("textures");
		cp.getProfile().getProperties().putAll("textures", props);
		cp.sendMessage("§7Du hast jetzt den Skin von " + NamefromPlayer);

		PacketPlayOutEntityDestroy deletePlayer = new PacketPlayOutEntityDestroy(cp.getEntityId());
		SendPacket(deletePlayer);

		PacketPlayOutPlayerInfo RemovefromTab = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, cp.getHandle());
		SendPacket(RemovefromTab);

		new BukkitRunnable() {
			
			public void run() {

				PacketPlayOutPlayerInfo AddtoTablist = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, cp.getHandle());
				SendPacket(AddtoTablist);

				PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(cp.getHandle());

				for (Player all : Bukkit.getOnlinePlayers()) {

					if (!all.getName().equals(cp.getName()))

					((CraftPlayer) all).getHandle().playerConnection.sendPacket(spawn);
				}

			}

		}.runTaskLater(Main.getInstance(), 4);
	}

	public static void SendPacket(net.minecraft.server.v1_9_R1.Packet<?> packet) {

		for (Player all : Bukkit.getOnlinePlayers()) {

			((CraftPlayer) all).getHandle().playerConnection.sendPacket(packet);
		}
	}
}
