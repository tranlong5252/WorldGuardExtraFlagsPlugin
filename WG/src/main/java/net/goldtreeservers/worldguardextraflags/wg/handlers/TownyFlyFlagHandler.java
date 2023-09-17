package net.goldtreeservers.worldguardextraflags.wg.handlers;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import lombok.Getter;
import lombok.SneakyThrows;
import net.goldtreeservers.worldguardextraflags.flags.Flags;
import org.bukkit.entity.Player;

public class TownyFlyFlagHandler extends FlagValueChangeHandler<StateFlag.State> {

	public static TownyFlyFlagHandler.Factory FACTORY() {
		return new TownyFlyFlagHandler.Factory();
	}

	public static class Factory extends Handler.Factory<TownyFlyFlagHandler> {
		@Override
		public TownyFlyFlagHandler create(Session session) {
			return new TownyFlyFlagHandler(session);
		}
	}

	@Getter private Boolean currentValue;
	private Boolean originalFly;

	protected TownyFlyFlagHandler(Session session) {
		super(session, Flags.TOWNY_FLY);
	}

	@Override
	protected void onInitialValue(LocalPlayer player, ApplicableRegionSet applicableRegionSet, StateFlag.State value) {
		this.handleValue(player, player.getWorld(), value);
	}

	@Override
	protected boolean onSetValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, StateFlag.State currentValue, StateFlag.State lastValue, MoveType moveType) {
		this.handleValue(player, (World) to.getExtent(), currentValue);
		return true;
	}

	@Override
	protected boolean onAbsentValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, StateFlag.State currentValue, MoveType moveType) {
		this.handleValue(player, (World) to.getExtent(), currentValue);
		return true;
	}

	@SneakyThrows
	private void handleValue(LocalPlayer player, World world, StateFlag.State state) {
		Player bukkitPlayer = ((BukkitPlayer) player).getPlayer();
		TownBlock townBlock = TownyAPI.getInstance().getTownBlock(bukkitPlayer.getLocation());
		if (townBlock == null) return;
		Town town = townBlock.getTown();
		if (!town.hasResident(bukkitPlayer)) return;
		if (!this.getSession().getManager().hasBypass(player, world) && state != null) {
			boolean value = state == StateFlag.State.ALLOW;
			if (bukkitPlayer.getAllowFlight() != value) {
				if (this.originalFly == null) {
					this.originalFly = bukkitPlayer.getAllowFlight();
				}

				bukkitPlayer.setAllowFlight(value);
			}

			this.currentValue = value;
		}
		else {
			if (this.originalFly != null) {
				bukkitPlayer.setAllowFlight(this.originalFly);

				this.originalFly = null;
			}

			this.currentValue = null;
		}
	}
}
