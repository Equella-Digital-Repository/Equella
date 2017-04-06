package com.tle.core.services.external.impl;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.tle.beans.filesystem.FileHandle;
import com.tle.common.PathUtils;
import com.tle.common.util.ExecUtils;
import com.tle.common.util.ExecUtils.ExecResult;
import com.tle.core.filesystem.ConversionFile;
import com.tle.core.guice.Bind;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.external.Office2HtmlConversionService;

/**
 * Provides file conversion services to the application. The conversion work is
 * performed by calls to conversion-service.jar via the command line.
 * 
 * @author Nicholas Read
 */
@Bind(Office2HtmlConversionService.class)
@Singleton
public class Office2HtmlConversionServiceImpl implements Office2HtmlConversionService
{
	private static final Logger LOGGER = Logger.getLogger(Office2HtmlConversionServiceImpl.class);

	private static final Collection<String> SUPPORTED_EXTENSIONS = Arrays.asList("doc", "xls", "ppt", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		"pps"); //$NON-NLS-1$

	@Inject
	private FileSystemService fileSystemService;

	@Inject(optional = true)
	@Named("conversionService.disableConversion")
	private boolean disableConversion;
	@Inject(optional = true)
	@Named("conversionService.conversionServicePath")
	private String conversionJar;
	private File conversionDir;

	@Inject
	@Named("java.home")
	private String javaHome;
	private String javaExe;

	public void setConversionServicePath(String conversionServicePath)
	{
		this.conversionJar = conversionServicePath;
	}

	public void setJavaPath(String javaPath)
	{
		this.javaHome = javaPath;
	}

	public void setDisableConversion(boolean disableConversion)
	{
		this.disableConversion = disableConversion;
	}

	@SuppressWarnings("nls")
	@PostConstruct
	public void setupBinaries()
	{
		if( disableConversion )
		{
			return;
		}

		// Test for the Java install
		File javaBinDir = new File(javaHome, "bin");
		if( !javaBinDir.exists() || !javaBinDir.isDirectory() )
		{
			throw new RuntimeException("Error setting conversionService.javaPath: Java directory not found at "
				+ javaBinDir);
		}

		File javaExeFile = ExecUtils.findExe(javaBinDir, "java");
		if( javaExeFile == null )
		{
			throw new RuntimeException("Error setting conversionService.javaPath: Java executable not found in "
				+ javaBinDir);
		}

		this.javaExe = javaExeFile.toString();

		// Test for the conversion service
		File cjFile = new File(conversionJar);
		if( !cjFile.exists() || cjFile.isDirectory() )
		{
			throw new RuntimeException(
				"Error setting conversionService.conversionServicePath: Conversion service JAR does not exist at "
					+ cjFile);
		}
		this.conversionDir = cjFile.getParentFile();
	}

	public ExecResult runService(String... args)
	{
		final String libraryPath = conversionDir.getAbsolutePath();
		final String[] cmd = new String[5 + args.length];
		cmd[0] = javaExe;
		cmd[1] = "-Demma.rt.control=false";
		cmd[2] = "-Djava.library.path=\"" + libraryPath + "\"";
		cmd[3] = "-jar";
		cmd[4] = conversionJar;

		System.arraycopy(args, 0, cmd, 5, args.length);
		final Map<String, String> env = new HashMap<String, String>();
		env.put("LD_LIBRARY_PATH", libraryPath);
		env.put("PATH", "%PATH%;" + libraryPath);
		return ExecUtils.exec(cmd, env, conversionDir);
	}

	@Override
	public boolean isConvertibleToHtml(String file) throws Exception
	{
		if( disableConversion )
		{
			return false;
		}
		return SUPPORTED_EXTENSIONS.contains(PathUtils.fileParts(file.toLowerCase()).getSecond());
	}

	@Override
	public String convert(FileHandle itemHandle, String file, String extension) throws Exception
	{
		if( disableConversion )
		{
			throw new RuntimeApplicationException("Conversion service is disabled");
		}

		ConversionFile targetHandle = new ConversionFile(itemHandle);
		String targetFile = file + '.' + extension;
		File source = fileSystemService.getExternalFile(itemHandle, file);
		File target = fileSystemService.getExternalFile(targetHandle, targetFile);

		if( !target.exists() || source.lastModified() > target.lastModified() )
		{
			LOGGER.info("Converting " + file + " to '" + extension + '\'');
			target.getParentFile().mkdirs();
			if( target.exists() )
			{
				fileSystemService.removeFile(targetHandle, targetFile);
			}

			ExecResult exec = runService(source.getAbsolutePath(), target.getAbsolutePath());
			exec.ensureOk();
		}
		else if( LOGGER.isDebugEnabled() )
		{
			LOGGER.debug("Using cached conversion " + target.getAbsolutePath());
		}

		String relativePath = PathUtils.filePath(targetHandle.getMyPathComponent(), targetFile);
		if( LOGGER.isDebugEnabled() )
		{
			LOGGER.debug("Converted path is " + relativePath);
		}

		return relativePath;
	}
}
