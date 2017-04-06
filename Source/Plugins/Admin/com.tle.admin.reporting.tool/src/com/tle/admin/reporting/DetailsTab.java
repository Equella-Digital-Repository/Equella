/*
 * Created on May 10, 2005
 */
package com.tle.admin.reporting;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.baseentity.BaseEntityEditor.AbstractDetailsTab;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.i18n.I18nTextArea;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.beans.entity.report.Report;
import com.tle.common.adminconsole.FileUploader;
import com.tle.common.applet.client.DialogUtils;
import com.tle.common.applet.client.DialogUtils.DialogResult;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.recipientselector.SingleUserSelector;
import com.tle.common.reporting.RemoteReportingService;
import com.tle.common.reporting.ReportingException;
import com.tle.common.reporting.ReportingException.Type;
import com.tle.core.remoting.RemoteUserService;
import com.tle.i18n.BundleCache;

/**
 * @author Nicholas Read
 */
public class DetailsTab extends BaseEntityTab<Report> implements AbstractDetailsTab<Report>, ActionListener
{
	@SuppressWarnings("nls")
	private static final String KEYPFX = "com.tle.admin.reporting.tool.";
	private static final String TITLE = CurrentLocale.get(KEYPFX + "detailstab.title"); //$NON-NLS-1$

	private JTextField identifier;
	private I18nTextField name;
	private I18nTextArea description;
	private SingleUserSelector owner;
	private JButton upload;
	private JButton download;
	private JCheckBox hideReport;

	private JComboBox initialReport;
	private RemoteReportingService service;

	public DetailsTab()
	{
		super();
	}

	@Override
	public void init(Component parent)
	{
		setupGUI();
		service = clientService.getService(RemoteReportingService.class);
	}

	@Override
	public String getTitle()
	{
		return TITLE;
	}

	private void setupGUI()
	{
		JLabel identifierLabel = new JLabel(CurrentLocale.get(KEYPFX + "detailstab.identifier")); //$NON-NLS-1$
		JLabel nameLabel = new JLabel(CurrentLocale.get(KEYPFX + "detailstab.name")); //$NON-NLS-1$
		JLabel descriptionLabel = new JLabel(CurrentLocale.get(KEYPFX + "detailstab.desc")); //$NON-NLS-1$
		JLabel ownerLabel = new JLabel(CurrentLocale.get(KEYPFX + "detailstab.owner")); //$NON-NLS-1$
		JLabel uploadLabel = new JLabel(CurrentLocale.get(KEYPFX + "detailstab.design")); //$NON-NLS-1$
		JLabel hideReportLabel = new JLabel(CurrentLocale.get(KEYPFX + "detailstab.hidereport")); //$NON-NLS-1$

		descriptionLabel.setVerticalAlignment(SwingConstants.TOP);

		identifier = new JTextField();
		identifier.setEditable(false);
		name = new I18nTextField(BundleCache.getLanguages());
		description = new I18nTextArea(BundleCache.getLanguages());
		owner = new SingleUserSelector(clientService.getService(RemoteUserService.class));
		hideReport = new JCheckBox();

		final int height1 = owner.getPreferredSize().height;
		final int width1 = hideReportLabel.getPreferredSize().width;

		final int[] rows = {height1, height1, height1 * 3, height1, height1, height1, TableLayout.FILL,};
		final int[] cols = {width1, TableLayout.DOUBLE_FILL, TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols));

		add(identifierLabel, new Rectangle(0, 0, 1, 1));
		add(identifier, new Rectangle(1, 0, 1, 1));

		add(nameLabel, new Rectangle(0, 1, 1, 1));
		add(name, new Rectangle(1, 1, 1, 1));

		add(descriptionLabel, new Rectangle(0, 2, 1, 1));
		add(description, new Rectangle(1, 2, 1, 1));

		add(ownerLabel, new Rectangle(0, 3, 1, 1));
		add(owner, new Rectangle(1, 3, 1, 1));

		// Make sure things are readonly.
		if( state.isReadonly() )
		{
			name.setEnabled(false);
			description.setEnabled(false);
			owner.setEnabled(false);
		}

