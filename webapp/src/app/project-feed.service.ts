import {Injectable} from '@angular/core';
import {environment} from '../environments/environment';
import {webSocket} from 'rxjs/webSocket';
import {BehaviorSubject, Subject, Subscription} from 'rxjs';

export interface ProjectFeedMessage {
  type: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProjectFeedService {
  private messageStream$ = new Subject<ProjectFeedMessage>();
  private connection$?: Subscription;
  private isConnected$ = new BehaviorSubject(false);

  constructor() {
    this.connect(environment.websocketPath);
  }

  public isConnectedObservable() {
    return this.isConnected$;
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
        next: () => {
          this.isConnected$.next(false);
          this.connectWebsocket(url);
        }
      }
    }).subscribe(m => {
      this.isConnected$.next(true);
      this.messageStream$.next(m);
    });
    console.log(`Successfully connected: ${url}`);
  }
}
