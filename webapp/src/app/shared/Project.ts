import {Build} from "./Build";

export interface Project {
  id: string;
  name: string;
  lastBuild?: Build;
  projectUrl: string;
}
