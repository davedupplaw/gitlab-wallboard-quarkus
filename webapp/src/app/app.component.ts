import { Component } from '@angular/core';
import {ProjectFeedService} from './project-feed.service';
import {SubSink} from 'subsink';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'webapp';
  noConnection = false;

  private subsink = new SubSink();

  constructor(
    private projectFeedService: ProjectFeedService
  ) {
    this.subsink.sink = projectFeedService.isConnectedObservable().subscribe(
      connected => this.noConnection = !connected
    );
  }
}
