import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { VersionComponent } from './version/version.component';
import {HttpClientModule} from '@angular/common/http';
import { ProjectStatusFeedComponent } from './project-status-feed/project-status-feed.component';
import { BuildCardComponent } from './build-card/build-card.component';

@NgModule({
  declarations: [
    AppComponent,
    VersionComponent,
    ProjectStatusFeedComponent,
    BuildCardComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
