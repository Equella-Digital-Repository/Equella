package com.tle.core.services;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.java.plugin.registry.Extension;

import com.tle.beans.Institution;
import com.tle.common.NameValue;
import com.tle.core.filesystem.ImportFile;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.progress.ListProgressCallback;

public interface InstitutionImportService
{
	public enum ConvertType
	{
		DELETE, EXPORT, IMPORT, CLONE
	}

	Map<String, String> validate(Institution inst);

	Institution update(Institution institution);

	void delete(Institution institution, ListProgressCallback callback);

	void clone(long targetSchemaId, Institution newInstitution, long cloneFrom, ListProgressCallback callback,
		Set<String> conversions);

	String exportInstitution(Institution i, ListProgressCallback callback, Set<String> conversions);

	InstitutionInfo getInstitutionInfo(ImportFile staging);

	Institution importInstitution(ImportFile staging, long targetSchemaId, InstitutionInfo imported,
		ListProgressCallback callback);

	/**
	 * Simply deletes the staging file
	 * 
	 * @param staging
	 */
	void cancelImport(ImportFile staging);

	Collection<NameValue> getMatchingConversions(Collection<String> name);

	Set<String> getMatchingIds(Collection<String> values);

	List<String> getConverterTasks(ConvertType type, InstitutionInfo info);

	InstitutionInfo getInfoForCurrentInstitution();

	Set<String> getAllConversions();

	Set<String> convertToFlags(Set<String> conversions);

	Set<Extension> orderExtsByDependencies(PluginTracker<?> tracker, Collection<Extension> extensions);

	InstitutionInfo getInstitutionInfo(Institution institution);

	String getVersionNumberForLegacyImport(String buildVersion);

	/*
	 * The following methods call straight through to InstitutionService. Could
	 * be removed.
	 */

	Institution getInstitution(long institutionId);

	void setEnabled(long instId, boolean enabled);
}
