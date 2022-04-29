import {Injectable} from '@angular/core';
import {environment} from '../environments/environment';
import {webSocket, WebSocketSubject} from 'rxjs/webSocket';

export interface ProjectFeedMessage {
  type: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProjectFeedService {
  private connection$?: WebSocketSubject<ProjectFeedMessage>;

  constructor() {
    this.connect(environment.websocketPath);
  }

  public connect(url: string = environment.websocketPath): WebSocketSubject<ProjectFeedMessage> {
    if (!this.connection$) {
      console.log(`Connecting to ${url}`);
      this.connection$ = webSocket({url});
      console.log(`Successfully connected: ${url}`);
    }

    return this.connection$;
  }
}
