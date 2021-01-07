import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms'
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LcdDashboardComponent } from './lcd-dashboard/lcd-dashboard.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { HttpClientModule } from '@angular/common/http';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { ExtractorComponent } from './extractor/extractor.component';
import { LightsComponent } from './lights/lights.component';
import { ConfigComponent } from './config/config.component';
import { NumberKeypadModalComponent } from './_helper/number-keypad-modal/number-keypad-modal.component';

@NgModule({
  declarations: [
    AppComponent,
    LcdDashboardComponent,
    ExtractorComponent,
    LightsComponent,
    ConfigComponent,
    NumberKeypadModalComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    NgbModule,
    HttpClientModule,
    FontAwesomeModule,
    FormsModule,
    ReactiveFormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
