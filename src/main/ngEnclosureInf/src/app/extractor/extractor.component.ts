import { Component, OnDestroy, OnInit } from '@angular/core';
import { faFan, faQuestion, faTachometerAlt, faThermometerHalf } from '@fortawesome/free-solid-svg-icons';
import { Subscription } from 'rxjs';
import { FanService } from '../services/fan.service';

@Component({
  selector: 'app-extractor',
  templateUrl: './extractor.component.html',
  styleUrls: ['./extractor.component.css']
})
export class ExtractorComponent implements OnInit, OnDestroy {

  extrSpeed: number = 0;
  extrFanRpm: string = '';
  extrSpeedLoading: boolean = false;

  fanRpmtimer: Subscription | undefined;

  btn1Loading: boolean = false;
  btn2Loading: boolean = false;
  btn3Loading: boolean = false;
  btn4Loading: boolean = false;

  disableIncBtn: boolean = false;
  disableDecBtn: boolean = false;

  autoExtrFan: boolean = false;
  autoExtrFanText: string = 'OFF';


  error: string = '';

  //icons
  faTachometerAlt = faTachometerAlt;
  faFan = faFan;
  faQuestion = faQuestion;

  constructor(private fanService: FanService) { }
  ngOnDestroy(): void {
    this.fanRpmtimer?.unsubscribe();
  }

  ngOnInit(): void {

    //get config info for extractor fan status to set the button true or false...  


    //   //get the fan speed to set the inital values
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

        // this.fanRpmtimer = timer(2000, 6000).subscribe(val => {
    //   this.fanService.getFanRmp().subscribe(rpm => {
    //     this.extrFanRpm = rpm;
    //   },
    //     error => {
    //       console.log('error', error);
    //       this.error = error.message + ' ' + error.error.error;
    //     });
    // });
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
  extrFanFullSpeed() {
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
  extrFanStop() {
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

  autoFanBtn(){
    this.autoExtrFan = !this.autoExtrFan;
    this.autoExtrFanText = 'ON';
  }

  showError(httpError: any): void {
    this.error = httpError.message + ' ' + httpError.error.error;
    console.log("httpError", httpError);
  }
}
