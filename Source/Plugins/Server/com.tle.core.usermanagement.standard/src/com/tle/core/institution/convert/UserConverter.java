/*
 * Created on 4/05/2006
 */
package com.tle.core.institution.convert;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.beans.user.TLEUser;
import com.tle.common.Check;
import com.tle.core.dao.user.TLEUserDao;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;

@SuppressWarnings("nls")
@Bind
@Singleton
public class UserConverter extends AbstractConverter<Object>
{
	private static final String USERS_FILE = "users/users.xml";

	@Inject
	private TLEUserDao tleUserDao;

	@Override
	public void doDelete(Institution institution, ConverterParams params)
	{
		tleUserDao.deleteAll();
	}

	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams callback)
		throws IOException
	{
		List<TLEUser> allUsers = tleUserDao.listAllUsers();
		tleUserDao.clear();
		for( TLEUser user : allUsers )
		{
			user.setInstitution(null);
		}
		xmlHelper.writeXmlFile(staging, USERS_FILE, allUsers);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		if( !fileSystemService.fileExists(staging, USERS_FILE) )
		{
			return;
		}

		List<TLEUser> users = (List<TLEUser>) xmlHelper.readXmlFile(staging, USERS_FILE);
		for( TLEUser user : users )
		{
			user.setInstitution(institution);

			// Hack! Some records don't have a first or last name. I don't think
			// the data was being correctly validated when doing a bulk import.
			// In any case, the validation does happen correctly now, so let's
			// just put the username into any blank fields to avoid errors.
			if( Check.isEmpty(user.getFirstName()) )
			{
				user.setFirstName(user.getUsername());
			}
			if( Check.isEmpty(user.getLastName()) )
			{
				user.setLastName(user.getUsername());
			}

			tleUserDao.save(user);
			tleUserDao.flush();
			tleUserDao.clear();
		}
	}

	@Override
	public ConverterId getConverterId()
	{
		return ConverterId.USERS;
	}
}
