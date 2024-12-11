package br.com.devpaulo.legendchat.ignore;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import br.com.devpaulo.legendchat.channels.types.Channel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.*;

public class IgnoreManager {

	private final Map<UUID, List<UUID>> IgnoreList = new HashMap<>();
	private final Map<UUID, List<String>> MuteList = new HashMap<>();
	private final File dataFile = new File("plugins/Legendchat/data/data.json");
	private final Gson gson = new Gson();

	public IgnoreManager() {
		if (!dataFile.getParentFile().exists()) {
			dataFile.getParentFile().mkdirs();
		}
		loadIgnoreData();
	}

	public void playerIgnorePlayer(Player who, Player ignored) {
		UUID uuid = who.getUniqueId();
		UUID ignoredUuid = ignored.getUniqueId();
		if (hasPlayerIgnoredPlayer(who, ignored)) {
			return;
		}
		List<UUID> ignorados = IgnoreList.getOrDefault(uuid, new ArrayList<>());
		ignorados.add(ignoredUuid);
		IgnoreList.put(uuid, ignorados);
		saveIgnoreData();
	}

	public void playerUnignorePlayer(Player who, Player ignored) {
		UUID uuid = who.getUniqueId();
		UUID ignoredUuid = ignored.getUniqueId();
		if (!hasPlayerIgnoredPlayer(who, ignored)) {
			return;
		}
		List<UUID> ignorados = IgnoreList.get(uuid);
		ignorados.remove(ignoredUuid);
		if (ignorados.isEmpty()) {
			IgnoreList.remove(uuid);
		} else {
			IgnoreList.put(uuid, ignorados);
		}
		saveIgnoreData();
	}

	public boolean hasPlayerIgnoredPlayer(Player who, Player ignored) {
		UUID uuid = who.getUniqueId();
		UUID ignoredUuid = ignored.getUniqueId();
		return IgnoreList.getOrDefault(uuid, Collections.emptyList()).contains(ignoredUuid);
	}

	public void playerIgnoreChannel(Player who, Channel ignored) {
		UUID uuid = who.getUniqueId();
		if (hasPlayerIgnoredChannel(who, ignored)) {
			return;
		}
		List<String> ignorados = MuteList.getOrDefault(uuid, new ArrayList<>());
		ignorados.add(ignored.getName().toLowerCase());
		MuteList.put(uuid, ignorados);
		saveIgnoreData();
	}

	public void playerUnignoreChannel(Player who, Channel c) {
		UUID uuid = who.getUniqueId();
		if (!hasPlayerIgnoredChannel(who, c)) {
			return;
		}
		List<String> ignorados = MuteList.get(uuid);
		ignorados.remove(c.getName().toLowerCase());
		if (ignorados.isEmpty()) {
			MuteList.remove(uuid);
		} else {
			MuteList.put(uuid, ignorados);
		}
		saveIgnoreData();
	}

	public boolean hasPlayerIgnoredChannel(Player who, Channel ignored) {
		UUID uuid = who.getUniqueId();
		return MuteList.getOrDefault(uuid, Collections.emptyList()).contains(ignored.getName().toLowerCase());
	}

	public void playerDisconnect(Player p) {
		saveIgnoreData();
	}

	public void playerLogin(Player p) {
		// No need to load data for individual players anymore
	}

	private void saveIgnoreData() {
		try {
			Map<String, Object> data = new HashMap<>();
			data.put("Players", IgnoreList);
			data.put("Channels", MuteList);
			String json = gson.toJson(data);
			Files.write(dataFile.toPath(), json.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadIgnoreData() {
		if (!dataFile.exists()) {
			return;
		}
		try {
			String json = new String(Files.readAllBytes(dataFile.toPath()));
			Type type = new TypeToken<Map<String, Object>>() {}.getType();
			Map<String, Object> data = gson.fromJson(json, type);
			Map<UUID, List<UUID>> loadedIgnoreList = (Map<UUID, List<UUID>>) data.get("Players");
			Map<UUID, List<String>> loadedIgnoreList2 = (Map<UUID, List<String>>) data.get("Channels");
			IgnoreList.putAll(loadedIgnoreList);
			MuteList.putAll(loadedIgnoreList2);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//TODO: Actually implement this.
	public void showIgnoreList(Player sender, Player target) {
		UUID targetUuid = target.getUniqueId();
		List<UUID> ignoredPlayers = IgnoreList.getOrDefault(targetUuid, Collections.emptyList());
		sender.sendMessage("Ignored players for " + target.getDisplayName() + ":");
		for (UUID ignoredUuid : ignoredPlayers) {
			Player ignoredPlayer = Bukkit.getPlayer(ignoredUuid);
			if (ignoredPlayer != null) {
				sender.sendMessage("- " + ignoredPlayer.getName() + " (" + ignoredPlayer.getDisplayName() + ")");
			} else {
				sender.sendMessage("- " + ignoredUuid.toString());
			}
		}
	}

	//TODO: Actually implement this.
	public void showMuteList(Player sender, Player target) {
		UUID targetUuid = target.getUniqueId();
		List<String> mutedChannels = MuteList.getOrDefault(targetUuid, Collections.emptyList());
		sender.sendMessage("Muted channels for " + target.getDisplayName() + ":");
		for (String channel : mutedChannels) {
			sender.sendMessage("- " + channel);
		}
	}
}