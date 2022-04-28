import { Component, OnInit } from '@angular/core';
import {ProjectFeedMessage, ProjectFeedService} from '../project-feed.service';
import {Observable} from 'rxjs';
import {SubSink} from 'subsink';
import {WebSocketSubject} from 'rxjs/webSocket';

@Component({
  selector: 'app-project-status-feed',
  templateUrl: './project-status-feed.component.html',
  styleUrls: ['./project-status-feed.component.scss']
})
export class ProjectStatusFeedComponent implements OnInit {
  private subsink = new SubSink();
  private feed: WebSocketSubject<ProjectFeedMessage>;

  constructor(private projectFeed: ProjectFeedService) {
    this.feed = this.projectFeed.connect();
  }

  ngOnInit(): void {
    this.subsink.sink = this.feed.subscribe(m => {
      console.log("New message", m);
    });
  }
}
