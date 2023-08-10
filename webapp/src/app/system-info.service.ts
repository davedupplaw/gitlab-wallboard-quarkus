import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {map, Observable} from 'rxjs';
import {SystemInfo, Toggles} from './shared/SystemInfo';

@Injectable({
  providedIn: 'root'
})
export class SystemInfoService {
  constructor(private httpClient: HttpClient) {
  }

  public getInfo(): Observable<SystemInfo> {
    return this.httpClient.get<SystemInfo>('/api/system/info');
  }

  public getToggles(): Observable<Toggles> {
    return this.getInfo().pipe(map(it => it.toggles));
  }
}
