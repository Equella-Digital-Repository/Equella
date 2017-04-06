package com.tle.admin.controls.standard.universal;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.dytech.gui.JShuffleBox;
import com.tle.admin.Driver;
import com.tle.admin.controls.universal.UniversalControlSettingPanel;
import com.tle.common.NameValue;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.mimetypes.RemoteMimetypeService;
import com.tle.common.wizard.controls.universal.UniversalSettings;
import com.tle.common.wizard.controls.universal.handlers.FileUploadSettings;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class FileUploadSettingsPanel extends UniversalControlSettingPanel
{
	private static final long serialVersionUID = 4826796254818947420L;

	private final JLabel packageHeading;
	private final JLabel thumbHeading;
	private final JLabel generalHeading;
	private final JLabel mimeHeading;
	private final JLabel fileSizeLabel;
	private final JCheckBox noUnzip;
	private final JCheckBox packageOnly;
	private final JLabel restrictionLabel;
	private final JCheckBox qtiPackage;
	private final JCheckBox scormPackage;
	private final JCheckBox suppressThumbnail;
	private final JCheckBox showSuppressOption;
	private final JCheckBox restrictMime;
	private final JCheckBox restrictFileSize;
	private final JShuffleBox<NameValue> mimeShuffle;
	private final JSpinner fileSizeEdit;
	private final SpinnerNumberModel fileSizeEditModel;

	// private final JCheckBox noScrapbook;

	public FileUploadSettingsPanel()
	{
		super();
		packageHeading = new JLabel(
			CurrentLocale.get("com.tle.admin.controls.standard.fileupload.settings.subtitle.packages"));
		thumbHeading = new JLabel(
			CurrentLocale.get("com.tle.admin.controls.standard.fileupload.settings.subtitle.thumbnails"));
		generalHeading = new JLabel(
			CurrentLocale.get("com.tle.admin.controls.standard.fileupload.settings.subtitle.general"));
		mimeHeading = new JLabel(CurrentLocale.get("com.tle.admin.controls.standard.fileupload.settings.subtitle.mime"));
		noUnzip = new JCheckBox(CurrentLocale.get("com.tle.admin.controls.standard.fileupload.settings.nounzip"));
		packageOnly = new JCheckBox(
			CurrentLocale.get("com.tle.admin.controls.standard.fileupload.settings.packageonly"));
		restrictionLabel = new JLabel(
			CurrentLocale.get("com.tle.admin.controls.standard.fileupload.settings.package.restriction"));
		qtiPackage = new JCheckBox(CurrentLocale.get("com.tle.admin.controls.standard.fileupload.settings.qti"));
		scormPackage = new JCheckBox(CurrentLocale.get("com.tle.admin.controls.standard.fileupload.settings.scorm"));
		suppressThumbnail = new JCheckBox(
			CurrentLocale.get("com.tle.admin.controls.standard.fileupload.settings.thumbnail"));
		showSuppressOption = new JCheckBox(
			CurrentLocale.get("com.tle.admin.controls.standard.fileupload.settings.forcethumboption"));
		restrictMime = new JCheckBox(
			CurrentLocale.get("com.tle.admin.controls.standard.fileupload.settings.restrictbymime"));
		restrictFileSize = new JCheckBox(
			CurrentLocale.get("com.tle.admin.controls.standard.fileupload.settings.restrictfilesize"));
		fileSizeLabel = new JLabel(CurrentLocale.get("com.tle.admin.controls.standard.fileupload.settings.filesize"));
		mimeShuffle = new JShuffleBox<NameValue>();
		fileSizeEditModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
		fileSizeEdit = new JSpinner(fileSizeEditModel);

		packageOnly.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				updateGui();
			}
		});

		restrictMime.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				updateGui();
			}
		});
		restrictFileSize.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				updateGui();
			}
		});
		add(generalHeading, "span 2");
		add(restrictFileSize, "span 2");
		add(fileSizeLabel, "gapleft 18");
		add(fileSizeEdit, "w 60!");
		add(noUnzip, "span 2, gapbottom 8");

		add(packageHeading, "span 2");
		add(packageOnly, "span 2");
		add(restrictionLabel, "gapleft 18, wrap");
		add(qtiPackage, "gapleft 18, wrap");
		add(scormPackage, "gapleft 18, wrap, gapbottom 8");

		add(thumbHeading, "span 2");
		add(suppressThumbnail, "span 2");
		add(showSuppressOption, "span 2, gapbottom 8");

		add(mimeHeading, "span 2");
		add(restrictMime, "span 2");
		add(mimeShuffle, "growx, span 5, gapbottom 8");
		mimeShuffle.addToLeft(getMimeTypeNames());
		updateGui();
	}

	@Override
	protected String getTitleKey()
	{
		return "com.tle.admin.controls.standard.fileupload.settings.title";
	}

	@Override
	public void load(UniversalSettings state)
	{
		FileUploadSettings settings = new FileUploadSettings(state);
		noUnzip.setSelected(settings.isNoUnzip());
		packageOnly.setSelected(settings.isPackagesOnly());
		qtiPackage.setSelected(settings.isQtiPackagesOnly());
		scormPackage.setSelected(settings.isScormPackagesOnly());
		suppressThumbnail.setSelected(settings.isSuppressThumbnails());
		showSuppressOption.setSelected(settings.isShowThumbOption());
		restrictMime.setSelected(settings.isRestrictByMime());
		restrictFileSize.setSelected(settings.isRestrictFileSize());
		mimeShuffle.removeAllFromLeft();
		mimeShuffle.removeAllFromRight();
		fileSizeEditModel.setValue(settings.getMaxFileSize());
		selectMimetypes(settings.getMimeTypes());
		updateGui();
	}

	@Override
	public void removeSavedState(UniversalSettings state)
	{
		// Nothing to do
	}

	@Override
	public void save(UniversalSettings state)
	{
		FileUploadSettings settings = new FileUploadSettings(state);
		settings.setNoUnzip(noUnzip.isSelected());
		settings.setPackagesOnly(packageOnly.isSelected());
		settings.setQtiPackagesOnly(qtiPackage.isSelected());
		settings.setScormPackagesOnly(scormPackage.isSelected());
		settings.setSuppressThumbnails(suppressThumbnail.isSelected());
		settings.setShowThumbOption(showSuppressOption.isSelected());
		settings.setRestrictByMime(restrictMime.isSelected());
		settings.setRestrictFileSize(restrictFileSize.isSelected());
		settings.setMaxFileSize(fileSizeEditModel.getNumber().intValue());
		List<String> selectedMimetypes = new ArrayList<String>();
		for( NameValue mime : mimeShuffle.getRight() )
		{
			selectedMimetypes.add(mime.getValue());
		}
		settings.setMimeTypes(selectedMimetypes);
	}

	private void updateGui()
	{
		boolean packagesOnly = packageOnly.isSelected();
		qtiPackage.setEnabled(packagesOnly);
		scormPackage.setEnabled(packagesOnly);
		restrictionLabel.setEnabled(packagesOnly);
		restrictMime.setEnabled(!packageOnly.isSelected());
		fileSizeEdit.setEnabled(restrictFileSize.isSelected());
		fileSizeLabel.setEnabled(restrictFileSize.isSelected());
		if( !packagesOnly )
		{
			qtiPackage.setSelected(false);
			scormPackage.setSelected(false);
		}

		if( !restrictMime.isEnabled() )
		{
			restrictMime.setSelected(false);
		}
		mimeShuffle.setEnabled(restrictMime.isSelected());
		if( !restrictFileSize.isEnabled() )
		{
			restrictFileSize.setSelected(false);
		}
	}

	private static List<NameValue> getMimeTypeNames()
	{
		List<NameValue> result;
		RemoteMimetypeService mimeService = Driver.instance().getClientService()
			.getService(RemoteMimetypeService.class);

		// extra shizzle might go here
		result = mimeService.listAll();
		return result;
	}

	protected void selectMimetypes(List<String> selectedMimetypes)
	{
		List<NameValue> mimeTypes = getMimeTypeNames();
		mimeShuffle.addToLeft(mimeTypes);
		for( NameValue mime : mimeTypes )
		{
			if( selectedMimetypes.contains(mime.getValue()) )
			{
				mimeShuffle.addToRight(mime);
			}
		}
	}
}
