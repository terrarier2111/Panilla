package com.ruinscraft.panilla.plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.ruinscraft.panilla.api.config.PConfig;
import com.ruinscraft.panilla.api.config.PStrictness;
import com.ruinscraft.panilla.api.exception.UnsupportedMinecraftVersionException;
import com.ruinscraft.panilla.api.io.IPacketInspector;
import com.ruinscraft.panilla.api.io.IPlayerInjector;
import com.ruinscraft.panilla.api.io.IProtocolConstants;

public class PanillaPlugin extends JavaPlugin {

	private PConfig panillaConfig;
	private IProtocolConstants protocolConstants;
	private IPacketInspector packetInspector;
	private IPlayerInjector playerInjector;

	public PConfig getPanillaConfig() {
		return panillaConfig;
	}

	public IProtocolConstants getProtocolConstants() {
		return protocolConstants;
	}

	public IPacketInspector getPacketInspector() {
		return packetInspector;
	}

	public IPlayerInjector getPlayerInjector() {
		return playerInjector;
	}

	private synchronized void loadConfig() {
		saveDefaultConfig();

		panillaConfig = new PConfig();

		panillaConfig.consoleLogging = getConfig().getBoolean("logging.console");
		panillaConfig.chatLogging = getConfig().getBoolean("logging.chat");
		panillaConfig.strictness = PStrictness.valueOf(getConfig().getString("strictness").toUpperCase());
		panillaConfig.nbtWhitelist = getConfig().getStringList("nbt-whitelist");
		panillaConfig.packetWhitelist = getConfig().getIntegerList("packet-whitelist");
	}

	private String v_Version() {
		return 	getServer().
				getClass().
				getPackage().
				getName().
				substring("org.bukkit.craftbukkit.".length());
	}

	@Override
	public void onEnable() {
		singleton = this;

		loadConfig();

		final String v_Version = v_Version();

		try {
			switch (v_Version) {
			case "v1_12_R1":
				protocolConstants = new com.ruinscraft.panilla.v1_12_R1.ProtocolConstants();
				packetInspector = new com.ruinscraft.panilla.v1_12_R1.PacketInspector(protocolConstants);
				playerInjector = new com.ruinscraft.panilla.v1_12_R1.PlayerInjector(packetInspector);
				break;
			case "v1_13_R2":
				// TODO: impl 1.13.2
				break;
			default:
				throw new UnsupportedMinecraftVersionException(v_Version);
			}
		} catch (UnsupportedMinecraftVersionException e) {
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		/* Register listeners */
		getServer().getPluginManager().registerEvents(new JoinQuitListener(), this);

		for (Player player : Bukkit.getOnlinePlayers()) {
			playerInjector.register(player);
		}
	}

	@Override
	public void onDisable() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			playerInjector.unregister(player);
		}

		singleton = null;
	}

	/* ========================================== static ========================================== */
	private static PanillaPlugin singleton;

	public static PanillaPlugin get() {
		return singleton;
	}
	/* ========================================== static ========================================== */

}
