import {Component, OnInit} from '@angular/core';
import {map, Observable, take} from 'rxjs';
import {SystemInfoService} from '../system-info.service';

@Component({
  selector: 'app-version',
  templateUrl: './version.component.html',
  styleUrls: ['./version.component.scss']
})
export class VersionComponent implements OnInit {
  version?: Observable<string>;

  constructor(private systemInfo: SystemInfoService) {
  }

  ngOnInit(): void {
    this.version = this.systemInfo.getVersion().pipe(take(1), map((v: any) => v.version));
  }
}
