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

  lightOn: boolean = false;
  lightTest: string = "OFF";
  lightLoading: boolean = false;

  encTemperature: string = "99";

  //air quality
  airVoc: string = "";

  //alerts
  error: string = '';
  message: string = '';

  dashBoardTimer: Subscription | undefined;
  temp1Timer: Subscription | undefined;

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
      },
        err => {
          this.showError(err);
        });
    });



    ///////////////////replace all this with a dashboard timer values.. one query to fetch all the values for the dashboard. 


    // this.fanRpmtimer = timer(2000, 6000).subscribe(val => {
    //   this.fanService.getFanRmp().subscribe(rpm => {
    //     this.extrFanRpm = rpm;
    //   },
    //     error => {
    //       console.log('error', error);
    //       this.error = error.message + ' ' + error.error.error;
    //     });
    // });

    // // temperature on a timer
    // this.temp1Timer = timer(3000, 6000).subscribe(val => {
    //   this.temperatureService.getEnclosureTemperature().subscribe(temp => {
    //     this.encTemperature = temp;
    //   },
    //     error => {
    //       console.log('error', error);
    //       this.error = error.message + ' ' + error.error.error;
    //     });
    // });

    /////////////   get the dashboard 

    // //get the fan speed to set the inital values
    // this.fanService.getFanSpeed().subscribe(speed => {
    //   if (speed === -1) {
    //     this.disableDecBtn = true;
    //     speed = 0;
    //   }
    //   this.fanService.setFanSpeed(speed).subscribe(
    //     result => {
    //       this.extrSpeed = parseInt(result.message);
    //       this.extrSpeedLoading = false;

    //     }, error => {
    //       this.showError(error);
    //     });
    // },
    //   err => {
    //     this.showError(err);
    //   }
    // );

    ////////////////////////
  }

  ngOnDestroy(): void {
    this.dashBoardTimer?.unsubscribe();
    this.temp1Timer?.unsubscribe();
  }



  light() {
    this.lightOn = !this.lightOn;
    if (this.lightOn) {
      this.lightTest = 'ON';
    } else {
      this.lightTest = 'OFF';
    }

    this.lightLoading = true;
    this.lightService.switchLightState(this.lightOn).subscribe(
      result => {
        console.log('li:', result);
        if (result.message === 'true') {
          this.lightTest = 'ON';
          this.lightOn = true;
        } else {
          this.lightTest = 'OFF';
          this.lightOn = false;
        }
        this.lightLoading = false;
      },
      err => {
        this.error = err.message + ' ' + err.error.error;
        this.lightTest = 'OFF';
        this.lightOn = false;
        this.lightLoading = false;
      }
    )
    console.log("light: ", this.lightOn);
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
