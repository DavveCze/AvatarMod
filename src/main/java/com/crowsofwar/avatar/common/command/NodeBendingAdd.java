package com.crowsofwar.avatar.common.command;

import static com.crowsofwar.avatar.common.AvatarChatMessages.*;

import java.util.List;

import com.crowsofwar.avatar.common.bending.BendingController;
import com.crowsofwar.avatar.common.bending.BendingManager;
import com.crowsofwar.avatar.common.data.AvatarPlayerData;
import com.crowsofwar.gorecore.tree.ArgumentList;
import com.crowsofwar.gorecore.tree.ArgumentOptions;
import com.crowsofwar.gorecore.tree.ArgumentPlayerName;
import com.crowsofwar.gorecore.tree.CommandCall;
import com.crowsofwar.gorecore.tree.IArgument;
import com.crowsofwar.gorecore.tree.ICommandNode;
import com.crowsofwar.gorecore.tree.NodeFunctional;

import net.minecraft.command.ICommandSender;
import net.minecraft.world.World;

public class NodeBendingAdd extends NodeFunctional {
	
	private final IArgument<String> argPlayerName;
	private final IArgument<BendingController> argBendingController;
	
	public NodeBendingAdd() {
		super("add", true);
		
		this.argPlayerName = addArgument(new ArgumentPlayerName("player"));
		this.argBendingController = addArgument(
				new ArgumentOptions<BendingController>(AvatarCommand.CONVERTER_BENDING, "bending",
						BendingManager.allBending().toArray(new BendingController[0])));
		
	}
	
	@Override
	protected ICommandNode doFunction(CommandCall call, List<String> options) {
		
		ICommandSender sender = call.getFrom();
		World world = sender.getEntityWorld();
		
		ArgumentList args = call.popArguments(this);
		
		String playerName = args.get(argPlayerName);
		BendingController controller = args.get(argBendingController);
		
		AvatarPlayerData data = AvatarPlayerData.fetcher().fetch(world, playerName,
				"Error while getting player data for /avatar bending add");
		
		if (data == null) {
			
			MSG_PLAYER_DATA_NO_DATA.send(sender, playerName);
			
		} else {
			
			if (data.hasBending(controller.getID())) {
				
				MSG_BENDING_ADD_ALREADY_HAS.send(sender, playerName, controller.getControllerName());
				
			} else {
				
				data.addBending(controller);
				MSG_BENDING_ADD_SUCCESS.send(sender, playerName, controller.getControllerName());
				
			}
			
		}
		
		return null;
	}
	
}
