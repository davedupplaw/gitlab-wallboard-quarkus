import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {ProjectFeedMessage, ProjectFeedService} from '../project-feed.service';
import {SubSink} from 'subsink';
import * as d3 from 'd3';
import {Project} from '../shared/Project';
import {Build, preferredBuildOrder} from "../shared/Build";
import {BehaviorSubject, Observer, Subject} from 'rxjs';
import * as moment from 'moment';

@Component({
  selector: 'app-project-status-feed',
  templateUrl: './project-status-feed.component.html',
  styleUrls: ['./project-status-feed.component.scss']
})
export class ProjectStatusFeedComponent implements OnInit {
  private subsink = new SubSink();
  private feed: Subject<ProjectFeedMessage>;

  private _data: { [key: string]: Project } = {}
  private data$: BehaviorSubject<{ [key: string]: Project }> = new BehaviorSubject(this._data);

  @ViewChild("fixture") fixture?: ElementRef;

  constructor(private projectFeed: ProjectFeedService) {
    this.feed = this.projectFeed.connect();
  }

  ngOnInit(): void {
    this.updateCards();
    this.subsink.sink = this.feed.subscribe({
      next: (message: ProjectFeedMessage) => {
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
      }
    } as Observer<ProjectFeedMessage>);
  }

  private updateOrAddProject(project: Project) {
    const existing: any = this._data[project.id];

    if (existing) {
      existing.name = project.name;
    } else {
      this._data[project.id] = {id: project.id, name: project.name, projectUrl: project.projectUrl} as Project;
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
          enter => ProjectStatusFeedComponent.buildCard(enter),
          update => update,
          exit => exit.remove()
        )
        .call(x => {
          x.call(x => x.select('.title').call(ProjectStatusFeedComponent.updateCardTitle))
           .call(x => x.select('.build-id a').call(ProjectStatusFeedComponent.updateBuildInfo))
           .call(x => x.select('.calendar').call(ProjectStatusFeedComponent.updateBuildTime))
           .call(x => x.select('.user').call(ProjectStatusFeedComponent.updateBuildUser))
           .call(x => x.select('.status').call(ProjectStatusFeedComponent.updateStatus))
           .call(ProjectStatusFeedComponent.updateBuildCardClass)
        })
      ;
    });
  }

  private static buildCard(enter: d3.Selection<d3.EnterElement, Project, HTMLDivElement, unknown>):
    d3.Selection<HTMLDivElement, Project, HTMLDivElement, unknown> {

    // <div class="build">...</div>
    const div = enter.append('div').attr('class', 'build');

    // <div class="title"><a href={{url}}>{{name}}</a></div>
    div.append('div').append('a').attr('class', 'title')
       .call(ProjectStatusFeedComponent.updateCardTitle);

    // <div class="project-id">{{id}}</div>
    div.append('div').attr('class', 'project-id').html(d => `id: ${d.id}`);

    // <div class="extra">...</div>
    const extra = div.append('div').attr('class', 'extra');

    // <div>
    //   <span class="user">{{user}}</span>
    //   <span class="status">{{status}}</span>
    // </div>
    extra.append('div').call(div => {
      div.append('span').attr('class', 'user').call(ProjectStatusFeedComponent.updateBuildUser);
      div.append('span').attr('class', 'status').call(ProjectStatusFeedComponent.updateStatus);
    });

    // <div>
    //   <span class="build-id"><a href={{buildUrl}}>#{{buildId}}</a></span>
    //   <span class="time">{{time}}</span>
    // </div>
    extra.append('div').call(div => {
      div.append('span').attr('class', 'build-id').append('a').call(ProjectStatusFeedComponent.updateBuildInfo);
      div.append('span').attr('class', 'calendar').call(ProjectStatusFeedComponent.updateBuildTime)
    });

    return div;
  }

  private static updateCardTitle(x: d3.Selection<any, Project, any, any>): d3.Selection<any, Project, any, any> {
    return x.attr('href', d => d.projectUrl).html(d => d.name);
  }

  private static updateBuildInfo(x: d3.Selection<any, Project, any, any>): d3.Selection<any, Project, any, any> {
    return x.attr('href', d => d.lastBuild?.buildUrl || '')
            .html(d => `#${d.lastBuild?.id || '??'}`)
  }

  private static updateBuildTime(x: d3.Selection<any, Project, any, any>): d3.Selection<any, Project, any, any> {
    return x.html(d => `${
      d.lastBuild?.lastBuildTimestamp
        ? moment(d.lastBuild?.lastBuildTimestamp).fromNow()
        : '??'
    }`);
  }

  private static updateBuildUser(x: d3.Selection<any, Project, any, any>): d3.Selection<any, Project, any, any> {
    return x.html(d => `${d.lastBuild?.user}`);
  }

  private static updateStatus(x: d3.Selection<any, Project, any, any>): d3.Selection<any, Project, any, any> {
    return x.html(d => `${d.lastBuild?.failReason || 'unknown'}`);
  }

  private static updateBuildCardClass(x: d3.Selection<any, Project, any, any>): d3.Selection<any, Project, any, any> {
    return x.classed('success', d => d.lastBuild?.status == 'SUCCESS')
            .classed('fail', d => d.lastBuild?.status == 'FAIL')
            .classed('running', d => d.lastBuild?.status == 'RUNNING')
            .classed('warning', d => d.lastBuild?.status == 'WARNING');
  }
}
