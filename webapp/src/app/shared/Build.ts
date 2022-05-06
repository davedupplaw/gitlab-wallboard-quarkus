export type BuildStatus = 'SUCCESS' | 'FAIL' | 'RUNNING' | 'WARNING' | 'UNKNOWN';

export class Build {
  constructor(
    public id: string,
    public projectId: string,
    public buildUrl: string,
    public status: BuildStatus,
    public lastBuildTimestamp: string,
    public user: string,
    public currentStatusReasons: string[],
    public textStatus?: string,
  ) {
  }
}

export const preferredBuildOrder = [
  'FAIL', 'WARNING', 'RUNNING', 'SUCCESS', 'UNKNOWN'
]