		initialReport = new JComboBox();
		initialReport.setEditable(false);
		initialReport.setPreferredSize(new Dimension(230, 23));

		upload = new JButton(CurrentLocale.get(KEYPFX + "detailstab.upload")); //$NON-NLS-1$
		upload.addActionListener(this);

		download = new JButton(CurrentLocale.get(KEYPFX + "detailstab.download")); //$NON-NLS-1$
		download.addActionListener(this);

		add(uploadLabel, new Rectangle(0, 4, 1, 1));

		// Stupid java layout!!!
		JPanel panelsomething = new JPanel();
		panelsomething.setLayout(new BorderLayout(0, 0));
		panelsomething.add(upload, BorderLayout.WEST);
		panelsomething.add(download, BorderLayout.EAST);
		JPanel panelsomething2 = new JPanel();
		panelsomething2.setLayout(new BorderLayout(5, 10));
		panelsomething2.add(initialReport, BorderLayout.CENTER);
		panelsomething2.add(panelsomething, BorderLayout.EAST);

		panelsomething2.add(initialReport, BorderLayout.CENTER);
		panelsomething2.add(panelsomething, BorderLayout.EAST);

		add(panelsomething2, new Rectangle(1, 4, 1, 1));

		add(hideReportLabel, new Rectangle(0, 5, 1, 1));
		add(hideReport, new Rectangle(1, 5, 1, 1));
	}

	@Override
	public void addNameListener(KeyListener listener)
	{
		name.addKeyListener(listener);
	}

	@Override
	public void load()
	{
		final Report report = state.getEntity();

		identifier.setText(report.getUuid());
		name.load(report.getName());
		description.load(report.getDescription());
		owner.setUserId(report.getOwner());
		initialReport.setModel(new ReportDesignModel(state.getEntityPack().getStagingID()));
		if( report.getFilename() != null )
		{
			initialReport.setSelectedItem(new ReportDesignValue(report.getFilename()));
		}
		hideReport.setSelected(report.isHideReport());
	}

	@Override
	public void save()
	{
		final Report report = state.getEntity();

		report.setName(name.save());
		report.setDescription(description.save());
		report.setOwner(owner.getUser().getUniqueID());
		report.setFilename(((ReportDesignValue) initialReport.getSelectedItem()).getFilename());
		report.setHideReport(hideReport.isSelected());
	}

	@SuppressWarnings("nls")
	@Override
	public void validation() throws EditorException
	{
		if( name.isCompletelyEmpty() )
		{
			throw new EditorException(CurrentLocale.get(KEYPFX + "detailstab.supplyname")); //$NON-NLS-1$
		}

		if( owner.getUser() == null )
		{
			throw new EditorException(CurrentLocale.get(KEYPFX + "detailstab.noowner")); //$NON-NLS-1$
		}

		if( initialReport.getSelectedIndex() == -1 )
		{
			throw new EditorException(CurrentLocale.get(KEYPFX + "detailstab.noreport"));
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == upload )
		{
			uploadFile();
		}
		else if( e.getSource() == download )
		{
			download();
		}
	}

	public static final class RPTFileFilter extends FileFilter
	{
		@Override
		public String getDescription()
		{
			return CurrentLocale.get(KEYPFX + "detailstab.reportdesign"); //$NON-NLS-1$
		}

		@SuppressWarnings("nls")
		@Override
		public boolean accept(File f)
		{
			if( f.isDirectory() )
			{
				return true;
			}
			else
			{
				String name = f.toString().toLowerCase();
				return name.endsWith(".rptdesign") || name.endsWith(".zip") || name.endsWith(".rptlibrary");
			}
		}
	}

	public static final class RPTArchiveFileFilter extends FileFilter
	{
		@Override
		public String getDescription()
		{
			return CurrentLocale.get(KEYPFX + "detailstab.reportdowntype"); //$NON-NLS-1$
		}

		@SuppressWarnings("nls")
		@Override
		public boolean accept(File f)
		{
			if( f.isDirectory() )
			{
				return true;
			}
			else
			{
				String name = f.toString().toLowerCase();
				return name.endsWith(".zip");
			}
		}
	}

	@SuppressWarnings({"nls", "deprecation"})
	public boolean uploadFile()
	{
		File file;
		FileFilter filter = new RPTFileFilter();
		final DialogResult result = DialogUtils.openDialogStrictFilter(panel,
			CurrentLocale.get(KEYPFX + "detailstab.selectreport"), //$NON-NLS-1$
			filter, null);
		if( result.isOkayed() )
		{
			file = result.getFile();
		}
		else
		{
			return false;
		}

		try
		{
			try
			{
				String staging = state.getEntityPack().getStagingID();
				BufferedInputStream in = null;
				adminService.removeFile(staging, "");
				in = new BufferedInputStream(new FileInputStream(file));
				String fileName = file.getName();
				FileUploader.upload(adminService, staging, fileName, in);
				service.processReportDesign(staging, fileName);
				initialReport.setModel(new ReportDesignModel(staging));
			}
			catch( ReportingException re )
			{
				if( re.getType() == Type.NODESIGNS )
				{
					Driver.displayInformation(description, CurrentLocale.get(KEYPFX + "uploaderr.noreports"));
					return false;
				}
				else
				{
					throw re;
				}
			}
		}
		catch( Exception e )
		{
			Driver.displayError(description, KEYPFX + "uploaderr.title", KEYPFX + "uploaderr.msg", e);
			return false;
		}
		return true;
	}

	public void download()
	{
		final File file;
		FileFilter filter = new RPTArchiveFileFilter();
		final DialogResult result = DialogUtils.saveDialogStrictFilter(panel,
			CurrentLocale.get(KEYPFX + "detailstab.selectfile"), //$NON-NLS-1$
			filter, null);
		if( result.isOkayed() )
		{
			File file2 = result.getFile();
			if( !file2.getName().contains(".") ) //$NON-NLS-1$
			{
				file2 = new File(file2.getParentFile(), file2.getName() + ".zip"); //$NON-NLS-1$
			}
			file = file2;
		}
		else
		{
			return;
		}

		GlassSwingWorker<Object> worker = new GlassSwingWorker<Object>()
		{
			@Override
			public Object construct() throws Exception
			{
				String filename = file.getName();
				String stagingId = service.prepareDownload(state.getEntity(), state.getEntityPack().getStagingID(),
					filename);
				byte[] bs = adminService.downloadFile(stagingId, filename);
				try( OutputStream stream = new BufferedOutputStream(new FileOutputStream(file)) )
				{
					stream.write(bs);
				}
				service.cleanDownload(stagingId);
				return null;
			}

			@Override
			public void finished()
			{
				Driver.displayInformation(parent, CurrentLocale.get(KEYPFX + "detailstab.complete")); //$NON-NLS-1$
			}

			@Override
			public void exception()
			{
				Driver.displayInformation(panel, CurrentLocale.get(KEYPFX + "detailstab.error")); //$NON-NLS-1$
				LOGGER.error("Error downloading file", getException()); //$NON-NLS-1$
			}
		};
		worker.setComponent(parent);
		worker.start();
	}

	public final class ReportDesignModel extends DefaultComboBoxModel
	{
		public ReportDesignModel(String stagingId)
		{
			List<String> reportFilenames = service.getReportDesignFiles(stagingId);
			for( String filename : reportFilenames )
			{
				addElement(new ReportDesignValue(filename));
			}
		}
	}

	public static final class ReportDesignValue
	{
		private final String filename;
		private final String nameOnly;

		public ReportDesignValue(String filename)
		{
			nameOnly = new File(filename).getName();
			this.filename = filename;
		}

		public String getFilename()
		{
			return filename;
		}

		@Override
		public boolean equals(Object obj)
		{
			if( this == obj )
			{
				return true;
			}

			if( !(obj instanceof ReportDesignValue) )
			{
				return false;
			}

			return ((ReportDesignValue) obj).filename.equals(filename);
		}

		@Override
		public int hashCode()
		{
			return filename.hashCode();
		}

		@Override
		public String toString()
		{
			return nameOnly;
		}
	}
}
