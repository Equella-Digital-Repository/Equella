package com.tle.core.connectors.service;

import java.util.List;

import com.tle.beans.item.IItem;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.connectors.ConnectorContent;
import com.tle.common.connectors.ConnectorCourse;
import com.tle.common.connectors.ConnectorFolder;
import com.tle.common.connectors.ConnectorTerminology;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.searching.SearchResults;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.web.sections.SectionInfo;
import com.tle.web.selection.SelectedResource;

public interface ConnectorRepositoryService
{
	public enum ExternalContentSortType
	{
		NAME, COURSE, DATE_ADDED;

		@Override
		public String toString()
		{
			return super.toString().toLowerCase();
		}
	}

	String mungeUsername(String username, Connector connector);

	List<ConnectorCourse> getAllCourses(Connector connector, String username, boolean archived)
		throws LmsUserNotFoundException;

	List<ConnectorCourse> getModifiableCourses(Connector connector, String username, boolean archived)
		throws LmsUserNotFoundException;

	ConnectorFolder addItemToCourse(Connector connector, String username, String courseId, String sectionId,
		IItem<?> item, IAttachment attachment, SectionInfo info) throws LmsUserNotFoundException;

	ConnectorFolder addItemToCourse(Connector connector, String username, String courseId, String sectionId,
		IItem<?> item, SelectedResource resource) throws LmsUserNotFoundException;

	List<String> getImplementationTypes();

	List<ConnectorFolder> getFoldersForCourse(Connector connector, String username, String courseId);

	List<ConnectorFolder> getFoldersForFolder(Connector connector, String username, String courseId, String folderId);

	List<ConnectorContent> findUsages(Connector connector, String username, String itemUuid, int itemVersion,
		boolean archived, boolean allVersions) throws LmsUserNotFoundException;

	List<ConnectorContent> findUsages(Connector connector, String username, IItem<?> item, boolean archived,
		boolean allVersions) throws LmsUserNotFoundException;

	/**
	 * @param connector
	 * @param username
	 * @param query May be blank
	 * @param courseId May be blank
	 * @param folderId May be blank
	 * @param archived
	 * @param offset
	 * @param count if one of the connectors isn't working.
	 * @return
	 * @throws LmsUserNotFoundException
	 */
	SearchResults<ConnectorContent> findAllUsages(Connector connector, String username, String query, String courseId,
		String folderId, boolean archived, int offset, int count, ExternalContentSortType sortType,
		boolean sortAscending) throws LmsUserNotFoundException;

	/**
	 * Purely for Birt
	 * 
	 * @param username
	 * @param query
	 * @param courseId
	 * @param folderId
	 * @param archived
	 * @param offset
	 * @param count
	 * @param sortType
	 * @param sortAscending
	 * @return
	 * @throws LmsUserNotFoundException
	 */
	SearchResults<ConnectorContent> findAllUsagesAllConnectors(String username, String query, String courseId,
		String folderId, boolean archived, int offset, int count, ExternalContentSortType sortType,
		boolean sortAscending) throws LmsUserNotFoundException;

	/**
	 * @param connector
	 * @param username
	 * @param archived
	 * @return
	 * @throws LmsUserNotFoundException
	 */
	int getUnfilteredAllUsagesCount(Connector connector, String username, String query, boolean archived)
		throws LmsUserNotFoundException;

	ConnectorTerminology getConnectorTerminology(String lmsType);

	/**
	 * Controls move and export access
	 * 
	 * @param connector
	 * @return
	 */
	boolean supportsExport(String lmsType);

	boolean moveContent(Connector connector, String username, String contentId, String courseId, String locationId)
		throws LmsUserNotFoundException;

	/**
	 * Controls edit access
	 * 
	 * @param connector
	 * @return
	 */
	boolean supportsEdit(String lmsType);

	boolean editContent(Connector connector, String username, String contentId, String title, String description)
		throws LmsUserNotFoundException;

	/**
	 * Controls ?? access
	 * 
	 * @param connector
	 * @return
	 */
	boolean supportsView(String lmsType);

	/**
	 * Controls delete access
	 * 
	 * @param connector
	 * @return
	 */
	boolean supportsDelete(String lmsType);

	boolean deleteContent(Connector connector, String username, String id) throws LmsUserNotFoundException;

	boolean supportsCourses(String lmsType);

	boolean supportsFindUses(String lmsType);
}
