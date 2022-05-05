import { Component } from '@angular/core';
import {ProjectFeedService} from './project-feed.service';
import {SubSink} from 'subsink';
import {now} from 'moment';
import * as moment from 'moment';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'webapp';
  noConnection = false;

  private subsink = new SubSink();
  private lastUpdate = now();

  constructor(
    private projectFeedService: ProjectFeedService
  ) {
    this.subsink.sink = projectFeedService.isConnectedObservable().subscribe(
      connected => this.noConnection = !connected
    );

    this.subsink.sink = projectFeedService.connect().subscribe(_ => this.lastUpdate = now());
  }

  get lastUpdated() {
    return moment(this.lastUpdate).fromNow()
  }
}
