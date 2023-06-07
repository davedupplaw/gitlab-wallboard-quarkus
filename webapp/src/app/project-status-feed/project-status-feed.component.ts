import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {ProjectFeedMessage, ProjectFeedService} from '../project-feed.service';
import {SubSink} from 'subsink';
import * as d3 from 'd3';
import {Project} from '../shared/Project';
import {Build, preferredBuildOrder} from "../shared/Build";
import {BehaviorSubject, combineLatest, Observer, Subject} from 'rxjs';
import * as moment from 'moment';
import {Quality, SonarqubeQuality} from '../shared/Quality';

type ProjectBuild = { project: Project, build?: Build };

@Component({
  selector: 'app-project-status-feed',
  templateUrl: './project-status-feed.component.html',
  styleUrls: ['./project-status-feed.component.scss']
})
export class ProjectStatusFeedComponent implements OnInit {
  @ViewChild("fixture") fixture?: ElementRef;
  private subSink = new SubSink();
  private feed: Subject<ProjectFeedMessage>;
  private _projects: { [key: string]: Project } = {}
  private _builds: { [key: string]: Build } = {}
  private projects$: BehaviorSubject<{ [key: string]: Project }> = new BehaviorSubject(this._projects);
  private builds$: BehaviorSubject<{ [key: string]: Build }> = new BehaviorSubject(this._builds);

  constructor(private projectFeed: ProjectFeedService) {
    this.feed = this.projectFeed.connect();
  }

  private static isSonarQualityInfo(quality: Quality | undefined): quality is SonarqubeQuality {
    console.log("Testing quality", quality);
    return !!quality && !!(quality as SonarqubeQuality).securityRating;
  }

  private static buildCard(enter: d3.Selection<d3.EnterElement, ProjectBuild, HTMLDivElement, unknown>):
    d3.Selection<HTMLDivElement, ProjectBuild, HTMLDivElement, unknown> {

    // <div class="build">...</div>
    const card = enter.append('div').attr('class', 'build');

    // <div class="title"><a href={{url}}>{{name}}</a></div>
    card.append('div').append('a').attr('class', 'title')
        .call(ProjectStatusFeedComponent.updateCardTitle);

    // <div class="project-id">{{id}}</div>
    card.append('div').attr('class', 'project-id').html(d => `id: ${d.project.id}`);

    // <div class="sonar-info">...</div>
    card.append('div').classed('sonar-info', true).call(ProjectStatusFeedComponent.createQualitySection);


    // <div class="status">{{status}}</div>
    card.append('div').attr('class', 'status-reason').call(ProjectStatusFeedComponent.updateReason)

    // <div class="extra">...</div>
    const extra = card.append('div').attr('class', 'extra');

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

    return card;
  }

  private static updateCardTitle(x: d3.Selection<any, ProjectBuild, any, any>): d3.Selection<any, ProjectBuild, any, any> {
    return x.attr('href', d => d.project.projectUrl).html(d => d.project.name);
  }

  private static updateBuildInfo(x: d3.Selection<any, ProjectBuild, any, any>): d3.Selection<any, ProjectBuild, any, any> {
    return x.attr('href', d => d.build?.buildUrl ?? '')
            .html(d => `#${d.build?.id ?? '??'}`)
  }

  private static createQualitySection(x: d3.Selection<any, ProjectBuild, any, any>): d3.Selection<any, ProjectBuild, any, any> {
    return x.style('display', d => ProjectStatusFeedComponent.isSonarQualityInfo(d.project.quality) ? 'flex' : 'none')
            .call(_ => {
              x.append('div').classed('reliability', true)
              x.append('div').classed('coverage', true)
              x.append('div').classed('duplications', true)
              x.append('div').classed('security', true)
            })
            .call(ProjectStatusFeedComponent.updateQuality);
  }

  private static updateQuality(x: d3.Selection<any, ProjectBuild, any, any>): d3.Selection<any, ProjectBuild, any, any> {
    return x.style('display', d => ProjectStatusFeedComponent.isSonarQualityInfo(d.project.quality) ? 'flex' : 'none')
            .call(x => {
              x.select('.reliability')
               .html(d => {
                 const reliability = ProjectStatusFeedComponent.toLetter((d.project.quality as SonarqubeQuality)?.reliabilityRating);
                 const style = ProjectStatusFeedComponent.goodOrBadRating(reliability)
                 return `<span class="label">reliability</span><span class="value ${style}">${reliability}</span>`;
               });
                x.select('.coverage')
                 .html(d => {
                   const coverage = (d.project.quality as SonarqubeQuality)?.coverage;
                   const style = ProjectStatusFeedComponent.goodOrBadValue(coverage, true);
                   return `<span class="label">coverage</span><span class="value ${style}">${coverage}%</span>`
                 })
                x.select('.duplications')
                 .html(d => {
                   const duplications = (d.project.quality as SonarqubeQuality)?.duplications;
                   const style = ProjectStatusFeedComponent.goodOrBadValue(duplications, false);
                   return `<span class="label">duplications</span><span class="value ${style}">${duplications}%</span>`
                 })
              x.select('.security')
               .html(d => {
                 const security = ProjectStatusFeedComponent.toLetter((d.project.quality as SonarqubeQuality)?.securityRating);
                 const style = ProjectStatusFeedComponent.goodOrBadRating(security ?? 'Z')
                 return `<span class="label">security</span><span class="value ${style}">${security}</span>`;
               });
            });
  }

