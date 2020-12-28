import { TemperatureService } from './../services/temperature.service';
import { FanService } from './../services/fan.service';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription, timer } from 'rxjs';
import { LightService } from '../services/light.service';
import { faThermometerHalf, faLightbulb, faFan , faTachometerAlt} from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-lcd-dashboard',
  templateUrl: './lcd-dashboard.component.html',
  styleUrls: ['./lcd-dashboard.component.css']
})
export class LcdDashboardComponent implements OnInit, OnDestroy {

  extrFanStatus: string = '';
  extrFanRpm: string = '';
  extrSpeed: number = 0;
  extrSpeedLoading: boolean = false;
  btn1Loading: boolean = false;
  btn2Loading: boolean = false;
  btn3Loading: boolean = false;
  btn4Loading: boolean = false;
 
  disableIncBtn: boolean = false;
  disableDecBtn: boolean = false;

  lightOn: boolean = false;
  lightTest: string = "OFF";
  lightLoading: boolean = false;

  encTemperature: string = "55";

  error: string = '';

  fanRpmtimer: Subscription | undefined;
  temp1Timer: Subscription | undefined;

  //icons
  faThermometerHalf = faThermometerHalf;
  faLightbulb = faLightbulb;
  faFan = faFan;
  faTachometerAlt = faTachometerAlt;

  constructor(private fanService: FanService,
     private lightService: LightService,
      private temperatureService: TemperatureService) { }


  ngOnInit(): void {
    this.fanRpmtimer = timer(2000, 6000).subscribe(val => {
      this.fanService.getFanRmp().subscribe(rpm => {
        this.extrFanRpm = rpm;
      },
        error => {
          console.log('error', error);
          this.error = error.message + ' ' + error.error.error;
        });
    });

    // temperature on a timer
    this.temp1Timer = timer(3000, 6000).subscribe(val => {
      this.temperatureService.getEnclosureTemperature().subscribe(temp => {
        this.encTemperature = temp;
      },
        error => {
          console.log('error', error);
          this.error = error.message + ' ' + error.error.error;
        });
    });

    //get the fan speed to set the inital values
    this.fanService.getFanSpeed().subscribe(speed => {
      if (speed === -1) {
        this.disableDecBtn = true;
        speed = 0;
      }
      this.fanService.setFanSpeed(speed).subscribe(
        result => {
          this.extrSpeed = parseInt(result.message);
          this.extrSpeedLoading = false;

        }, error => {
          this.showError(error);
        });
    },
      err => {
        this.showError(err);
      }
    );
  }

  ngOnDestroy(): void {
    this.fanRpmtimer?.unsubscribe();
  }

  extrFanSpeedDec() {
    //re-enable increase button
    this.disableIncBtn = false;

    let extTemp = this.extrSpeed;
    if (extTemp > 0) {
      extTemp -= 10;
    }

    if (extTemp === 0) {
      extTemp = 0;
      this.disableDecBtn = true;
    }
    this.extrSpeedLoading = true;
    this.btn1Loading = true;
    this.fanService.setFanSpeed(extTemp).subscribe(
      result => {
        this.extrSpeed = parseInt(result.message);
        this.extrSpeedLoading = false;
        this.btn1Loading = false;
      }, error => {
        this.showError(error);
      });


  }
  extrFanSpeedInc() {
    this.disableDecBtn = false;

    let extTemp = this.extrSpeed;
    if (extTemp >= 0 && extTemp < 100) {
      extTemp += 10;
    } else if (extTemp < 0) {
      extTemp = 0;
    }

    if (!this.disableIncBtn) {
      this.extrSpeedLoading = true;
      this.btn2Loading = true;
      this.fanService.setFanSpeed(extTemp).subscribe(
        result => {
          console.log(" this.extrSpeed: ", result);
          this.extrSpeed = parseInt(result.message);
          this.extrSpeedLoading = false;
          this.btn2Loading = false;
        }, error => {
          this.showError(error);
        });
    }
    // disable if we are at 100%
    if (extTemp === 100) {
      this.disableIncBtn = true;
    }
  }
  extrFanFullSpeed(){
    if (!this.disableIncBtn) {
      this.extrSpeedLoading = true;
      this.btn3Loading = true;
      this.fanService.setFanSpeed(100).subscribe(
        result => {
          console.log(" this.extrSpeed: ", result);
          this.extrSpeed = parseInt(result.message);
          this.extrSpeedLoading = false;
          this.btn3Loading = false;
        }, error => {
          this.showError(error);
        });
        this.disableIncBtn = true;
        this.disableDecBtn = false;
    }
    

  }
  extrFanStop(){
    if (!this.disableDecBtn) {
      this.extrSpeedLoading = true;
      this.btn4Loading = true;
      this.fanService.setFanSpeed(0).subscribe(
        result => {
          console.log(" this.extrSpeed: ", result);
          this.extrSpeed = parseInt(result.message);
          this.extrSpeedLoading = false;
          this.btn4Loading = false;
        }, error => {
          this.showError(error);
        });
        this.disableDecBtn = true;
        this.disableIncBtn = false;
    }
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
    this.extrSpeedLoading = false;
    this.btn1Loading = false;
    this.btn2Loading = false;
    this.btn3Loading = false;
    this.btn4Loading = false;
    console.log("httpError", httpError);
  }

  close() {
    // this.alerts.splice(this.alerts.indexOf(alert), 1);
    this.error = '';
  }
}
