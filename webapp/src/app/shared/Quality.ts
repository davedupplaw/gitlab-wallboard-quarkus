export interface Quality {
  component: string;
  url: string;
}

export class SonarqubeQuality implements Quality {
  constructor(
    public component: string,
    public url: string,
    public securityRating: number,
    public reliabilityRating: number,
    public coverage: number,
    public duplications: number,
  ) {
  }
}
