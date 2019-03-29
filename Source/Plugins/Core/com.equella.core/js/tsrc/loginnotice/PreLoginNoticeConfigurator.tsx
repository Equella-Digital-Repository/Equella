import * as React from "react";
import { ChangeEvent } from "react";
import {
  FormControl,
  FormControlLabel,
  Grid,
  Radio,
  RadioGroup,
  Typography
} from "@material-ui/core";
import {
  clearPreLoginNotice,
  emptyTinyMCEString,
  getPreLoginNotice,
  NotificationType,
  PreLoginNotice,
  ScheduleTypeSelection,
  strings,
  submitPreLoginNotice,
  uploadPreLoginNoticeImage
} from "./LoginNoticeModule";
import { AxiosError, AxiosResponse } from "axios";
import RichTextEditor from "../components/RichTextEditor";
import SettingsMenuContainer from "../components/SettingsMenuContainer";
import { DateTimePicker } from "material-ui-pickers";

interface PreLoginNoticeConfiguratorProps {
  handleError: (axiosError: AxiosError) => void;
  notify: (notificationType: NotificationType) => void;
  submit: boolean;
}

interface PreLoginNoticeConfiguratorState {
  current: PreLoginNotice;
  db: PreLoginNotice;
}

class PreLoginNoticeConfigurator extends React.Component<
  PreLoginNoticeConfiguratorProps,
  PreLoginNoticeConfiguratorState
> {
  constructor(props: PreLoginNoticeConfiguratorProps) {
    super(props);
    this.state = {
      current: {
        notice: "",
        scheduleSettings: ScheduleTypeSelection.ON,
        startDate: new Date(),
        endDate: new Date()
      },
      db: {
        notice: "",
        scheduleSettings: ScheduleTypeSelection.ON,
        startDate: new Date(),
        endDate: new Date()
      }
    };
  }

  componentWillReceiveProps(
    nextProps: Readonly<PreLoginNoticeConfiguratorProps>,
    nextContext: any
  ): void {
    if (nextProps.submit != this.props.submit) {
      this.handleSubmitPreNotice();
    }
  }

  handleSubmitPreNotice = () => {
    if (this.state.current.notice == emptyTinyMCEString) {
      clearPreLoginNotice()
        .then(() => {
          this.props.notify(NotificationType.Clear);
          this.setState({
            db: this.state.current
            // dbHtml: this.state.html
          });
        })
        .catch((error: AxiosError) => {
          this.props.handleError(error);
        });
    } else {
      submitPreLoginNotice(this.state.current)
        .then(() => {
          this.props.notify(NotificationType.Save);
          this.setDBToValues();
        })
        .catch((error: AxiosError) => {
          this.props.handleError(error);
        });
    }
  };

  setValuesToDB = () => {
    this.setState({
      current: this.state.db
    });
  };

  setDBToValues = () => {
    this.setState({
      db: this.state.current
    });
  };

  componentDidMount = () => {
    getPreLoginNotice()
      .then((response: AxiosResponse<PreLoginNotice>) => {
        if (response.data.notice != undefined) {
          this.setState({
            db: response.data
          });
          this.setValuesToDB();
        }
      })
      .catch((error: AxiosError) => {
        this.props.handleError(error);
      });
  };

  handleEditorChange = (html: string) => {
    this.setState({
      current: { ...this.state.current, notice: html }
    });
  };

  areButtonsEnabled = (): boolean => {
    //state matches database?
    return this.state.current == this.state.db;
  };

  ScheduleSettings = () => {
    return (
      <FormControl>
        <Typography color="textSecondary" variant="subtitle1">
          {strings.scheduling.title}
        </Typography>
        <RadioGroup
          row
          value={ScheduleTypeSelection[this.state.current.scheduleSettings]}
          onChange={this.handleScheduleTypeSelectionChange}
        >
          <FormControlLabel
            value={ScheduleTypeSelection[ScheduleTypeSelection.ON]}
            label={strings.scheduling.alwayson}
            control={<Radio id="onRadioButton" />}
          />
          <FormControlLabel
            value={ScheduleTypeSelection[ScheduleTypeSelection.SCHEDULED]}
            label={strings.scheduling.scheduled}
            control={<Radio id="scheduledRadioButton" />}
          />
          <FormControlLabel
            value={ScheduleTypeSelection[ScheduleTypeSelection.OFF]}
            label={strings.scheduling.disabled}
            control={<Radio id="offRadioButton" />}
          />
        </RadioGroup>

        <div
          hidden={
            this.state.current.scheduleSettings !=
            ScheduleTypeSelection.SCHEDULED
          }
        >
          <Typography color="textSecondary" variant="subtitle1">
            {strings.scheduling.start}
          </Typography>

          <DateTimePicker
            id="startDatePicker"
            okLabel={<span id="ok">OK</span>}
            minDate={new Date().toLocaleDateString()}
            onChange={this.handleStartDateChange}
            format={"dd/MM/yyyy hh:mm a"}
            value={this.state.current.startDate}
          />

          <Typography color="textSecondary" variant="subtitle1">
            {strings.scheduling.end}
          </Typography>

          <DateTimePicker
            id="endDatePicker"
            minDate={this.state.current.startDate}
            minDateMessage={strings.scheduling.endbeforestart}
            onChange={this.handleEndDateChange}
            format={"dd/MM/yyyy hh:mm a"}
            value={this.state.current.endDate}
          />
        </div>
      </FormControl>
    );
  };

  handleStartDateChange = (startDate: Date) => {
    this.setState({ current: { ...this.state.current, startDate } });
  };

  handleEndDateChange = (endDate: Date) => {
    this.setState({ current: { ...this.state.current, endDate } });
  };

  handleScheduleTypeSelectionChange = (event: ChangeEvent, value: string) => {
    this.setState({
      current: {
        ...this.state.current,
        scheduleSettings: ScheduleTypeSelection[value]
      }
    });
  };

  render() {
    const ScheduleSettings = this.ScheduleSettings;
    return (
      <SettingsMenuContainer>
        <Grid id="preLoginConfig" container spacing={8} direction="column">
          <Grid item>
            <RichTextEditor
              htmlInput={this.state.db.notice}
              onStateChange={this.handleEditorChange}
              imageUploadCallBack={uploadPreLoginNoticeImage}
            />
          </Grid>
          <Grid item>
            <ScheduleSettings />
          </Grid>
        </Grid>
      </SettingsMenuContainer>
    );
  }
}

export default PreLoginNoticeConfigurator;
