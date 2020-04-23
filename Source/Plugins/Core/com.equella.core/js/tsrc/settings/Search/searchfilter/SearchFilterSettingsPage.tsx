import * as React from "react";
import {
  templateDefaults,
  templateError,
  TemplateUpdateProps,
} from "../../../mainui/Template";
import { routes } from "../../../mainui/routes";
import {
  Button,
  Card,
  CardActions,
  ListItem,
  ListItemSecondaryAction,
  ListItemText,
  ListSubheader,
  IconButton,
  List,
  makeStyles,
} from "@material-ui/core";
import EditIcon from "@material-ui/icons/Edit";
import DeleteIcon from "@material-ui/icons/Delete";
import AddCircleIcon from "@material-ui/icons/AddCircle";
import { languageStrings } from "../../../util/langstrings";
import SettingsList from "../../../components/SettingsList";
import SettingsListControl from "../../../components/SettingsListControl";
import SettingsToggleSwitch from "../../../components/SettingsToggleSwitch";
import { useEffect, useState } from "react";
import {
  defaultSearchSettings,
  getSearchSettingsFromServer,
  saveSearchSettingsToServer,
  SearchSettings,
} from "../SearchSettingsModule";
import { Save } from "@material-ui/icons";
import MessageInfo from "../../../components/MessageInfo";
import {
  batchDelete,
  batchUpdateOrAdd,
  filterComparator,
  getMimeTypeFiltersFromServer,
  MimeTypeFilter,
} from "./SearchFilterSettingsModule";
import MimeTypeFilterEditingDialog from "./MimeTypeFilterEditingDialog";
import { commonString } from "../../../util/commonstrings";
import { generateFromError } from "../../../api/errors";
import { AxiosError } from "axios";
import {
  addElement,
  deleteElement,
  replaceElement,
} from "../../../util/ImmutableArrayUtil";
import MessageDialog from "../../../components/MessageDialog";

const useStyles = makeStyles({
  spacedCards: {
    margin: "16px",
    width: "75%",
    padding: "16px",
    float: "left",
  },
  cardAction: {
    display: "flex",
    justifyContent: "flex-end",
  },
  floatingButton: {
    position: "fixed",
    top: 0,
    right: 0,
    marginTop: "80px",
    marginRight: "16px",
    width: "calc(25% - 112px)",
  },
});

const SearchFilterPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const classes = useStyles();
  const searchFilterStrings =
    languageStrings.settings.searching.searchfiltersettings;

  // The general Search settings. Here only configure searchingDisableOwnerFilter and searchingDisableDateModifiedFilter.
  const [searchSettings, setSearchSettings] = useState<SearchSettings>(
    defaultSearchSettings
  );

  // Used to record the initial Search settings and compare if values are changed or not when saving.
  const [initialSearchSettings, setInitialSearchSettings] = useState<
    SearchSettings
  >(defaultSearchSettings);

  // mimeTypeFilters contains all filters displayed in the list, including those saved in the Server and visually added/deleted.
  const [mimeTypeFilters, setMimeTypeFilters] = useState<MimeTypeFilter[]>([]);

  // changedMimeTypeFilters contains filters to be added or edited on the Server.
  const [changedMimeTypeFilters, setChangedMimeTypeFilters] = useState<
    MimeTypeFilter[]
  >([]);

  // deletedMimeTypeFilters contains filters to be deleted from the Server.
  const [deletedMimeTypeFilters, setDeletedMimeTypeFilters] = useState<
    MimeTypeFilter[]
  >([]);

  // selectedMimeTypeFilter refers to the filter to be edited in the dialog.
  const [selectedMimeTypeFilter, setSelectedMimeTypeFilter] = useState<
    MimeTypeFilter | undefined
  >();

  const [showSnackBar, setShowSnackBar] = useState<boolean>(false);
  const [openMimeTypeFilterEditor, setOpenMimeTypeFilterEditor] = useState<
    boolean
  >(false);
  const [openMessageDialog, setOpenMessageDialog] = useState<boolean>(false);
  const [messageDialogMessages, setMessageDialogMessages] = useState<string[]>(
    []
  );

  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(searchFilterStrings.name)(tp),
      backRoute: routes.Settings.to,
    }));
    getSearchSettings();
    getMimeTypeFilters();
  }, []);

  const mimeTypeFilterChanged =
    deletedMimeTypeFilters.length || changedMimeTypeFilters.length;

  const generalSearchSettingChanged =
    initialSearchSettings.searchingDisableOwnerFilter !==
      searchSettings.searchingDisableOwnerFilter ||
    initialSearchSettings.searchingDisableDateModifiedFilter !==
      searchSettings.searchingDisableDateModifiedFilter;

  /**
   * Fetch the general Search Settings from the Server.
   * Set the initial values of the Owner filter and Date modified filter to state.
   */
  const getSearchSettings = () => {
    getSearchSettingsFromServer()
      .then((settings) => {
        setSearchSettings(settings);
        setInitialSearchSettings(settings);
      })
      .catch((error: AxiosError) => handleError(error));
  };

  /**
   * Fetch MIME type filters from the Server and set them to state.
   * Clear the two collections of changed and deleted MIME type filters.
   */
  const getMimeTypeFilters = () => {
    getMimeTypeFiltersFromServer()
      .then((filters: MimeTypeFilter[]) => {
        setMimeTypeFilters(filters);
        setDeletedMimeTypeFilters([]);
        setChangedMimeTypeFilters([]);
      })
      .catch((error: AxiosError) => handleError(error));
  };

  /**
   * Visually add or update a filter.
   * If 'selectedMimeTypeFilter' is undefined, then the action is adding a new filter, so add
   * 'filter' to the 'mimeTypeFilters' and 'changedMimeTypeFilters'; otherwise replace 'selectedMimeTypeFilter'
   * with 'filter'.
   */
  const addOrUpdateMimeTypeFilter = (filter: MimeTypeFilter) => {
    if (!selectedMimeTypeFilter) {
      setMimeTypeFilters(addElement(mimeTypeFilters, filter));
      setChangedMimeTypeFilters(addElement(changedMimeTypeFilters, filter));
    } else {
      setMimeTypeFilters(
        replaceElement(
          mimeTypeFilters,
          filterComparator(selectedMimeTypeFilter),
          filter
        )
      );
      setChangedMimeTypeFilters(
        replaceElement(
          changedMimeTypeFilters,
          filterComparator(selectedMimeTypeFilter),
          filter
        )
      );
    }
  };

  /**
   * Visually delete a filter.
   * Remove 'filter' from 'mimeTypeFilters' and 'changedMimeTypeFilters'.
   * If the filter has an ID then add it to 'deletedMimeTypeFilters'.
   */
  const deleteMimeTypeFilter = (filter: MimeTypeFilter) => {
    setMimeTypeFilters(
      deleteElement(mimeTypeFilters, filterComparator(filter), 1)
    );
    setChangedMimeTypeFilters(
      deleteElement(changedMimeTypeFilters, filterComparator(filter), 1)
    );

    // Only put filters that already have an id into deletedMimeTypeFilters
    if (filter.id) {
      setDeletedMimeTypeFilters(addElement(deletedMimeTypeFilters, filter));
    }
  };

  const openMimeTypeFilterDialog = (filter?: MimeTypeFilter) => {
    setOpenMimeTypeFilterEditor(true);
    setSelectedMimeTypeFilter(filter);
  };

  const closeMimeTypeFilterDialog = () => {
    setOpenMimeTypeFilterEditor(false);
  };

  /**
   * Save general Search setting only when the configuration of Owner filter or Date modified filter has been changed.
   * Save MIME type filters only when they have been updated, delete or just created.
   */
  const save = () => {
    const errorMessages: string[] = [];
    if (!mimeTypeFilterChanged && !generalSearchSettingChanged) {
      return;
    }
    (generalSearchSettingChanged
      ? saveSearchSettingsToServer(searchSettings).catch((error) =>
          handleError(error)
        )
      : Promise.resolve()
    )
      .then(
        (): Promise<any> =>
          changedMimeTypeFilters.length
            ? batchUpdateOrAdd(changedMimeTypeFilters)
                .then((messages) => errorMessages.push(...messages))
                .catch((error) => handleError(error))
            : Promise.resolve()
      )
      .then(
        (): Promise<any> =>
          // Filters stored in 'deletedMimeTypeFilters' always have an ID.
          deletedMimeTypeFilters.length
            ? batchDelete(deletedMimeTypeFilters.map((filter) => filter.id!))
                .then((messages) => errorMessages.push(...messages))
                .catch((error) => handleError(error))
            : Promise.resolve()
      )
      .then(() => {
        // If the 207 response includes any non-2xx responses then display a dialog to show error messages.
        if (errorMessages.length > 0) {
          setMessageDialogMessages(errorMessages);
          setOpenMessageDialog(true);
        } else {
          setShowSnackBar(true);
        }
      })
      .catch(() => {}) // Errors have been handled and subsequent promises have skipped.
      .finally(() => {
        getMimeTypeFilters();
        getSearchSettings();
      });
  };

  const handleError = (error: Error) => {
    updateTemplate(templateError(generateFromError(error)));
    // The reason for throwing an error again is to prevent subsequent REST calls.
    throw new Error(error.message);
  };

  return (
    <>
      {/* Owner filter and Date modified filter */}
      <Card className={classes.spacedCards}>
        <SettingsList subHeading={searchFilterStrings.visibilityconfigtitle}>
          <SettingsListControl
            divider
            primaryText={searchFilterStrings.disableownerfilter}
            control={
              <SettingsToggleSwitch
                value={searchSettings.searchingDisableOwnerFilter}
                setValue={(disabled) => {
                  setSearchSettings({
                    ...searchSettings,
                    searchingDisableOwnerFilter: disabled,
                  });
                }}
                id={"disable_owner_filter_toggle"}
              />
            }
          />
          <SettingsListControl
            primaryText={searchFilterStrings.disabledatemodifiedfilter}
            control={
              <SettingsToggleSwitch
                value={searchSettings.searchingDisableDateModifiedFilter}
                setValue={(disabled) => {
                  setSearchSettings({
                    ...searchSettings,
                    searchingDisableDateModifiedFilter: disabled,
                  });
                }}
                id={"disable_date_modified_filter_toggle"}
              />
            }
          />
        </SettingsList>
      </Card>

      {/* MIME type filters */}
      <Card className={classes.spacedCards}>
        <List
          subheader={
            <ListSubheader disableGutters>
              {searchFilterStrings.mimetypefiltertitle}
            </ListSubheader>
          }
        >
          {mimeTypeFilters.map((filter, index) => {
            return (
              <ListItem divider={true} key={index}>
                <ListItemText primary={filter.name} />
                <ListItemSecondaryAction>
                  <IconButton
                    onClick={() => {
                      openMimeTypeFilterDialog(filter);
                    }}
                    aria-label={`${searchFilterStrings.edit} ${filter.name}`}
                    color={"secondary"}
                  >
                    <EditIcon />
                  </IconButton>
                  |
                  <IconButton
                    onClick={() => deleteMimeTypeFilter(filter)}
                    aria-label={`${searchFilterStrings.delete} ${filter.name}`}
                    color="secondary"
                  >
                    <DeleteIcon />
                  </IconButton>
                </ListItemSecondaryAction>
              </ListItem>
            );
          })}
        </List>
        <CardActions className={classes.cardAction}>
          <IconButton
            onClick={() => openMimeTypeFilterDialog()}
            aria-label={searchFilterStrings.add}
            color={"primary"}
          >
            <AddCircleIcon fontSize={"large"} />
          </IconButton>
        </CardActions>
      </Card>

      {/* MIME type filter dialog */}
      <MimeTypeFilterEditingDialog
        open={openMimeTypeFilterEditor}
        onClose={closeMimeTypeFilterDialog}
        addOrUpdate={addOrUpdateMimeTypeFilter}
        mimeTypeFilter={selectedMimeTypeFilter}
        handleError={handleError}
      />

      <MessageDialog
        open={openMessageDialog}
        messages={messageDialogMessages}
        title={searchFilterStrings.messagedialogtitle}
        subtitle={searchFilterStrings.messagedialogsubtitle}
        close={() => setOpenMessageDialog(false)}
      />

      {/* SAVE button*/}
      <Button
        color={"primary"}
        className={classes.floatingButton}
        variant="contained"
        size="large"
        onClick={save}
        aria-label={searchFilterStrings.save}
      >
        <Save />
        {commonString.action.save}
      </Button>

      {/* Snackbar */}
      <MessageInfo
        title={searchFilterStrings.changesaved}
        open={showSnackBar}
        onClose={() => setShowSnackBar(false)}
        variant={"success"}
      />
    </>
  );
};

export default SearchFilterPage;
