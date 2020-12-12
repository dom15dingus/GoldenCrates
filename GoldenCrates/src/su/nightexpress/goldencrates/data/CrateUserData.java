package su.nightexpress.goldencrates.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import com.google.common.reflect.TypeToken;

import su.nexmedia.engine.data.DataTypes;
import su.nexmedia.engine.data.IDataHandler;
import su.nightexpress.goldencrates.GoldenCrates;

public class CrateUserData extends IDataHandler<GoldenCrates, CrateUser> {

	private static CrateUserData instance;
	
	private final Function<ResultSet, CrateUser> FUNC_USER;
	
	@SuppressWarnings("serial")
	protected CrateUserData(@NotNull GoldenCrates plugin) throws SQLException {
		super(plugin);
		
		this.FUNC_USER = (rs) -> {
			try {
				UUID uuid = UUID.fromString(rs.getString(COL_USER_UUID));
				String name = rs.getString(COL_USER_NAME);
				long lastOnline = rs.getLong(COL_USER_LAST_ONLINE);
				
				Map<String, Integer> keys = this.gson.fromJson(rs.getString("keys"), new TypeToken<Map<String, Integer>>(){}.getType());
				Map<String, Long> openCooldowns = gson.fromJson(rs.getString("cd"), new TypeToken<Map<String, Long>>(){}.getType());
				
				return new CrateUser(plugin, uuid, name, lastOnline, keys, openCooldowns);
			}
			catch (SQLException e) {
				return null;
			}
		};
	}

	@NotNull
	public static CrateUserData getInstance(@NotNull GoldenCrates plugin) throws SQLException {
		if (instance == null) {
			instance = new CrateUserData(plugin);
		}
		return instance;
	}

	@Override
	@NotNull
	protected LinkedHashMap<String, String> getColumnsToCreate() {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("keys", DataTypes.STRING.build(this.dataType));
		map.put("cd", DataTypes.STRING.build(this.dataType));
		return map;
	}

	@Override
	@NotNull
	protected LinkedHashMap<String, String> getColumnsToSave(@NotNull CrateUser user) {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("keys", this.gson.toJson(user.getKeysMap()));
		map.put("cd", this.gson.toJson(user.getCooldowns()));
		return map;
	}

	@Override
	@NotNull
	protected Function<ResultSet, CrateUser> getFunctionToUser() {
		return this.FUNC_USER;
	}
}
