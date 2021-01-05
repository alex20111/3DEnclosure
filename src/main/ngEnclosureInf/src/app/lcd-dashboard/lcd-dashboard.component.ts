import { GeneralService, DashBoard } from './../services/general.service';
import { TemperatureService } from './../services/temperature.service';
import { FanService } from './../services/fan.service';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription, timer } from 'rxjs';
import { LightService } from '../services/light.service';
import { faThermometerHalf, faLightbulb, faFan, faTachometerAlt, faCog } from '@fortawesome/free-solid-svg-icons';
import { NgbDropdownConfig } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-lcd-dashboard',
  templateUrl: './lcd-dashboard.component.html',
  styleUrls: ['./lcd-dashboard.component.css']
})
export class LcdDashboardComponent implements OnInit, OnDestroy {

  extrFanRpm: number = 0;
  extrSpeed: number = 0;

  encTemperature: string = "99";

  lightOn: boolean = false;
  lightColor: string = 'red';

  //air quality
  airVoc: string = "";

  //alerts
  error: string = '';
  message: string = '';

  dashBoardTimer: Subscription | undefined;
  // temp1Timer: Subscription | undefined;

  //icons
  faThermometerHalf = faThermometerHalf;
  faLightbulb = faLightbulb;
  faFan = faFan;
  faTachometerAlt = faTachometerAlt;
  faCog = faCog;

  constructor(private fanService: FanService,
    private lightService: LightService,
    private temperatureService: TemperatureService,
    private generalService: GeneralService) { }


  ngOnInit(): void {



    this.dashBoardTimer = timer(2000, 6000).subscribe(val => {
      this.generalService.dashBoard().subscribe(dashboard => {
        const dboard: DashBoard = dashboard as DashBoard;
        this.extrFanRpm = dboard.extracFanRPM;
        this.extrSpeed = dboard.extracFanSpeed;
        this.lightOn = dboard.lightOn;
        this.encTemperature = dboard.temperature;
        this.airVoc = dboard.airQualityVoc;
        if (this.lightOn) {
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
