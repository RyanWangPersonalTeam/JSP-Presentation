import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule,HttpClient } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { environment } from '../environments/environment';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { GSTCComponent, AngularGanttScheduleTimelineCalendarModule } from 'angular-gantt-schedule-timeline-calendar';
import { DatePipe } from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCardModule } from '@angular/material/card';
import {MatInputModule} from '@angular/material/input';
import {MatRadioModule} from '@angular/material/radio';
import {MatIconModule} from '@angular/material/icon';
import {MatDialogModule} from '@angular/material';
import { ReactiveFormsModule,FormsModule } from '@angular/forms';
import { AlgorithmSettingComponent } from './algorithm-setting/algorithm-setting.component';
import { HomeComponent } from './home/home.component';
import { MarkdownModule } from 'ngx-markdown';
import { DirectFormulationIntroComponent } from './doc/direct-formulation-intro/direct-formulation-intro.component';

@NgModule({
  declarations: [
    AppComponent,
    //GSTCComponent,
    AlgorithmSettingComponent,
    HomeComponent,
    DirectFormulationIntroComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    BrowserAnimationsModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatCardModule,
    MatInputModule,
    MatRadioModule,
    ReactiveFormsModule,
    MatIconModule,
    MatDialogModule,
    FormsModule,
    AngularGanttScheduleTimelineCalendarModule,
    MarkdownModule.forRoot({ loader: HttpClient }),
  ],
  providers: [DatePipe],
  bootstrap: [AppComponent],
  entryComponents: [
    AlgorithmSettingComponent
   ]
})
export class AppModule { }
