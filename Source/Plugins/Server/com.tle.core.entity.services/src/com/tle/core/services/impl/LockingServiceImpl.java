package com.tle.core.services.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.common.LockedException;
import com.google.common.collect.Lists;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.EntityLock;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemLock;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.dao.EntityLockingDao;
import com.tle.core.dao.ItemLockingDao;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.events.UserEditEvent;
import com.tle.core.events.UserIdChangedEvent;
import com.tle.core.events.UserSessionLogoutEvent;
import com.tle.core.events.listeners.UserChangeListener;
import com.tle.core.events.listeners.UserSessionLogoutListener;
import com.tle.core.guice.Bind;
import com.tle.core.services.LockingService;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.user.CurrentUser;
import com.tle.core.user.UserState;

/**
 * @author Nicholas Read
 */
@Bind(LockingService.class)
@Singleton
@SuppressWarnings("nls")
public class LockingServiceImpl implements LockingService, UserChangeListener, UserSessionLogoutListener
{
	@Inject
	private EntityLockingDao entityLockDao;
	@Inject
	private ItemLockingDao itemLockingDao;

	@Override
	@Transactional
	public EntityLock lockEntity(BaseEntity entity)
	{
		EntityLock lock = entityLockDao.findByCriteria(Restrictions.eq("entity", entity));
		if( lock != null )
		{
			throw new LockedException("Entity is already locked", lock.getUserID(), lock.getUserSession(),
				entity.getId());
		}
		else
		{
			UserState userState = CurrentUser.getUserState();

			lock = new EntityLock();
			lock.setEntity(entity);
			lock.setUserID(userState.getUserBean().getUniqueID());
			lock.setUserSession(userState.getSessionID());
			lock.setInstitution(CurrentInstitution.get());
			entityLockDao.save(lock);
		}
		return lock;
	}

	@Override
	@Transactional
	public EntityLock lockEntity(BaseEntity entity, String sessionId)
	{
		EntityLock lock = entityLockDao.findByCriteria(Restrictions.eq("entity", entity));
		if( lock != null )
		{
			throw new LockedException("Entity is already locked", lock.getUserID(), lock.getUserSession(),
				entity.getId());
		}
		else
		{
			lock = new EntityLock();
			lock.setEntity(entity);
			lock.setUserID(CurrentUser.getUserID());
			lock.setUserSession(sessionId);
			lock.setInstitution(CurrentInstitution.get());
			entityLockDao.save(lock);
			return lock;
		}
	}

	@Override
	@Transactional
	public void unlockEntity(BaseEntity entity, boolean force)
	{
		EntityLock lock = entityLockDao.findByCriteria(Restrictions.eq("entity", entity));
		if( lock != null )
		{
			if( force || CurrentUser.getSessionID().equals(lock.getUserSession()) )
			{
				entityLockDao.delete(lock);
			}
			else
			{
				throw new LockedException("Entity is locked by another session", lock.getUserID(),
					lock.getUserSession(), entity.getId());
			}
		}
	}

	@Override
	@Transactional
	public EntityLock getLock(BaseEntity entity)
	{
		EntityLock lock = entityLockDao.findByCriteria(Restrictions.eq("entity", entity));
		if( lock != null )
		{
			if( !CurrentUser.getSessionID().equals(lock.getUserSession()) )
			{
				throw new LockedException(CurrentLocale.get(entity.getName()) + " is locked by another session",
					lock.getUserID(), lock.getUserSession(), entity.getId());
			}
		}
		return lock;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public EntityLock getLock(BaseEntity entity, String lockId)
	{
		EntityLock lock = entityLockDao.findByCriteria(Restrictions.eq("entity", entity));
		if( lock != null )
		{
			if( lockId == null )
			{
				throw new LockedException("Entity is locked by another user '" + lock.getUserID() + "'",
					lock.getUserID(), lock.getUserSession(), entity.getId());
			}
			if( !lockId.equals(lock.getUserSession()) )
			{
				throw new LockedException("Wrong lock id. Entity is locked by user.", lock.getUserID(),
					lock.getUserSession(), entity.getId());
			}
		}
		else if( lockId != null )
		{
			throw new LockedException("Entity is not locked", null, null, entity.getId());
		}
		return lock;
	}

	/**
	 * From REST calls, we may not care about who or how a lock was set- just
	 * return it if it exists
	 */
	@Override
	@Transactional
	public EntityLock getLockUnbound(BaseEntity entity)
	{
		EntityLock lock = entityLockDao.findByCriteria(Restrictions.eq("entity", entity));
		return lock;
	}

	@Override
	public boolean isEntityLocked(BaseEntity entity, String userId, String sessionId)
	{
		List<Criterion> crit = Lists.newArrayList();
		crit.add(Restrictions.eq("entity", entity));
		if( userId != null )
		{
			crit.add(Restrictions.eq("userID", userId));
		}
		if( sessionId != null )
		{
			crit.add(Restrictions.eq("userSession", sessionId));
		}
		return (entityLockDao.findByCriteria(crit.toArray(new Criterion[crit.size()])) != null);
	}

	@Override
	@Transactional
	public ItemLock lockItem(Item item)
	{
		ItemLock lock = itemLockingDao.findByCriteria(Restrictions.eq("item", item));
		if( lock != null )
		{
			throw new LockedException("Item is already locked", lock.getUserID(), lock.getUserSession(), item.getId());
		}

		UserState userState = CurrentUser.getUserState();

		lock = new ItemLock();
		lock.setItem(item);
		lock.setUserID(userState.getUserBean().getUniqueID());
		lock.setUserSession(userState.getSessionID());
		lock.setInstitution(CurrentInstitution.get());
		itemLockingDao.save(lock);
		return lock;
	}

	@Override
	@Transactional
	public void unlockItem(Item item, boolean force)
	{
		ItemLock lock = itemLockingDao.findByCriteria(Restrictions.eq("item", item));
		if( lock != null )
		{
			if( force || CurrentUser.getSessionID().equals(lock.getUserSession()) )
			{
				itemLockingDao.delete(lock);
			}
			else
			{
				throw new LockedException("Item is locked by another session", lock.getUserID(), lock.getUserSession(),
					item.getId());
			}
		}
	}

	@Override
	@Transactional
	public ItemLock getLock(Item item)
	{
		return itemLockingDao.findByCriteria(Restrictions.eq("item", item));
	}

	private void unlock(Criterion crit)
	{
		for( EntityLock lock : entityLockDao.findAllByCriteria(crit) )
		{
			entityLockDao.delete(lock);
		}

		for( ItemLock lock : itemLockingDao.findAllByCriteria(crit) )
		{
			itemLockingDao.delete(lock);
		}
	}

	@Override
	@Transactional
	public void userDeletedEvent(UserDeletedEvent event)
	{
		unlock(Restrictions.eq("userID", event.getUserID()));
	}

	@Override
	public void userEditedEvent(UserEditEvent event)
	{
		// Nothing to do here
	}

	@Override
	@Transactional
	public void userIdChangedEvent(UserIdChangedEvent event)
	{
		// We can't trust that any logged in sessions for the changing user ID
		// are valid, so just remove all the locks. Tough luck for them.
		unlock(Restrictions.eq("userID", event.getFromUserId()));
	}

	@Override
	@Transactional
	public void userSessionDestroyedEvent(UserSessionLogoutEvent event)
	{
		unlock(Restrictions.eq("userSession", event.getSessionId()));
	}
}
