package com.tle.web.viewitem.sharing;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.mail.internet.AddressException;

import com.dytech.edge.common.valuebean.UserBean;
import com.google.common.base.Strings;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.email.EmailResult;
import com.tle.core.email.EmailService;
import com.tle.core.security.SharePassService;
import com.tle.core.services.UrlService;
import com.tle.core.services.item.ItemService;
import com.tle.core.services.user.UserService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ViewableChildInterface;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.model.SelectionsTableState;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.viewitem.summary.content.AbstractContentSection;

@SuppressWarnings("nls")
public abstract class AbstractShareWithOthersSection
	extends
		AbstractContentSection<AbstractShareWithOthersSection.ShareModel> implements ViewableChildInterface
{
	private static final String MESSAGE_FIELD = "message";
	private static final String EMAIL_FIELD = "email";

	@PlugKey("summary.content.sharewithothers.share.email.")
	private static String PREFIX;
	@PlugKey("unknown.user")
	protected static String UNKNOWN_USER;

	@PlugKey("summary.content.sharewithothers.pagetitle")
	protected static Label TITLE_LABEL;
	@PlugKey("summary.content.sharewithothers.notify.ok")
	protected static Label OK_LABEL;
	@PlugKey("summary.content.sharewithothers.share.success")
	protected static Label SUCCESS_LABEL;
	@PlugKey("summary.content.sharewithothers.share.fail")
	protected static Label FAIL_LABEL;
	@PlugKey("summary.content.sharewithothers.share.email.error.email")
	protected static String EMAIL_BLANK;
	@PlugKey("summary.content.sharewithothers.share.email.error.emailinvalid")
	protected static String EMAIL_INVALID;
	@PlugKey("summary.content.sharewithothers.share.email.error.message")
	protected static String MESSAGE_BLANK;
	@PlugKey("summary.content.sharewithothers.share.email.subject")
	protected static String EMAIL_SUBJECT;

	@Inject
	protected UrlService urlService;
	@Inject
	protected SharePassService sharePassService;
	@Inject
	protected EmailService emailService;
	@Inject
	protected ItemService itemService;
	@Inject
	protected UserService userService;
	@Inject
	protected ReceiptService receiptService;

	@Component
	protected TextField messageField;
	@Component
	protected TextField emailField;

	@Component
	@PlugKey("summary.content.sharewithothers.share.send")
	protected Button sendEmailButton;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		sendEmailButton.setClickHandler(events.getSubmitValuesHandler("sendEmail"));
	}

	@EventHandlerMethod
	public void sendEmail(SectionInfo info)
	{
		if( !canView(info) )
		{
			throw new AccessDeniedException(
				"You do not have the required privilege [SHARE_ITEM] or this service is not enabled");
		}

		final ShareModel model = getModel(info);

		model.getErrors().clear();
		boolean error = false;

		final String message = messageField.getValue(info);
		if( Check.isEmpty(message) )
		{
			model.addError(MESSAGE_FIELD, CurrentLocale.get(MESSAGE_BLANK));
			error = true;
		}

		final String email = emailField.getValue(info);
		if( Check.isEmpty(email) )
		{
			model.addError(EMAIL_FIELD, CurrentLocale.get(EMAIL_BLANK));
			error = true;
		}

		if( !error )
		{
			try
			{
				List<String> addresses = emailService.parseAddresses(email);

				Future<EmailResult<String>> result = emailService.sendEmail(CurrentLocale.get(EMAIL_SUBJECT),
					addresses, createEmail(info));

				EmailResult<String> emailResult;
					emailResult = result.get();
				boolean successful = emailResult.isSuccessful();

				receiptService.setReceipt(successful ? SUCCESS_LABEL : FAIL_LABEL);

				clearForm(info);
			}
			catch( AddressException ae )
			{
				model.addError(EMAIL_FIELD, CurrentLocale.get(EMAIL_INVALID));
			}
			catch( InterruptedException | ExecutionException e )
			{
				receiptService.setReceipt(FAIL_LABEL);
				clearForm(info);
			}
		}
	}

	protected abstract String createEmail(SectionInfo info);

	public void clearForm(SectionInfo info)
	{
		emailField.setValue(info, null);
		messageField.setValue(info, null);
	}

	protected String s(String key, Object... args)
	{
		return CurrentLocale.get(PREFIX + key, args);
	}

	protected String getUser(UserBean ub)
	{
		if(ub == null)
		{
			return CurrentLocale.get(UNKNOWN_USER);
		}
		
		return MessageFormat.format("{0} {1} ({2})", Strings.nullToEmpty(ub.getFirstName()),
			Strings.nullToEmpty(ub.getLastName()), Strings.nullToEmpty(ub.getEmailAddress()).toString());
	}

	@Override
	public Class<ShareModel> getModelClass()
	{
		return ShareModel.class;
	}

	public static class ShareModel
	{
		private SelectionsTableState usersSelected;
		private final Map<String, String> errors = new HashMap<String, String>();
		private boolean sharePassOn;
		private boolean showNotifyWhenLive;

		public Map<String, String> getErrors()
		{
			return errors;
		}

		public void addError(String key, String error)
		{
			this.errors.put(key, error);
		}

		public SelectionsTableState getUsersSelected()
		{
			return usersSelected;
		}

		public void setUsersSelected(SelectionsTableState usersSelected)
		{
			this.usersSelected = usersSelected;
		}

		public boolean isSharePassOn()
		{
			return sharePassOn;
		}

		public void setSharePassOn(boolean sharePassOn)
		{
			this.sharePassOn = sharePassOn;
		}

		public boolean isShowNotifyWhenLive()
		{
			return showNotifyWhenLive;
		}

		public void setShowNotifyWhenLive(boolean showNotifyWhenLive)
		{
			this.showNotifyWhenLive = showNotifyWhenLive;
		}
	}

	public TextField getMessageField()
	{
		return messageField;
	}

	public TextField getEmailField()
	{
		return emailField;
	}

	public Button getSendEmailButton()
	{
		return sendEmailButton;
	}

	public Label getTitle()
	{
		return TITLE_LABEL;
	}

}
