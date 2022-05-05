import {Component, OnInit} from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import {SystemInfoService} from '../system-info.service';
import {SubSink} from 'subsink';
import {SystemInfo} from '../shared/SystemInfo';

@Component({
  selector: 'app-version',
  templateUrl: './version.component.html',
  styleUrls: ['./version.component.scss']
})
export class VersionComponent implements OnInit {
  version = new BehaviorSubject<string>('');
  name = new BehaviorSubject<string>('');
  build = new BehaviorSubject<string>('');

  private subsink = new SubSink();

  constructor(private systemInfo: SystemInfoService) {
  }

  ngOnInit(): void {
    this.subsink.sink = this.systemInfo.getInfo().subscribe((v: SystemInfo) => {
      this.name.next(v.name);
      this.version.next(v.version.version);
      this.build.next(v.version.buildNumber);
    });
  }
}