  private static toLetter(n?: number) {
    if (!n) return null;
    return String.fromCharCode(n + 65);
  }

  private static goodOrBadRating(r: string | null): 'GOOD' | 'OK' | 'BAD' {
    if (r == null) return 'BAD';
    switch (r) {
      case 'A':
        return 'GOOD';
      case 'B':
      case 'C':
      case 'D':
        return 'OK';
      default:
        return 'BAD';
    }
  }

  private static goodOrBadValue(v?: number | null, zeroWorst?: boolean): 'GOOD' | 'OK' | 'BAD' {
    if (v == null) return 'BAD';
    const value = zeroWorst ? v : 100 - v;
    if (value > 80) return 'GOOD';
    if (value > 50) return 'OK';
    return 'BAD';
  }

  private static updateBuildTime(x: d3.Selection<any, ProjectBuild, any, any>): d3.Selection<any, ProjectBuild, any, any> {
    return x.html(d => `${
      d.build?.lastBuildTimestamp
        ? moment(d.build?.lastBuildTimestamp).fromNow()
        : '??'
    }`);
  }

  private static updateBuildUser(x: d3.Selection<any, ProjectBuild, any, any>): d3.Selection<any, ProjectBuild, any, any> {
    return x.html(d => `${d.build?.user}`);
  }

  private static updateStatus(x: d3.Selection<any, ProjectBuild, any, any>): d3.Selection<any, ProjectBuild, any, any> {
    return x.html(d => `${d.build?.textStatus ?? 'unknown'}`);
  }

  private static updateReason(x: d3.Selection<any, ProjectBuild, any, any>): d3.Selection<any, ProjectBuild, any, any> {
    return x.html(d => d.build?.currentStatusReasons.map(it => `<li>${it}</li>`).join('') ?? '');
  }

  private static updateBuildCardClass(x: d3.Selection<any, ProjectBuild, any, any>): d3.Selection<any, ProjectBuild, any, any> {
    return x.classed('success', d => d.build?.status == 'SUCCESS')
            .classed('fail', d => d.build?.status == 'FAIL')
            .classed('running', d => d.build?.status == 'RUNNING')
            .classed('warning', d => d.build?.status == 'WARNING');
  }

  ngOnInit(): void {
    this.subSink.sink = this.feed.subscribe({
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

    this.subSink.sink = combineLatest([
      this.projects$, this.builds$
    ]).subscribe(([projects, builds]) => this.updateCards(projects, builds));
  }

  private updateOrAddProject(project: Project) {
    this._projects[project.id] = {
      id: project.id,
      name: project.name,
      projectUrl: project.projectUrl,
      quality: project.quality
    } as Project;
    this.projects$.next(this._projects);
  }

  private updateBuild(build: Build) {
    this._builds[build.projectId] = build;
    this.builds$.next(this._builds);
  }

  private updateCards(projects: { [key: string]: Project }, builds: { [key: string]: Build }) {
    const pairs = Object.values(projects).map(
      (p: Project) => ({project: p, build: builds[p.id]} as ProjectBuild)
    );

    d3.select(this.fixture?.nativeElement)
      .selectAll('div.build')
      .data(
        Object.values(pairs).sort((a, b) =>
          d3.ascending(
            preferredBuildOrder.indexOf(a.build?.status ?? 'UNKNOWN'),
            preferredBuildOrder.indexOf(b.build?.status ?? 'UNKNOWN')
          ) || d3.ascending(a.project.name.toLocaleLowerCase(), b.project.name.toLocaleLowerCase())
        ),
        d => (d as ProjectBuild).project.id
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
         .call(x => x.select('.status-reason').call(ProjectStatusFeedComponent.updateReason))
         .call(x => x.select('.sonar-info').call(ProjectStatusFeedComponent.updateQuality))
         .call(ProjectStatusFeedComponent.updateBuildCardClass)
      })
    ;
  }
}
