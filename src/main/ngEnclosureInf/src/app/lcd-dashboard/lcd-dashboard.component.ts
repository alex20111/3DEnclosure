import { GeneralService, DashBoard } from './../services/general.service';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription, timer } from 'rxjs';
import { faThermometerHalf, faLightbulb, faFan, faTachometerAlt, faCog } from '@fortawesome/free-solid-svg-icons';


@Component({
  selector: 'app-lcd-dashboard',
  templateUrl: './lcd-dashboard.component.html',
  styleUrls: ['./lcd-dashboard.component.css']
})
export class LcdDashboardComponent implements OnInit, OnDestroy {

  dashBoard!: DashBoard;

  lightColor: string = 'red';

  //alerts
  error: string = '';
  message: string = '';

  dashBoardTimer: Subscription | undefined;

  //icons
  faThermometerHalf = faThermometerHalf;
  faLightbulb = faLightbulb;
  faFan = faFan;
  faTachometerAlt = faTachometerAlt;
  faCog = faCog;

  constructor(
    private generalService: GeneralService) { }


  ngOnInit(): void {
  console.log("windows size: " , window.innerWidth);
  this.message = `width: ${window.innerWidth}   -  height: ${window.innerHeight}`; 

    this.dashBoardTimer = timer(2000, 6000).subscribe(val => {
      this.generalService.dashBoard().subscribe(dshboard => {
        // console.log("get: " , new Date());
        this.dashBoard = dshboard as DashBoard;

        if (this.dashBoard.lightOn) {
          this.lightColor = 'rgb(12, 247, 12)'
        } else {
          this.lightColor = 'red';
        }

      },
        err => {
          this.showError(err);
        });
    });
  }

  ngOnDestroy(): void {
    this.dashBoardTimer?.unsubscribe();
  }

  showError(httpError: any): void {
    this.error = httpError.message + ' ' + httpError.error.error;
    console.log("httpError", httpError);
  }

  //shutdown the system.
  shutDown() {
    this.generalService.shutdownSystem().subscribe(success => {
      this.message = success.message;
    },
      httpError => {
        this.error = httpError.message + ' ' + httpError.error.error;
      })
  }
}
