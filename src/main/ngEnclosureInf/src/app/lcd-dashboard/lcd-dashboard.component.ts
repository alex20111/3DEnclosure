import { Message } from './../_model/Message';
import { PrintService } from './../services/print.service';
import { SessionService } from './../services/session.service';
import { GeneralService, DashBoard } from './../services/general.service';
import { Component, OnDestroy, OnInit, ɵConsole } from '@angular/core';
import { Subscription, timer } from 'rxjs';
import { faThermometerHalf, faLightbulb, faFan, faTachometerAlt, faCog, faUnderline } from '@fortawesome/free-solid-svg-icons';
import { LightService } from '../services/light.service';
import { PrintMessage } from '../_model/PrintMessage';
import { Constants } from '../_model/Constants';


@Component({
  selector: 'app-lcd-dashboard',
  templateUrl: './lcd-dashboard.component.html',
  styleUrls: ['./lcd-dashboard.component.css']
})
export class LcdDashboardComponent implements OnInit, OnDestroy {

  dashBoard!: DashBoard;

  lightColor: string = 'red';
  lightText: string = 'OFF';
  lightLoading: boolean = false;

  //alerts
  error: string = '';
  message: string = '';
  printMessage: Date | undefined;

  dashBoardTimer: Subscription | undefined;
  printMsg!: Subscription;

  //icons
  faThermometerHalf = faThermometerHalf;
  faLightbulb = faLightbulb;
  faFan = faFan;
  faTachometerAlt = faTachometerAlt;
  faCog = faCog;

  constructor(
    private generalService: GeneralService, private lightService: LightService, private printService: PrintService) { }


  ngOnInit(): void {

    this.printMsg = this.printService.getPrintMessage().subscribe(msg => {
      if (msg != null) {
        if (msg.date != null && msg.started) {
          // console.log("one: ", msg.date);
          this.printMessage = msg.date;
        } else if (msg.finished) {          
          this.printMessage = undefined;
          this.printService.stopPrinting().subscribe(stopped => {
            this.message = "print finished, Fan will stop in 5 min";
          },
          err => {
            this.error = err.error.error;
          })
        }else if (msg.stoped) {
          this.message = "";
          this.printMessage = undefined;
        }
      }

    });

    this.dashBoardTimer = timer(100, 6000).subscribe(val => {
      this.generalService.dashBoard().subscribe(dshboard => {
        this.error = '';
        // console.log("get: " , new Date());
        this.dashBoard = dshboard;

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

  light() {
    this.dashBoard.lightOn = !this.dashBoard.lightOn;
    if (this.dashBoard.lightOn) {
      this.lightText = 'ON';
    } else {
      this.lightText = 'OFF';
    }

    this.lightLoading = true;
    this.lightService.switchLightState(this.dashBoard.lightOn).subscribe(
      result => {
        if (result.message === 'true') {
          this.lightText = 'ON';
          this.dashBoard.lightOn = true;
          this.lightColor = 'rgb(12, 247, 12)';
        } else {
          this.lightText = 'OFF';
          this.dashBoard.lightOn = false;
          this.lightColor = 'red';
        }
        this.lightLoading = false;
      },
      err => {
        this.error = err.message + ' ' + err.error.error;
        this.lightText = 'OFF';
        this.dashBoard.lightOn = false;
        this.lightLoading = false;
      }
    );


  }

  ngOnDestroy(): void {
    this.dashBoardTimer?.unsubscribe();
    this.printMsg.unsubscribe();
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
      });
  }
}
