package com.tle.core.services.language.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.tle.beans.Institution;
import com.tle.beans.Language;
import com.tle.common.Check;
import com.tle.common.filters.AndFilter;
import com.tle.core.dao.LanguageDao;
import com.tle.core.filesystem.LanguageFile;
import com.tle.core.filesystem.LanguagesFile;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.plugins.PluginTracker.ParamFilter;
import com.tle.core.security.impl.RequiresPrivilege;
import com.tle.core.services.EventService;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.language.LanguagePackChangedEvent;
import com.tle.core.services.language.LanguagePackChangedListener;
import com.tle.core.services.language.LanguageService;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.util.archive.ArchiveType;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
@Bind(LanguageService.class)
@Singleton
public class LanguageServiceImpl implements LanguageService, LanguagePackChangedListener
{
	private static final Logger LOGGER = Logger.getLogger(LanguageServiceImpl.class);

	private static final String LOCALE_PROPERTIES_FILE = "locale.properties";
	private static final String LOCALE_RESOURCE_CENTRE = "resource-centre.xml";
	private static final String LOCALE_PROPERTIES_LANGUAGE = "language";
	private static final String LOCALE_PROPERTIES_COUNTRY = "country";
	private static final String LOCALE_PROPERTIES_VARIANT = "variant";
	private static final String LOCALE_PROPERTIES_RTL = "rtl";

	private static final String LOCALE_RTL_FILE = "rtl";

	private final Map<Locale, Boolean> localeToRTL = new HashMap<Locale, Boolean>();

	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private EventService eventService;
	@Inject
	private LanguageDao languageDao;

	private PluginTracker<?> bundleFileTracker;

	@Override
	public Map<Long, String> getNames(Collection<Long> bundleRefs)
	{
		return languageDao.getNames(bundleRefs);
	}

	@Override
	public boolean isRightToLeft(Locale locale)
	{
		if( CurrentInstitution.get() == null )
		{
			return false;
		}

		synchronized( localeToRTL )
		{
			Boolean rtl = localeToRTL.get(locale);
			if( rtl == null )
			{
				rtl = fileSystemService.fileExists(new LanguageFile(locale), LOCALE_RTL_FILE);
				localeToRTL.put(locale, rtl);
			}
			return rtl;
		}
	}

	@Override
	public ResourceBundle getResourceBundle(Locale locale, String bundleGroup)
	{
		Institution institution = CurrentInstitution.get();
		if( institution != null )
		{
			String language = locale.getLanguage();
			locale = new Locale(institution.getUniqueId() + "-" + language, locale.getCountry(), locale.getVariant());
		}
		return ResourceBundle.getBundle(bundleGroup, locale, resourceBundleControl);
	}

