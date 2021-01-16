import { SessionService } from './../../services/session.service';
import { FanService } from './../../services/fan.service';
import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { faQuestion } from '@fortawesome/free-solid-svg-icons';
import { Config } from 'src/app/services/config.service';
import { Subscription } from 'rxjs';
import { faLess } from '@fortawesome/free-brands-svg-icons';

@Component({
  selector: 'app-extr-auto-button',
  templateUrl: './extr-auto-button.component.html',
  styleUrls: ['./extr-auto-button.component.css']
})
export class ExtrAutoButtonComponent implements OnInit, OnDestroy {


  @Input() loadOnInit: boolean = false;

  autoExtrFanAuto: boolean = false;
  autoExtrFanText: string = 'OFF';
  autoExtrFanLoading: boolean = false;

  error: string = "";

  faQuestion = faQuestion;

  msg!: Subscription;

  constructor(private fanService: FanService, private session: SessionService) { }
  ngOnDestroy(): void {
    this.msg?.unsubscribe();
  }

  ngOnInit(): void {

    if (this.loadOnInit) {
      this.fanService.getExtrFanParam().subscribe(param => {

        if (param.fanIsOnAuto) {
          this.autoExtrFanAuto = true;
          this.autoExtrFanText = "ON";
        } else {
          this.autoExtrFanAuto = false;
          this.autoExtrFanText = "OFF";
        }
      },
        err => {
          this.error = err.message + ' ' + err.error.error;
        }
      );
    }

    if (!this.loadOnInit) { //

      this.msg = this.fanService.getIsFanOnAuto().subscribe(exrFan => {
        console.log("extractor: ", exrFan);

        if (exrFan != null) {
          if (exrFan.fanIsOnAuto ) {
            this.autoExtrFanAuto = true;
            this.autoExtrFanText = "ON";
          } else {
            this.autoExtrFanAuto = false;
            this.autoExtrFanText = "OFF";
          }
          this.fanService.resetFanOnAuto();
        }
      });
      // // this.msgSubs = this.session.getMessage().subscribe(fanAuto => {
      // console.log("extractor: " , this.fanOnAuto);


      //     if ( this.fanOnAuto === "true") {
      //       this.autoExtrFanAuto = true;
      //       this.autoExtrFanText = "ON";
      //     } else {
      //       this.autoExtrFanAuto = false;
      //       this.autoExtrFanText = "OFF";
      //     }

      // //     this.msgSubs.unsubscribe();

      // // });
    }
  }

  autoFanBtn() {
    this.autoExtrFanLoading = true;
    this.autoExtrFanText = "wait";
    // this.autoExtrFan = !this.autoExtrFan;  

    let cfg = new Config();
    cfg.extractorAuto = !this.autoExtrFanAuto;
    this.fanService.updateExtrFanAuto(cfg).subscribe(result => {
      this.autoExtrFanLoading = false;
      if (cfg.extractorAuto) {
        this.autoExtrFanAuto = true;
        this.autoExtrFanText = 'ON';
      } else {
        this.autoExtrFanAuto = false;
        this.autoExtrFanText = 'OFF'
      }
    },
      httpError => {
        this.error = httpError.message + ' ' + httpError.error.error;
        this.autoExtrFanLoading = false;
        this.autoExtrFanAuto = false;
        this.autoExtrFanText = "err";
        this.session.sendMessage(this.error);
      });
  }



}
