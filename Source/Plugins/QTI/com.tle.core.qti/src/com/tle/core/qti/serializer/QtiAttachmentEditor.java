package com.tle.core.qti.serializer;

import com.tle.core.guice.Bind;
import com.tle.core.item.edit.attachment.AbstractCustomAttachmentEditor;
import com.tle.core.qti.QtiConstants;

@Bind
public class QtiAttachmentEditor extends AbstractCustomAttachmentEditor
{
	@Override
	public String getCustomType()
	{
		return QtiConstants.TEST_CUSTOM_ATTACHMENT_TYPE;
	}

	public void editXmlFullPath(String xmlFullPath)
	{
		editCustomData(QtiConstants.KEY_XML_PATH, xmlFullPath);
	}

	public void editTestUuid(String testUuid)
	{
		editCustomData(QtiConstants.KEY_TEST_UUID, testUuid);
	}

	public void editToolName(String toolName)
	{
		editCustomData(QtiConstants.KEY_TOOL_NAME, toolName);
	}

	public void editToolVersion(String toolVersion)
	{
		editCustomData(QtiConstants.KEY_TOOL_VERSION, toolVersion);
	}

	public void editMaxTime(long maxTime)
	{
		editCustomData(QtiConstants.KEY_MAX_TIME, maxTime);
	}

	public void editQuestionCount(int questionCount)
	{
		editCustomData(QtiConstants.KEY_QUESTION_COUNT, questionCount);
	}

	public void editSectionCount(int sectionCount)
	{
		editCustomData(QtiConstants.KEY_SECTION_COUNT, sectionCount);
	}

	public void editNavigationMode(String navigationMode)
	{
		editCustomData(QtiConstants.KEY_NAVIGATION_MODE, navigationMode);
	}

	public void editManifestPath(String manifestPath)
	{
		if( hasBeenEdited(customAttachment.getUrl(), manifestPath) )
		{
			customAttachment.setUrl(manifestPath);
		}
	}
}