	@Override
	public List<Language> getLanguages()
	{
		return languageDao.enumerateAll();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	@RequiresPrivilege(priv = "EDIT_SYSTEM_SETTINGS")
	public void setLanguages(Collection<Language> languages)
	{
		for( Language lang : languageDao.enumerateAll() )
		{
			languageDao.delete(lang);
		}

		for( Language language : languages )
		{
			language.setInstitution(CurrentInstitution.get());
			languageDao.save(language);
		}
		eventService.publishApplicationEvent(new LanguagePackChangedEvent(null));

		refreshBundles();
	}

	@Override
	public List<Locale> listAvailableResourceBundles()
	{
		List<Locale> result = new ArrayList<Locale>();
		for( String name : fileSystemService.grepIncludingDirs(new LanguagesFile(), null, "*") )
		{
			try
			{
				result.add(LanguageFile.parseLocale(name));
			}
			catch( RuntimeException ex )
			{
				LOGGER.warn("Invalid Locale file " + name);
			}
		}
		return result;
	}

	@Override
	@RequiresPrivilege(priv = "EDIT_SYSTEM_SETTINGS")
	public void deleteLanguagePack(Locale locale)
	{
		deleteLanguagePack(locale, true);
	}

	@RequiresPrivilege(priv = "EDIT_SYSTEM_SETTINGS")
	private void deleteLanguagePack(Locale locale, boolean refreshBundles)
	{
		fileSystemService.removeFile(new LanguageFile(locale), null);
		if( refreshBundles )
		{
			eventService.publishApplicationEvent(new LanguagePackChangedEvent(locale));
			refreshBundles();
		}
	}

	@Override
	public void refreshBundles()
	{
		ResourceBundle.clearCache();

		synchronized( localeToRTL )
		{
			localeToRTL.clear();
		}
	}

	@Override
	@RequiresPrivilege(priv = "EDIT_SYSTEM_SETTINGS")
	public void importLanguagePack(final String stagingId, final String filename) throws IOException
	{
		StagingFile staging = new StagingFile(stagingId);
		fileSystemService.unzipFile(staging, filename, null);

		Locale locale = null;

		if( fileSystemService.fileExists(staging, LOCALE_PROPERTIES_FILE) )
		{
			try( InputStream in = fileSystemService.read(staging, LOCALE_PROPERTIES_FILE) )
			{
				Properties props = new Properties();
				props.load(in);

				locale = new Locale(getLocalePart(props, LOCALE_PROPERTIES_LANGUAGE, 2), getLocalePart(props,
					LOCALE_PROPERTIES_COUNTRY, 2), getLocalePart(props, LOCALE_PROPERTIES_VARIANT, 10));

				if( Boolean.parseBoolean(props.getProperty(LOCALE_PROPERTIES_RTL)) )
				{
					fileSystemService.write(staging, LOCALE_RTL_FILE, new StringReader("rtl"), false);
				}
			}
		}
		else
		{
			locale = Locale.ROOT;
		}

		// Delete the existing language pack for the locale
		deleteLanguagePack(locale, false);

		// Delete the uploaded ZIP file
		fileSystemService.removeFile(staging, filename);

		// Commit our new language pack
		fileSystemService.commitFiles(staging, new LanguageFile(locale));

		eventService.publishApplicationEvent(new LanguagePackChangedEvent(locale));
		refreshBundles();
	}

	/**
	 * Institution import only.
	 */
	@Override
	@RequiresPrivilege(priv = "EDIT_SYSTEM_SETTINGS")
	public LanguageFile importLanguagePack(TemporaryFileHandle staging, InputStream zipIn) throws IOException
	{
		SubTemporaryFile unzipped = new SubTemporaryFile(staging, "unzipped");
		fileSystemService.unzipFile(unzipped, zipIn, ArchiveType.ZIP);

		Locale locale = null;

		if( fileSystemService.fileExists(unzipped, LOCALE_PROPERTIES_FILE) )
		{
			try( InputStream in = fileSystemService.read(unzipped, LOCALE_PROPERTIES_FILE) )
			{
				Properties props = new Properties();
				props.load(in);

				locale = new Locale(getLocalePart(props, LOCALE_PROPERTIES_LANGUAGE, 2), getLocalePart(props,
					LOCALE_PROPERTIES_COUNTRY, 2), getLocalePart(props, LOCALE_PROPERTIES_VARIANT, 10));

				if( Boolean.parseBoolean(props.getProperty(LOCALE_PROPERTIES_RTL)) )
				{
					fileSystemService.write(unzipped, LOCALE_RTL_FILE, new StringReader("rtl"), false);
				}
			}
		}
		else
		{
			locale = Locale.ROOT;
		}

		// Delete the existing language pack for the locale
		deleteLanguagePack(locale);

		LanguageFile result = new LanguageFile(locale);
		// Commit our new language pack
		fileSystemService.commitFiles(staging, result);

		return result;
	}

	private String getLocalePart(final Properties properties, final String key, final int maxLen)
	{
		String result = properties.getProperty(key, "").trim();
		if( result.length() > maxLen )
		{
			throw new RuntimeApplicationException("Locale for " + key + " with value of '" + result
				+ "' should be be no more than " + maxLen + " characters in length");
		}
		return result;
	}

	@Override
	@RequiresPrivilege(priv = "EDIT_SYSTEM_SETTINGS")
	public void exportLanguagePack(Locale locale, OutputStream out) throws IOException
	{
		LanguageFile handle = new LanguageFile(locale);
		ZipOutputStream zout = new ZipOutputStream(out);

		if( !fileSystemService.fileExists(handle, LOCALE_PROPERTIES_FILE) )
		{
			Properties config = new Properties();
			config.setProperty(LOCALE_PROPERTIES_LANGUAGE, locale.getLanguage());
			config.setProperty(LOCALE_PROPERTIES_COUNTRY, locale.getCountry());
			config.setProperty(LOCALE_PROPERTIES_VARIANT, locale.getVariant());

			if( fileSystemService.fileExists(handle, LOCALE_RTL_FILE) )
			{
				config.setProperty(LOCALE_PROPERTIES_RTL, Boolean.TRUE.toString());
			}

			zout.putNextEntry(new ZipEntry(LOCALE_PROPERTIES_FILE));
			config.store(zout, null);
			zout.closeEntry();
		}

		List<String> exportPaths = fileSystemService.grep(handle, null, "*.properties");
		if( fileSystemService.fileExists(handle, LOCALE_RESOURCE_CENTRE) )
		{
			exportPaths.add(LOCALE_RESOURCE_CENTRE);
		}

		for( String bundleGroup : exportPaths )
		{
			zout.putNextEntry(new ZipEntry(bundleGroup));
			InputStream in = null;
			try
			{
				in = fileSystemService.read(handle, bundleGroup);
				ByteStreams.copy(in, zout);
			}
			finally
			{
				Closeables.close(in, true);
				zout.closeEntry();
			}
		}
		zout.close();
	}

	@Override
	public void languageChangedEvent(LanguagePackChangedEvent event)
	{
		refreshBundles();
	}

	private final Control resourceBundleControl = new Control()
	{
		@Override
		public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader,
			boolean reload) throws IllegalAccessException, InstantiationException, IOException
		{
			try
			{
				final PluginResourcesBundle bundle = new PluginResourcesBundle();

				final String filename = baseName + ".properties";
				if( locale == Locale.ROOT )
				{
					try( InputStream in = LanguageServiceImpl.class.getResourceAsStream("i18n-" + filename) )
					{
						if( in != null )
						{
							bundle.addProperties(in, null);
						}
					}
					// Plug-in strings override the i18n-* files
					loadPluginStrings(bundle, baseName, locale);
				}
				else if( CurrentInstitution.get() != null )
				{
					String language = locale.getLanguage();
					int sep = language.indexOf('-');
					if( sep < 0 )
					{
						// Institution-specific ROOT locale. Don't bother
						// loading
						// ROOT strings from plug-ins, as they're already loaded
						// by
						// the server-wide ROOT locale.
						locale = Locale.ROOT;
					}
					else
					{
						locale = new Locale(language.substring(sep + 1), locale.getCountry(), locale.getVariant());

						// Only load plug-ins if not institution-specific ROOT.
						// Load
						// before language pack too, as we want the pack strings
						// to
						// override ours.
						loadPluginStrings(bundle, baseName, locale);
					}

					LanguageFile handle = new LanguageFile(locale);
					if( fileSystemService.fileExists(handle, filename) )
					{
						try( InputStream in = fileSystemService.read(handle, filename) )
						{
							bundle.addProperties(in, null);
						}
					}

					// Check for an XML version of the same file
					String xmlFilename = baseName + ".xml";
					if( fileSystemService.fileExists(handle, xmlFilename) )
					{
						try( InputStream in = fileSystemService.read(handle, xmlFilename) )
						{
							bundle.addProperties(in, null, true);
						}
					}
				}

				return (bundle.isEmpty() && locale != Locale.ROOT) ? null : bundle;
			}
			catch( Exception e )
			{
				LOGGER.error("Error loading language bundles", e);
				throw Throwables.propagate(e);
			}
		}

		@SuppressWarnings("unchecked")
		private void loadPluginStrings(PluginResourcesBundle bundle, String baseName, Locale locale) throws IOException
		{
			for( Extension ext : bundleFileTracker.getExtensions(new AndFilter<Extension>(new LocaleParamFilter(
				"locale", locale.toString()), new ParamFilter("group", baseName))) )
			{
				String filename = ext.getParameter("file").valueAsString();
				try( InputStream propStream = bundleFileTracker.getResourceAsStream(ext, filename) )
				{
					if( propStream != null )
					{
						final String prepend = ext.getDeclaringPluginDescriptor().getId() + '.';
						bundle.addProperties(propStream, prepend, filename.endsWith(".xml"));
					}
				}
			}
		}

		@Override
		public List<Locale> getCandidateLocales(String basename, Locale locale)
		{
			List<Locale> rv = super.getCandidateLocales(basename, locale);
			if( CurrentInstitution.get() != null )
			{
				// Insert our institution-specific root locale.
				String lang = locale.getLanguage();
				rv.add(rv.size() - 1, new Locale(lang.substring(0, lang.indexOf('-'))));
			}
			return rv;
		}

		@Override
		public Locale getFallbackLocale(String baseName, Locale locale)
		{
			return null;
		}
	};

	public static class LocaleParamFilter extends ParamFilter
	{
		public LocaleParamFilter(String parameter, String value)
		{
			super(parameter, false, value);
		}

		@Override
		public boolean include(Extension ext)
		{
			Collection<Parameter> ps = ext.getParameters(parameter);
			if( ps.isEmpty() )
			{
				for( String value : values )
				{
					if( !Check.isEmpty(value) )
					{
						return false;
					}
				}
				return true;
			}
			return super.include(ext);
		}
	}

	// // SPRING //////////////////////////////////////////////////////////////

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		bundleFileTracker = new PluginTracker<Object>(pluginService, "com.tle.common.i18n", "bundle", null);
	}
}
