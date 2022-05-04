import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {VersionComponent} from './version/version.component';
import {HttpClientModule} from '@angular/common/http';
import {ProjectStatusFeedComponent} from './project-status-feed/project-status-feed.component';
import { NotificationBarComponent } from './notification-bar/notification-bar.component';
import { ThrobberComponent } from './throbber/throbber.component';

@NgModule({
  declarations: [
    AppComponent,
    VersionComponent,
    ProjectStatusFeedComponent,
    NotificationBarComponent,
    ThrobberComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
