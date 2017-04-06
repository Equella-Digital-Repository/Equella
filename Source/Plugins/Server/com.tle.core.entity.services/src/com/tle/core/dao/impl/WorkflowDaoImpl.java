/*
 * Created on Oct 26, 2005
 */
package com.tle.core.dao.impl;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemTaskId;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.dao.WorkflowDao;
import com.tle.core.guice.Bind;
import com.tle.core.user.CurrentInstitution;

/**
 * @author Nicholas Read
 */
@Bind(WorkflowDao.class)
@Singleton
@SuppressWarnings("nls")
public class WorkflowDaoImpl extends AbstractEntityDaoImpl<Workflow> implements WorkflowDao
{

	public WorkflowDaoImpl()
	{
		super(Workflow.class);
	}

	@Override
	public WorkflowItem getTaskForItem(Item item, String taskId)
	{
		@SuppressWarnings("unchecked")
		List<WorkflowItem> task = getHibernateTemplate()
			.findByNamedParam(
				"select wn from WorkflowNode wn, Item i where wn.uuid = :uuid and i = :item and i.itemDefinition.workflow = wn.workflow",
				new String[]{"uuid", "item"}, new Object[]{taskId, item});
		if( task.isEmpty() )
		{
			return null;
		}
		return task.get(0);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void markForReset(final Set<WorkflowNode> delNodes)
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@SuppressWarnings({"unchecked"})
			@Override
			public Object doInHibernate(Session session)
			{
				Query resetQuery = session
					.createQuery("update ModerationStatus s set s.needsReset = true where s.id in "
						+ "(select s.id from ModerationStatus as s inner join s.statuses as ns where ns.node in (:steps))");
				resetQuery.setParameterList("steps", delNodes);
				resetQuery.executeUpdate();

				Query deleteUsers = session
					.createQuery("from WorkflowItemStatus s where s.modStatus.needsReset = true and exists elements(s.acceptedUsers)");
				List<WorkflowItemStatus> itemList = deleteUsers.list();
				for( WorkflowItemStatus task : itemList )
				{
					session.delete(task);
				}
				Query deleteAffectedNodesQuery = session.createQuery("delete from WorkflowNodeStatus n where n.id in "
					+ "(select n.id from WorkflowNodeStatus n where n.modStatus.needsReset = true)");
				deleteAffectedNodesQuery.executeUpdate();
				return null;
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public Collection<WorkflowItem> findTasksForUser(String userId)
	{
		return getHibernateTemplate().findByNamedParam(
			"from WorkflowItem i join i.users as u where u = :userID and i.workflow.institution = :inst",
			new String[]{"userID", "inst"}, new Object[]{userId, CurrentInstitution.get()});
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public Collection<WorkflowItem> findTasksForGroup(String groupId)
	{
		return getHibernateTemplate().findByNamedParam(
			"from WorkflowItem i join i.groups as g where g = :groupID and i.workflow.institution = :inst",
			new String[]{"groupID", "inst"}, new Object[]{groupId, CurrentInstitution.get()});
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public Collection<WorkflowMessage> findMessagesForUser(String userID)
	{
		return getHibernateTemplate().findByNamedParam(
			"from WorkflowMessage where user = :userID and node.node.workflow.institution = :inst",
			new String[]{"userID", "inst"}, new Object[]{userID, CurrentInstitution.get()});
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public Collection<WorkflowItemStatus> findWorkflowItemStatusesForUser(String userID)
	{
		Collection<WorkflowItemStatus> col = getHibernateTemplate().findByNamedParam(
			"from WorkflowItemStatus wis where wis.node.workflow.institution = :inst and wis.assignedTo = :userID",
			new String[]{"inst", "userID"}, new Object[]{CurrentInstitution.get(), userID});
		Collection<WorkflowItemStatus> col2 = getHibernateTemplate()
			.findByNamedParam(
				"from WorkflowItemStatus wis join wis.acceptedUsers u where wis.node.workflow.institution = :inst and (:userID in u)",
				new String[]{"inst", "userID"}, new Object[]{CurrentInstitution.get(), userID});
		col.addAll(col2);
		return col;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public Collection<ModerationStatus> findModerationStatusesForUser(String userID)
	{
		return getHibernateTemplate()
			.findByNamedParam(
				"from ModerationStatus ms join ms.statuses s where s.node.workflow.institution = :inst and ms.rejectedBy = :userID",
				new String[]{"inst", "userID"}, new Object[]{CurrentInstitution.get(), userID});
	}

	private String getMessageHQL()
	{
		return "from Item i join i.moderation as ms join ms.statuses as ws join ws.comments as m where i.uuid = :uuid and i.version = :version and i.institution = :inst";
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public int getCommentCount(ItemKey itemKey)
	{
		return ((Number) getHibernateTemplate().findByNamedParam("select count(*) " + getMessageHQL(),
			new String[]{"uuid", "version", "inst"},
			new Object[]{itemKey.getUuid(), itemKey.getVersion(), CurrentInstitution.get()}).get(0)).intValue();
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<WorkflowMessage> getMessages(ItemKey itemKey)
	{
		return getHibernateTemplate().findByNamedParam("select m " + getMessageHQL(),
			new String[]{"uuid", "version", "inst"},
			new Object[]{itemKey.getUuid(), itemKey.getVersion(), CurrentInstitution.get()});
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public WorkflowItemStatus getIncompleteStatus(ItemTaskId itemTaskId)
	{
		List<WorkflowNodeStatus> node = getHibernateTemplate()
			.findByNamedParam(
				"SELECT ws FROM Item i JOIN i.moderation AS ms JOIN ms.statuses AS ws "
					+ "WHERE ws.status = 'i' AND ws.node.uuid = :task AND i.uuid = :uuid AND i.version = :version AND i.institution = :inst",
				new String[]{"task", "uuid", "version", "inst"},
				new Object[]{itemTaskId.getTaskId(), itemTaskId.getUuid(), itemTaskId.getVersion(),
						CurrentInstitution.get()});
		return node.size() > 0 ? ((WorkflowItemStatus) node.get(0)) : null;
	}

	@SuppressWarnings({"unchecked"})
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<WorkflowItem> getIncompleteTasks(Item item)
	{
		return getHibernateTemplate()
			.findByNamedParam(
				"SELECT ws.node FROM Item i JOIN i.moderation AS ms JOIN ms.statuses AS ws "
					+ "WHERE ws.status = 'i' AND ws.class = 'task' AND i.uuid = :uuid AND i.version = :version AND i.institution = :inst",
				new String[]{"uuid", "version", "inst"},
				new Object[]{item.getUuid(), item.getVersion(), CurrentInstitution.get()});

	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public WorkflowItem getWorkflowTaskById(long taskId)
	{
		return (WorkflowItem) getHibernateTemplate().get(WorkflowItem.class, taskId);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public int getItemCountForWorkflow(String uuid)
	{
		return ((Number) getHibernateTemplate().findByNamedParam(
			"SELECT count(*) FROM Item i WHERE i.itemDefinition.workflow.uuid = :uuid " + "AND i.moderating = true "
				+ "AND i.institution = :inst ", new String[]{"uuid", "inst"},
			new Object[]{uuid, CurrentInstitution.get()}).get(0)).intValue();
	}
}
