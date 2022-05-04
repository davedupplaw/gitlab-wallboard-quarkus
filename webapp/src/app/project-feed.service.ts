import {Injectable} from '@angular/core';
import {environment} from '../environments/environment';
import {webSocket} from 'rxjs/webSocket';
import {Subject, Subscription} from 'rxjs';

export interface ProjectFeedMessage {
  type: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProjectFeedService {
  private messageStream$ = new Subject<ProjectFeedMessage>();
  private connection$?: Subscription;

  constructor() {
    this.connect(environment.websocketPath);
  }

  public connect(url: string = environment.websocketPath): Subject<ProjectFeedMessage> {
    if (!this.connection$) {
      this.connectWebsocket(url);
    }

    return this.messageStream$;
  }

  private connectWebsocket(url: string) {
    console.log(`Connecting to ${url}`);
    this.connection$ = webSocket<ProjectFeedMessage>({
      url,
      closeObserver: {
        next: () => this.connectWebsocket(url)
      }
    }).subscribe(m => this.messageStream$.next(m));
    console.log(`Successfully connected: ${url}`);
  }
}
