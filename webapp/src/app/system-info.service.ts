import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {SystemInfo} from './shared/SystemInfo';

@Injectable({
  providedIn: 'root'
})
export class SystemInfoService {
  constructor(private httpClient: HttpClient) {
  }

  public getInfo(): Observable<SystemInfo> {
    return this.httpClient.get<SystemInfo>('/api/system/info');
  }
}
