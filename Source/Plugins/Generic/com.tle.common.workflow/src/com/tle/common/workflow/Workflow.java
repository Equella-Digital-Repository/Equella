package com.tle.common.workflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;

import com.tle.beans.entity.BaseEntity;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.common.workflow.node.WorkflowTreeNode;

@Entity
@AccessType("field")
public class Workflow extends BaseEntity
{
	private static final long serialVersionUID = 1L;

	private boolean movelive;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "workflow_id", nullable = false)
	private Set<WorkflowNode> nodes;
	@Transient
	private WorkflowNode rootCache;

	public Workflow()
	{
		super();
	}

	public boolean isMovelive()
	{
		return movelive;
	}

	public void setMovelive(boolean movelive)
	{
		this.movelive = movelive;
	}

	public synchronized Set<WorkflowNode> getNodes()
	{
		return nodes;
	}

	public synchronized void setNodes(Set<WorkflowNode> nodes)
	{
		this.nodes = nodes;
	}

	public synchronized WorkflowNode getRoot()
	{
		if( rootCache == null && nodes != null )
		{
			for( WorkflowNode node : nodes )
			{
				WorkflowNode parent = node.getParent();
				if( parent == null )
				{
					rootCache = node;
				}
				else
				{
					parent.setChild(node);
				}
			}
		}
		return rootCache;
	}

	public Map<String, WorkflowNode> getAllNodesAsMap()
	{
		Map<String, WorkflowNode> allNodes = new HashMap<String, WorkflowNode>();
		for( WorkflowNode node : nodes )
		{
			allNodes.put(node.getUuid(), node);
		}
		return allNodes;
	}

	public Map<String, WorkflowItem> getAllWorkflowItems()
	{
		Map<String, WorkflowItem> allNodes = new HashMap<String, WorkflowItem>();
		for( WorkflowNode node : nodes )
		{
			if( node.getType() == WorkflowNode.ITEM_TYPE )
			{
				allNodes.put(node.getUuid(), (WorkflowItem) node);
			}
		}
		return allNodes;
	}

	public static Map<String, WorkflowItem> getAllWorkflowItems(WorkflowNode node)
	{
		// Use a LinkedHashMap to maintain correct step ordering
		Map<String, WorkflowItem> results = new LinkedHashMap<String, WorkflowItem>();
		recurseWorkflowItems(results, node);
		return results;
	}

	/**
	 * @return true if the recursion should stop.
	 */
	private static boolean recurseWorkflowItems(Map<String, WorkflowItem> items, WorkflowNode node)
	{
		if( node instanceof WorkflowItem )
		{
			items.put(node.getUuid(), (WorkflowItem) node);
		}

		if( !node.isLeafNode() )
		{
			WorkflowTreeNode treenode = (WorkflowTreeNode) node;
			int num = treenode.numberOfChildren();
			for( int i = 0; i < num; i++ )
			{
				boolean stop = recurseWorkflowItems(items, treenode.getChild(i));
				if( stop )
				{
					return true;
				}
			}
		}

		return false;
	}

	public synchronized void setRoot(WorkflowTreeNode root)
	{
		nodes = new HashSet<WorkflowNode>();
		addToSet(root);
		this.rootCache = root;
	}

	private void addToSet(WorkflowNode node)
	{
		nodes.add(node);
		for( WorkflowNode child : node.getChildren() )
		{
			addToSet(child);
		}
	}

	@Override
	public String toString()
	{
		final StringBuilder string = new StringBuilder(super.toString());
		try
		{
			dfsWorkflowItems(getRoot(), 0, new NodeVisitorCallback()
			{
				@Override
				public void visitNode(WorkflowNode node, int depth)
				{
					string.append('\n');
					for( int i = 0; i < depth; i++ )
					{
						string.append(' ');
						string.append(' ');
					}
					string.append('-');
					string.append(node.getName());
				}
			});
		}
		catch( Exception e )
		{
			// Forget about it
		}
		return string.toString();
	}

	private interface NodeVisitorCallback
	{
		void visitNode(WorkflowNode node, int depth);
	}

	private static void dfsWorkflowItems(WorkflowNode node, int depth, NodeVisitorCallback cb)
	{
		cb.visitNode(node, depth);
		if( !node.isLeafNode() )
		{
			WorkflowTreeNode treenode = (WorkflowTreeNode) node;
			int num = treenode.numberOfChildren();
			for( int i = 0; i < num; i++ )
			{
				dfsWorkflowItems(treenode.getChild(i), depth + 1, cb);
			}
		}
	}
}
