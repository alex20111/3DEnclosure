import { SessionService } from './../services/session.service';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { faFan, faQuestion, faTachometerAlt } from '@fortawesome/free-solid-svg-icons';
import { Subscription, timer } from 'rxjs';
import { Config } from '../services/config.service';
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

  autoExtrFanAuto: boolean = false;
  // autoExtrFanText: string = 'OFF';
  // autoExtrFanLoading: boolean = false;

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

    //get the fan speed to set the inital values
    this.fanService.getExtrFanParam().subscribe(param => {

      let fanSpd = 0;
      if (param.fanSpeed === -1) {
        this.disableDecBtn = true;
        fanSpd = 0;
      }else{
        fanSpd = param.fanSpeed ;
      }
      this.fanService.setFanSpeed(fanSpd).subscribe(
        result => {
          this.extrSpeed = parseInt(result.message);
          this.extrSpeedLoading = false;

        }, error => {
          this.showError(error);
        });

        this.autoExtrFanAuto = param.fanIsOnAuto;
        this.fanService.sendFanAutoMode( this.autoExtrFanAuto); 

    },
      err => {
        this.showError(err);
      }
    );

    //get the fan RPM
    this.fanRpmtimer = timer(500, 5000).subscribe(val => {
      this.fanService.getFanRmp().subscribe(rpm => {
        this.extrFanRpm = rpm;
      },
        error => {
          console.log('error', error);
          this.error = error.message + ' ' + error.error.error;
        });
    });
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

  // autoFanBtn() {
  //   this.autoExtrFanLoading = true;
  //   this.autoExtrFanText = "wait";
  //   // this.autoExtrFan = !this.autoExtrFan;  

  //   let cfg = new Config();
  //   cfg.extractorAuto = !this.autoExtrFanAuto;
  //   this.fanService.updateExtrFanAuto(cfg).subscribe(result => {
  //     this.autoExtrFanLoading = false;
  //     if(cfg.extractorAuto){
  //       this.autoExtrFanAuto = true;
  //       this.autoExtrFanText = 'ON';
  //     }else{
  //       this.autoExtrFanAuto = false;
  //       this.autoExtrFanText = 'OFF'
  //     }
  //   },
  //   httpError => {
  //     this.error = httpError.message + ' ' + httpError.error.error;
  //     this.autoExtrFanLoading = false;
  //     this.autoExtrFanAuto = false;
  //     this.autoExtrFanText = "err";
  //   });
    

  // }

  showError(httpError: any): void {
    
    
    console.log("httpError", httpError);
  }
}
