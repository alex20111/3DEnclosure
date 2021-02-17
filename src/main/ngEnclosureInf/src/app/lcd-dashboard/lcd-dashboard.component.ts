import { PiWebSocketService, SocketMessage } from './../services/pi-web-socket.service';
import { PrintService, PrintServiceData } from './../services/print.service';
import { GeneralService, DashBoard } from './../services/general.service';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { interval, Subscription, timer } from 'rxjs';
import { faThermometerHalf, faLightbulb, faFan, faTachometerAlt, faCog } from '@fortawesome/free-solid-svg-icons';
import { LightService } from '../services/light.service';


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
  printMessage: string;
  printData: PrintServiceData = new PrintServiceData();

  dashBoardTimer: Subscription | undefined;
  printerSubscription: Subscription;

  //icons
  faThermometerHalf = faThermometerHalf;
  faLightbulb = faLightbulb;
  faFan = faFan;
  faTachometerAlt = faTachometerAlt;
  faCog = faCog;

  //timer
  printLength: number;

  constructor(
    private generalService: GeneralService, private lightService: LightService, private printService: PrintService,
    private wsSocket: PiWebSocketService) { }

  ngOnInit(): void {

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
    this.wsSocket.connect().subscribe(wsReturn => {
      // console.log("WebSocket result", wsReturn);

      this.webSocketData(wsReturn);
    },
      err => {
        console.log("Web Socket error!!!", err);
      });

    //request new print data
    const socketMessage = new SocketMessage();
    socketMessage.action = "REQUEST_DATA";  //request  
    socketMessage.dataType = "PRINT_DATA";  //request print data if any
    this.wsSocket.sendMessage(socketMessage);
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
    this.wsSocket.closeSocket();
    if (this.printerSubscription) {
      this.printerSubscription.unsubscribe();
    }
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
        this.error = httpError.message + ' ' + httpError.error.st;
      });
  }
  //handle websocket data
  webSocketData(message: SocketMessage) {
    if (message.dataType === "PRINT_DATA" 
          || message.dataType === "PRINT_TOTAL_TIME"
          || message.dataType === "PRINT_DONE") {
      let data: PrintServiceData = JSON.parse(message.message);
      this.printData = data;
      if (data.printing) {
        if (!this.printerSubscription) { //timer to display print time            
          console.log("datadatadata: " , data);
          let printStartedDate = new Date(data.printStarted);

          let totPrintTime = undefined;
          if (data.printTimeSeconds > 0) {
            totPrintTime = this.getTotalTime(data.printTimeSeconds);
          }
          this.printerSubscription = interval(1000).subscribe(() => {
            let totalSeconds = Math.floor(
              (new Date().getTime() - printStartedDate.getTime()) / 1000
            );

            let hours = 0;
            let minutes = 0;
            let seconds = 0;

            if (totalSeconds >= 3600) {
              hours = Math.floor(totalSeconds / 3600);
              totalSeconds -= 3600 * hours;
            }

            if (totalSeconds >= 60) {
              minutes = Math.floor(totalSeconds / 60);
              totalSeconds -= 60 * minutes;
            }
            seconds = totalSeconds;

            if (totPrintTime){ //if we got a time to complete in seconds
             this.printMessage = `${hours} h ${minutes} m ${seconds} s / ${totPrintTime}`;
            }else{
              this.printMessage = `${hours} h ${minutes} m ${seconds} s` ;
            }
          });
        }
      }else if (message.dataType === "PRINT_DONE") {
        this.printMessage = "Print Completed. ";
        this.printData = undefined;
        if (this.printerSubscription) {
          this.printerSubscription.unsubscribe();
        }
      }
    } 
  }

  private getTotalTime(sec: number): string {
    let hours = 0;
    let minutes = 0;
    let seconds = 0;

    if (sec >= 3600) {
      hours = Math.floor(sec / 3600);
      sec -= 3600 * hours;
    }

    if (sec >= 60) {
      minutes = Math.floor(sec / 60);
      sec -= 60 * minutes;
    }
    seconds = sec;

    return `${hours} h ${minutes} m ${seconds} s`;
  }
}
