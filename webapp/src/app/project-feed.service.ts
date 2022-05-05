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

  public connect(path: string = environment.websocketPath): Subject<ProjectFeedMessage> {
    if (!this.connection$) {
      const protocol = window.location.protocol;
      const hostname = window.location.hostname;
      const port = window.location.port;
      if (protocol === 'https:') {
        this.connectWebsocket(`wss://${hostname}:${port}${path}`);
      } else if (protocol === 'http:') {
        this.connectWebsocket(`ws://${hostname}:${port}${path}`);
      } else {
        console.warn(`Unknown location protocol ${protocol}. Don't know how to connect websocket.`);
      }
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
