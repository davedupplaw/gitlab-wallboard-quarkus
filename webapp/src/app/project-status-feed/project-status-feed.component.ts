import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {ProjectFeedMessage, ProjectFeedService} from '../project-feed.service';
import {SubSink} from 'subsink';
import {WebSocketSubject} from 'rxjs/webSocket';
import * as d3 from 'd3';
import {Project} from '../shared/Project';

@Component({
  selector: 'app-project-status-feed',
  templateUrl: './project-status-feed.component.html',
  styleUrls: ['./project-status-feed.component.scss']
})
export class ProjectStatusFeedComponent implements OnInit {
  private subsink = new SubSink();
  private feed: WebSocketSubject<ProjectFeedMessage>;

  private data: Project[] = [];

  @ViewChild("fixture") fixture?: ElementRef;

  constructor(private projectFeed: ProjectFeedService) {
    this.feed = this.projectFeed.connect();
  }

  ngOnInit(): void {
    this.subsink.sink = this.feed.subscribe(project => {
      switch(project.type) {
        case 'project-info': { this.updateOrAddProject(project); break; }
      }
      this.updateCards();
    });
  }

  private updateOrAddProject(m: ProjectFeedMessage) {
    const existing: any = this.data.find(p => p.id === m.id);
    if (existing) {
      Object.keys(m).filter(k => k !== "id").forEach(k => existing[k] = (m as any)[k]);
    } else {
      this.data.push(m);
    }
  }

  private updateCards() {
    d3.select(this.fixture?.nativeElement)
      .selectAll('app-build-card')
      .data(this.data)
      .join(
        enter => enter.append('app-build-card'),
        update => update,
        exit => exit.remove()
      )
  }
}