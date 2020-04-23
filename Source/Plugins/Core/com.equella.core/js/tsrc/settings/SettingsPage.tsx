import * as React from "react";
import axios from "axios";
import {
  Theme,
  ExpansionPanel,
  ExpansionPanelSummary,
  ExpansionPanelDetails,
  Typography,
  List,
  ListItem,
  ListItemText,
  CircularProgress,
} from "@material-ui/core";
import {
  templateDefaults,
  templateError,
  TemplateUpdateProps,
} from "../mainui/Template";
import { fetchSettings } from "./SettingsPageModule";
import { GeneralSetting } from "./SettingsPageEntry";
import { languageStrings } from "../util/langstrings";
import MUILink from "@material-ui/core/Link";
import ExpandMoreIcon from "@material-ui/icons/ExpandMore";
import { Link } from "react-router-dom";
import { makeStyles } from "@material-ui/styles";
import AdminDownloadDialog from "../settings/AdminDownloadDialog";
import { ReactElement, useEffect, useState } from "react";
import UISettingEditor from "./UISettingEditor";
import { generateFromError } from "../api/errors";
import { groupMap, SettingGroup } from "./SettingGroups";

const useStyles = makeStyles((theme: Theme) => {
  return {
    heading: {
      fontSize: theme.typography.pxToRem(15),
      flexBasis: "33.33%",
      flexShrink: 0,
    },
    secondaryHeading: {
      fontSize: theme.typography.pxToRem(15),
    },
    progress: {
      display: "flex",
      marginTop: theme.spacing(4),
      justifyContent: "center",
    },
  };
});

interface SettingsPageProps extends TemplateUpdateProps {
  refreshUser: () => void;
}

const SettingsPage = ({ refreshUser, updateTemplate }: SettingsPageProps) => {
  const classes = useStyles();

  const [adminDialogOpen, setAdminDialogOpen] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);
  const [settingGroups, setSettingGroups] = useState<SettingGroup[]>([]);

  useEffect(() => {
    updateTemplate(templateDefaults(languageStrings["com.equella.core"].title));
  }, []);

  useEffect(() => {
    // Use a flag to prevent setting state when component is being unmounted
    const cancelToken = axios.CancelToken.source();
    fetchSettings(cancelToken.token)
      .then((settings) => {
        setSettingGroups(groupMap(settings));
        setLoading(false);
      })
      .catch((error) => {
        if (axios.isCancel(error)) {
          return; // Request was cancelled
        }
        handleError(error);
        setLoading(false);
      });

    return () => {
      cancelToken.cancel();
    };
  }, []);

  const handleError = (error: Error) => {
    updateTemplate(templateError(generateFromError(error)));
  };

  /**
   * Create the UI content for setting category
   * @param category - One of the pre-defined categories
   * @param settings - settings of the category
   * @returns {ReactElement} Either a List or UISettingEditor, depending on the category
   */
  const expansionPanelContent = ({
    category,
    settings,
  }: SettingGroup): ReactElement => {
    if (category.name === languageStrings.settings.ui.name) {
      return (
        <UISettingEditor refreshUser={refreshUser} handleError={handleError} />
      );
    }
    return (
      <ExpansionPanelDetails>
        <List>
          {settings.map((setting) => (
            <ListItem key={setting.id}>
              <ListItemText
                primary={settingLink(setting)}
                secondary={setting.description}
              />
            </ListItem>
          ))}
        </List>
      </ExpansionPanelDetails>
    );
  };

  /**
   * Create a link for each setting
   * @param {GeneralSetting} setting - A oEQ general setting
   * @returns {ReactElement} A link to the setting's page
   */
  const settingLink = (setting: GeneralSetting): ReactElement => {
    let link = <div />;
    if (setting.links.route) {
      link = <Link to={setting.links.route}>{setting.name}</Link>;
    } else if (setting.links.href) {
      link = <MUILink href={setting.links.href}>{setting.name}</MUILink>;
    } else if (setting.id === languageStrings.adminconsoledownload.id) {
      link = (
        <MUILink
          href="javascript:void(0)"
          onClick={() => setAdminDialogOpen(true)}
        >
          {setting.name}
        </MUILink>
      );
    }

    return link;
  };

  return (
    <div id="settingsPage">
      <AdminDownloadDialog
        open={adminDialogOpen}
        onClose={() => setAdminDialogOpen(false)}
      />
      {
        // Display a circular Progress Bar or the Setting menu, depending on the state of 'loading'
        loading ? (
          <div className={classes.progress}>
            <CircularProgress variant="indeterminate" />
          </div>
        ) : (
          settingGroups.map((group) => {
            const { name, desc } = group.category;
            return (
              <ExpansionPanel key={name}>
                <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography className={classes.heading}>{name}</Typography>
                  <Typography className={classes.secondaryHeading}>
                    {desc}
                  </Typography>
                </ExpansionPanelSummary>
                {expansionPanelContent(group)}
              </ExpansionPanel>
            );
          })
        )
      }
    </div>
  );
};

export default SettingsPage;
