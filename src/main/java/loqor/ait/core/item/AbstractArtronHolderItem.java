package loqor.ait.core.item;

import loqor.ait.api.tardis.ArtronHolderItem;
import net.minecraft.item.Item;

public abstract class AbstractArtronHolderItem extends Item implements ArtronHolderItem {
	public AbstractArtronHolderItem(Settings settings) {
		super(settings);
	}
}
