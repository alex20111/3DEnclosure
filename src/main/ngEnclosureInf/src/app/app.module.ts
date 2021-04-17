import { PiWebSocketService } from './services/pi-web-socket.service';
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
import { ConfigComponent } from './config/config.component';
import { NumberKeypadModalComponent } from './_helper/number-keypad-modal/number-keypad-modal.component';
import { PrintingComponent } from './printing/printing.component';
import { CountdownComponent } from './_helper/countdown/countdown.component';
import { ExtrAutoButtonComponent } from './extractor/extr-auto-button/extr-auto-button.component';
import { GcodeFileUploadComponent } from './gcode-file-upload/gcode-file-upload.component';
import { DndDirective } from './directive/dnd.directive';
import { ProgressComponent } from './_helper/progress/progress.component';
import { SerialTerminalComponent } from './serial-terminal/serial-terminal.component';


@NgModule({
  declarations: [
    AppComponent,
    LcdDashboardComponent,
    ExtractorComponent,
    ConfigComponent,
    NumberKeypadModalComponent,
    PrintingComponent,
    CountdownComponent,
    ExtrAutoButtonComponent,
    GcodeFileUploadComponent,
    DndDirective,
    ProgressComponent,
    SerialTerminalComponent
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
