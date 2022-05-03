import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {ProjectFeedMessage, ProjectFeedService} from '../project-feed.service';
import {SubSink} from 'subsink';
import {WebSocketSubject} from 'rxjs/webSocket';
import * as d3 from 'd3';
import {Project} from '../shared/Project';
import {Build, preferredBuildOrder} from "../shared/Build";
import {BehaviorSubject} from 'rxjs';

@Component({
  selector: 'app-project-status-feed',
  templateUrl: './project-status-feed.component.html',
  styleUrls: ['./project-status-feed.component.scss']
})
export class ProjectStatusFeedComponent implements OnInit {
  private subsink = new SubSink();
  private feed: WebSocketSubject<ProjectFeedMessage>;

  private _data: { [key: string]: Project } = {}
  private data$: BehaviorSubject<{ [key: string]: Project }> = new BehaviorSubject(this._data);

  @ViewChild("fixture") fixture?: ElementRef;

  constructor(private projectFeed: ProjectFeedService) {
    this.feed = this.projectFeed.connect();
  }

  ngOnInit(): void {
    this.updateCards();
    this.subsink.sink = this.feed.subscribe(message => {
      switch (message.type) {
        case 'project-info': {
          this.updateOrAddProject((message as any) as Project);
          break;
        }
        case 'build-info': {
          this.updateBuild((message as any) as Build);
          break;
        }
      }
    });
  }

  private updateOrAddProject(project: Project) {
    const existing: any = this._data[project.id];

    if (existing) {
      existing.name = project.name;
    } else {
      this._data[project.id] = {id: project.id, name: project.name} as Project;
    }

    this.data$.next(this._data);
  }

  private updateBuild(build: Build) {
    let existing: any = this._data[build.projectId];

    if (!existing) {
      existing = {id: build.projectId, name: 'unknown'} as Project;
      this._data[build.projectId] = existing;
    }

    existing.lastBuild = build;
    this.data$.next(this._data);
  }

  private updateCards() {
    this.subsink.sink = this.data$.subscribe(data => {
      d3.select(this.fixture?.nativeElement)
        .selectAll('div.build')
        .data(
          Object.values(data).sort((a, b) =>
            d3.ascending(
              preferredBuildOrder.indexOf(a.lastBuild?.status || 'UNKNOWN'),
              preferredBuildOrder.indexOf(b.lastBuild?.status || 'UNKNOWN')
            ) || d3.ascending(a.name, b.name)
          ),
          d => (d as any).id
        )
        .join(
          enter => this.buildCard(enter),
          update => update,
          exit => exit.remove()
        )
        .call(x => {
          x.call(x => x.select('.title').html(d => d.name))
           .call(x => x.select('.id a')
                       .attr('href', d => d.lastBuild?.buildUrl || '')
                       .html(d => `#${d.lastBuild?.id || '??'}`))
           .call(x => x.select('.user').html(d => `${d.lastBuild?.user}`))
           .classed('success', d => d.lastBuild?.status == 'SUCCESS')
           .classed('fail', d => d.lastBuild?.status == 'FAIL')
           .classed('running', d => d.lastBuild?.status == 'RUNNING')
           .classed('warning', d => d.lastBuild?.status == 'WARNING')
        })
      ;
    });
  }

  private buildCard(enter: d3.Selection<d3.EnterElement, Project, HTMLDivElement, unknown>):
    d3.Selection<HTMLDivElement, Project, HTMLDivElement, unknown> {
    const div = enter.append('div').attr('class', 'build');
    div.append('div').attr('class', 'title').html(d => d.name);
    div.append('div').attr('class', 'project-id').html(d => `id: ${d.id}`);
    div.append('div').attr('class', 'user').html(d => `${d.lastBuild?.user}`);
    div.append('div')
       .attr('class', 'id')
       .append('a')
       .attr('href', d => d.lastBuild?.buildUrl || '')
       .html(d => `#${d.lastBuild?.id || '??'}`);
    return div;
  }
}
