package fr.devsylone.fkpi.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import fr.devsylone.fkpi.api.event.RuleChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.cryptomorin.xseries.XMaterial;

import fr.devsylone.fkpi.util.BlockDescription;

public class AllowedBlocks implements RuleValue
{
	private final List<BlockDescription> allowed = new ArrayList<>();

	public List<BlockDescription> getValue()
	{
		return allowed;
	}

	public boolean isAllowed(BlockDescription block)
	{
		return getValue().contains(block);
	}

	public List<BlockDescription> reducedList()
	{
		List<BlockDescription> list = new ArrayList<>(getValue());
		if (list.containsAll(allSigns())) {
			list.removeIf(b -> b.getBlockName().contains("SIGN"));
			list.add(new BlockDescription("SIGN (tous types)"));
		}
		return list;
	}

	@Override
	public void fillWithDefaultValue()
	{
		allowed.add(new BlockDescription("TORCH"));
		allowed.add(new BlockDescription("WALL_TORCH"));
		if (XMaterial.isNewVersion()) {
			allowed.add(new BlockDescription(XMaterial.REDSTONE_TORCH.parseMaterial()));
			allowed.add(new BlockDescription(XMaterial.REDSTONE_WALL_TORCH.parseMaterial()));
		} else {
			allowed.add(new BlockDescription("REDSTONE_TORCH_ON"));
		}

		allowed.add(new BlockDescription("FIRE"));
		allowed.addAll(allSigns());
		allowed.add(new BlockDescription("TNT"));

	}

	@Override
	public JsonElement toJSON()
	{
		JsonArray jsonArray = new JsonArray();
		for (BlockDescription block : getValue()) {
			jsonArray.add(block.toString());
		}
		return jsonArray;
	}

	public static List<BlockDescription> allSigns()
	{
		List<BlockDescription> signs = new ArrayList<>();
		if (XMaterial.isNewVersion())
			signs.add(new BlockDescription(XMaterial.OAK_SIGN.parseMaterial()));
		else
			signs.add(new BlockDescription("SIGN_POST"));
		signs.add(new BlockDescription(XMaterial.OAK_WALL_SIGN.parseMaterial()));
		if (Material.getMaterial("ACACIA_SIGN") != null)
		{
			signs.add(new BlockDescription(XMaterial.ACACIA_SIGN.parseMaterial()));
			signs.add(new BlockDescription(XMaterial.ACACIA_WALL_SIGN.parseMaterial()));
			signs.add(new BlockDescription(XMaterial.BIRCH_SIGN.parseMaterial()));
			signs.add(new BlockDescription(XMaterial.BIRCH_WALL_SIGN.parseMaterial()));
			signs.add(new BlockDescription(XMaterial.DARK_OAK_SIGN.parseMaterial()));
			signs.add(new BlockDescription(XMaterial.DARK_OAK_WALL_SIGN.parseMaterial()));
			signs.add(new BlockDescription(XMaterial.JUNGLE_SIGN.parseMaterial()));
			signs.add(new BlockDescription(XMaterial.JUNGLE_WALL_SIGN.parseMaterial()));
			signs.add(new BlockDescription(XMaterial.SPRUCE_SIGN.parseMaterial()));
			signs.add(new BlockDescription(XMaterial.SPRUCE_WALL_SIGN.parseMaterial()));
		}
		return signs;
	}

	public String format()
	{
		StringBuilder formatted = new StringBuilder();
		for(BlockDescription b : reducedList())
			formatted.append("\n§a✔ ").append(b.toString());
		return formatted.toString();
	}

	public void load(ConfigurationSection config)
	{
		List<String> blocksString = config.getStringList("value");
		allowed.addAll(blocksString.stream().map(BlockDescription::new).collect(Collectors.toList()));
	}

	public void save(ConfigurationSection config)
	{
		List<String> blocksString = getValue().stream().map(BlockDescription::toString).collect(Collectors.toList());
		config.set("value", blocksString);
	}

	@Override
	public String toString()
	{
		return "Blocks[" + getValue().stream()
				.map(BlockDescription::toString)
				.collect(Collectors.joining(", ")) + "]";
	}

    public void add(BlockDescription blockDescription)
	{
		Bukkit.getPluginManager().callEvent(new RuleChangeEvent<>(Rule.ALLOWED_BLOCKS, this));
		allowed.add(blockDescription);
    }

	public void removeIf(Predicate<? super BlockDescription> filter)
	{
		Bukkit.getPluginManager().callEvent(new RuleChangeEvent<>(Rule.ALLOWED_BLOCKS, this));
		allowed.removeIf(filter);
	}
}