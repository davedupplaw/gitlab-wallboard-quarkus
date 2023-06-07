import {Build} from "./Build";
import {Quality} from './Quality';

export interface Project {
  id: string;
  name: string;
  lastBuild?: Build;
  projectUrl: string;
  quality?: Quality
}
