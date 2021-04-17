import { SerialTerminalComponent } from './serial-terminal/serial-terminal.component';
import { GcodeFileUploadComponent } from './gcode-file-upload/gcode-file-upload.component';
import { PrintingComponent } from './printing/printing.component';
import { ConfigComponent } from './config/config.component';
import { ExtractorComponent } from './extractor/extractor.component';
import { LcdDashboardComponent } from './lcd-dashboard/lcd-dashboard.component';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

const routes: Routes = [
  { path: '', component: LcdDashboardComponent },
  { path: 'extracFan', component: ExtractorComponent },
  { path: 'config', component: ConfigComponent },
  { path: 'print', component: PrintingComponent },
  { path: 'fileUpload', component: GcodeFileUploadComponent },
  { path: 'terminal', component: SerialTerminalComponent }

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
