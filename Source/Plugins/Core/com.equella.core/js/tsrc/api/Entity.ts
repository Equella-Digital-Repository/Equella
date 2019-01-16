import { IDictionary } from "../util/dictionary";
import { User } from "./User";
import { TargetListEntry } from "./acleditor";

export interface Entity {
  uuid?: string;
  name: string;
  description?: string;

  modifiedDate?: string;
  createdDate?: string;

  owner?: User;

  security?: EntitySecurity;
  exportDetails?: EntityExport;
  validationErrors?: IDictionary<string>;
  readonly?: {
    granted: string[];
  };
}

export interface EntitySecurity {
  rules: TargetListEntry[];
}

export interface EntityExport {}
