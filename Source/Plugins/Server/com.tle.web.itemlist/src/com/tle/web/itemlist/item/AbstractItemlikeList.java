package com.tle.web.itemlist.item;

import java.util.List;
import java.util.Set;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.core.services.item.FreetextResult;
import com.tle.web.itemlist.StandardListSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;

@NonNullByDefault
public abstract class AbstractItemlikeList<I extends IItem<?>, LE extends ItemlikeListEntry<I>, M extends StandardListSection.Model<LE>>
	extends
		StandardListSection<LE, M> implements ItemlikeList<I, LE>
{
	protected abstract Set<String> getExtensionTypes();

	protected abstract LE createItemListEntry(SectionInfo info, I item, @Nullable FreetextResult result);

	@Override
	protected void customiseListEntries(RenderContext context, List<LE> entries)
	{
		// Nada
	}

	@Override
	public LE addItem(SectionInfo info, I item, @Nullable FreetextResult result)
	{
		LE entry = createItemListEntry(info, item, result);
		entry.setAttribute(FreetextResult.class, result);
		addListItem(info, entry);
		return entry;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model<I, LE>();
	}

	public static class Model<I extends IItem<?>, LE extends ItemlikeListEntry<I>>
		extends
			StandardListSection.Model<LE>
	{
		// nothing
	}
}
