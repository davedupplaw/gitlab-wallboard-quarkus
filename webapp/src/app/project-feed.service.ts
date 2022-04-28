import {Injectable} from '@angular/core';
import {map, Observable, Observer} from 'rxjs';
import {Message} from '@angular/compiler/src/i18n/i18n_ast';
import {environment} from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProjectFeedService {
  private subject?: Observable<Message>;

  constructor() {
    this.connect(environment.websocketPath);
  }

  public connect(url: string): Observable<Message> {
    if (!this.subject) {
      this.subject = this.create(url).pipe(
        map(
          (response: MessageEvent): Message => {
            console.log(response.data);
            return JSON.parse(response.data);
          }
        )
      );
      console.log("Successfully connected: " + url);
    }

    return this.subject;
  }

  private create(url: string): Observable<MessageEvent> {
    const ws = new WebSocket(url);

    return new Observable((obs: Observer<MessageEvent>) => {
      ws.onmessage = obs.next.bind(obs);
      ws.onerror = obs.error.bind(obs);
      ws.onclose = obs.complete.bind(obs);
      return ws.close.bind(ws);
    });
  }
}
