
export interface Version {
  version: string;
  buildNumber: string;
}

export interface SystemInfo {
  name: string;
  version: Version;
  toggles: Toggles;
}

export interface Toggles {
  sortOrder: SortOrder;
}

export class DefaultToggles implements Toggles {
  sortOrder = SortOrder.ALPHABETICAL;
}

export enum SortOrder { ALPHABETICAL = "ALPHABETICAL", TIME = "TIME",}
