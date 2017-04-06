package com.tle.web.api.taxonomy;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.taxonomy.TaxonomyBean;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.core.guice.BindFactory;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.taxonomy.TaxonomyService;
import com.tle.web.api.baseentity.serializer.AbstractBaseEntityEditor;

/**
 * @author Aaron
 */
@NonNullByDefault
public class TaxonomyEditorImpl extends AbstractBaseEntityEditor<Taxonomy, TaxonomyBean> implements TaxonomyEditor
{
	@Inject
	private TaxonomyService taxonomyService;

	@AssistedInject
	public TaxonomyEditorImpl(@Assisted Taxonomy entity, @Assisted("stagingUuid") @Nullable String stagingUuid,
		@Assisted("lockId") @Nullable String lockId, @Assisted("editing") boolean editing,
		@Assisted("importing") boolean importing)
	{
		super(entity, stagingUuid, lockId, editing, importing);
	}

	@AssistedInject
	public TaxonomyEditorImpl(@Assisted Taxonomy entity, @Assisted("stagingUuid") @Nullable String stagingUuid,
		@Assisted("importing") boolean importing)
	{
		this(entity, stagingUuid, null, false, importing);
	}

	@Override
	protected AbstractEntityService<?, Taxonomy> getEntityService()
	{
		return taxonomyService;
	}

	@BindFactory
	public interface TaxonomyEditorFactory
	{
		TaxonomyEditorImpl createExistingEditor(Taxonomy taxonomy,
			@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("lockId") @Nullable String lockId,
			@Assisted("editing") boolean editing, @Assisted("importing") boolean importing);

		TaxonomyEditorImpl createNewEditor(Taxonomy taxonomy, @Assisted("stagingUuid") @Nullable String stagingUuid,
			@Assisted("importing") boolean importing);
	}
}
