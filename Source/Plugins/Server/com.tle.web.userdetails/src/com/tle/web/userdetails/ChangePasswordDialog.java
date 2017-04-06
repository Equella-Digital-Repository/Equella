package com.tle.web.userdetails;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.edge.common.valuebean.ValidationError;
import com.dytech.edge.exceptions.InvalidDataException;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.user.TLEUser;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.TLEUserService;
import com.tle.core.user.CurrentUser;
import com.tle.core.user.ModifiableUserState;
import com.tle.core.user.UserState;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.AbstractOkayableDialog;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.function.ReloadFunction;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;

@SuppressWarnings("nls")
@Bind
@NonNullByDefault
public class ChangePasswordDialog extends AbstractOkayableDialog<ChangePasswordDialog.ChangePasswordDialogModel>
{
	@PlugKey("internal.change")
	private static Label LABEL_TITLE;
	@PlugKey("internal.passsuccess")
	private static Label PASSWORD_SUCCESS;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private TLEUserService tleUserService;

	@Component(stateful = false)
	private TextField oldPassword;
	@Component(stateful = false)
	private TextField newPassword;
	@Component(stateful = false)
	private TextField confirmPassword;
	@Inject
	private ReceiptService receiptService;
	private JSCallable reloadParent;

	@Override
	public ChangePasswordDialogModel instantiateDialogModel(SectionInfo info)
	{
		return new ChangePasswordDialogModel();
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_TITLE;
	}

	@Override
	protected String getContentBodyClass(RenderContext context)
	{
		return "cp";
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		setAjax(true);
		reloadParent = addParentCallable(new ReloadFunction(false));
	}

	@Override
	protected JSHandler createOkHandler(SectionTree tree)
	{
		return events.getNamedHandler("savePassword");
	}

	@Override
	public String getHeight()
	{
		return "auto";
	}

	@EventHandlerMethod
	public void savePassword(SectionInfo info)
	{
		ChangePasswordDialogModel model = getModel(info);
		Map<String, String> errorList = model.getErrorList();
		errorList.clear();

		TLEUser old = tleUserService.get(CurrentUser.getUserID());

		String newPasswordText = newPassword.getValue(info);
		if( !newPasswordText.equals(confirmPassword.getValue(info)) )
		{
			errorList.put("confirmpass", CurrentLocale.get("com.tle.web.userdetails.internal.nomatch")); //$NON-NLS-2$
		}

		boolean b = tleUserService.checkPasswordMatch(old, oldPassword.getValue(info));
		if( !b )
		{
			errorList.put("oldpass", CurrentLocale.get("com.tle.web.userdetails.common.invalidpassword")); //$NON-NLS-2$
		}

		boolean blankPass = checkBlank(info, oldPassword, "oldpass");
		blankPass |= checkBlank(info, newPassword, "password");
		blankPass |= checkBlank(info, confirmPassword, "confirmpass");

		if( !blankPass )
		{
			try
			{
				tleUserService.validatePassword(newPasswordText, true);
			}
			catch( InvalidDataException ex )
			{
				for( ValidationError error : ex.getErrors() )
				{
					errorList.put(error.getField(), error.getMessage());
				}
			}

			if( errorList.size() == 0 )
			{
				old.setPassword(newPasswordText);
				tleUserService.editSelf(old, true);

				// We probably need a nicer way of doing this
				UserState userState = CurrentUser.getUserState();
				if( userState instanceof ModifiableUserState )
				{
					ModifiableUserState s = (ModifiableUserState) userState;
					s.setLoggedInUser(old);
					CurrentUser.setUserState(s);
				}
				receiptService.setReceipt(PASSWORD_SUCCESS);
				closeDialog(info, reloadParent);
			}
		}
	}

	private boolean checkBlank(SectionInfo info, TextField field, String key)
	{
		if( Check.isEmpty(field.getValue(info)) )
		{
			getModel(info).getErrorList().put(key, CurrentLocale.get("com.tle.web.userdetails.internal.blank"));
			return true;
		}
		return false;
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		return viewFactory.createResult("edit/password-dialog.ftl", this);
	}

	public TextField getOldPassword()
	{
		return oldPassword;
	}

	public TextField getNewPassword()
	{
		return newPassword;
	}

	public TextField getConfirmPassword()
	{
		return confirmPassword;
	}

	public static final class ChangePasswordDialogModel extends DialogModel
	{
		private final Map<String, String> errorList = new HashMap<String, String>();
		private boolean successful = false;

		public Map<String, String> getErrorList()
		{
			return errorList;
		}

		public void setSuccessful(boolean successful)
		{
			this.successful = successful;
		}

		public boolean isSuccessful()
		{
			return successful;
		}
	}
}
