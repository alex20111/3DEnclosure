import { ExtractorComponent } from './extractor/extractor.component';
import { LcdDashboardComponent } from './lcd-dashboard/lcd-dashboard.component';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

const routes: Routes = [
  { path: '', component: LcdDashboardComponent },
  { path: 'extracFan', component: ExtractorComponent },

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
