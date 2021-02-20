import { SessionService } from './../services/session.service';
import { PiWebSocketService, SocketMessage } from './../services/pi-web-socket.service';
import { PrintService, PrintServiceData } from './../services/print.service';
import { GeneralService } from './../services/general.service';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { interval, Subscription } from 'rxjs';
import { faThermometerHalf, faLightbulb, faFan, faTachometerAlt, faCog } from '@fortawesome/free-solid-svg-icons';
import { LightService } from '../services/light.service';
import { Constants } from '../_model/Constants';

@Component({
  selector: 'app-lcd-dashboard',
  templateUrl: './lcd-dashboard.component.html',
  styleUrls: ['./lcd-dashboard.component.css']
})
export class LcdDashboardComponent implements OnInit, OnDestroy {
  lightColor: string = 'red';
  lightText: string = 'OFF';
  lightLoading: boolean = false;

  printerImg: string = "assets/3d-printer-red.png";
  printerOnOffLoading: boolean = false;
  coolingDelay: boolean = false;
  coolingDelayLoading: boolean = false;
  countdownToDate: Date;

  //alerts
  error: string = '';
  message: string = '';
  printMessage: string;
  printData: PrintServiceData = new PrintServiceData();

  // dashBoardTimer: Subscription | undefined;
  printerSubscription: Subscription;

  //icons
  faThermometerHalf = faThermometerHalf;
  faLightbulb = faLightbulb;
  faFan = faFan;
  faTachometerAlt = faTachometerAlt;
  faCog = faCog;

  constructor(
    private generalService: GeneralService, private lightService: LightService, private printService: PrintService,
    private wsSocket: PiWebSocketService, private session: SessionService) { }

  ngOnInit(): void {

    this.countdownToDate = this.session.getSharedObject(Constants.PRINTER_COOLDOWN_TIMER) as Date;

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
    this.printData.lightOn = !this.printData.lightOn;
    if (this.printData.lightOn) {
      this.lightText = 'ON';
    } else {
      this.lightText = 'OFF';
    }

    this.lightLoading = true;
    this.lightService.switchLightState(this.printData.lightOn).subscribe(
      result => {
        if (result.message === 'true') {
          this.lightText = 'ON';
          this.printData.lightOn = true;
          this.lightColor = 'rgb(12, 247, 12)';
        } else {
          this.lightText = 'OFF';
          this.printData.lightOn = false;
          this.lightColor = 'red';
        }
        this.lightLoading = false;
      },
      err => {
        this.error = err.message + ' ' + err.error.error;
        this.lightText = 'OFF';
        this.printData.lightOn = false;
        this.lightLoading = false;
      }
    );
  }

  ngOnDestroy(): void {
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

  connectPrinter() {

    if (!this.printerOnOffLoading) {

      let actionRequest = true;
      let action = "turnOn";
    

      if (this.printData.printerConnected) {
        if (confirm("Turn off printer? ")) {
          action = "turnOff";
          this.printerOnOffLoading = true;
        } else {
          actionRequest = false;
        }
      }else{
        this.printerOnOffLoading = true;
      }

      console.log("action request", actionRequest);
      if (actionRequest) {
        this.coolingDelay = false;

        this.printService.printerOnOff(action).subscribe(result => {
          this.printerOnOffLoading = false;

          console.log("Printer power relay", result);

          if (result.messageType === "SUCCESS") {
            if (result.message === 'on') {
              this.printerImg = 'assets/3d-printer-green.png'
            } else if (result.message === "offwithdelay") {

              this.handlePrinterCooldown();
              console.log("cooling delay: ", this.countdownToDate);
              this.coolingDelay = true;
            } else {
              this.printerImg = 'assets/3d-printer-red.png';
            }
          } else if (result.messageType === "WARN") {
            this.error = result.message;
            this.printerImg = 'assets/3d-printer-red.png';
          }
        },
          err => {
            this.printerOnOffLoading = false;
            this.error = err.message;
          });
      }
    }
  }

  cancelShutdown() {
    this.coolingDelayLoading = true;
    this.printService.stopPrinterShutDown().subscribe(result => {
      console.log("Cooling result", result);

      if (result.messageType === "SUCCESS") {
        this.printMessage = "";
        this.coolingDelay = false;
        this.coolingDelayLoading = false;
        this.session.removeSharedObject(Constants.PRINTER_COOLDOWN_TIMER);
      }
    },
      err => {
        this.error = err.message;
      });
  }

  //handle websocket data
  webSocketData(message: SocketMessage) {
    if (message.dataType === "PRINT_DATA"
      || message.dataType === "PRINT_TOTAL_TIME"
      || message.dataType === "PRINT_DONE") {
      let data: PrintServiceData = JSON.parse(message.message);
      console.log("Print data: ", data);
      this.printData = data;

      if (data.printing) {
        if (!this.printerSubscription) { //timer to display print time            
          console.log("datadatadata: ", data);
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

            if (totPrintTime) { //if we got a time to complete in seconds
              this.printMessage = `${hours} h ${minutes} m ${seconds} s / ${totPrintTime}`;
            } else {
              this.printMessage = `${hours} h ${minutes} m ${seconds} s`;
            }
          });
        }
      } else if (data.printCompleted) { //print complete
        if (data.printerShutdownInProgress) {
          this.printMessage = "Print Completed. Printer cooling down and turning off in 5 min. ";
          this.handlePrinterCooldown();
          this.coolingDelay = true;

        } else {
          this.printMessage = "Print Completed. ";
        }
        if (this.printerSubscription) {
          this.printerSubscription.unsubscribe();
        }
      } else if (data.printerAborded) {
        this.printMessage = "Print Aborded. ";
        if (this.printerSubscription) {
          this.printerSubscription.unsubscribe();
        }
      } else if (data.printerShutdownInProgress) {
        this.printMessage = "Printer too hot, cooling down and turning off in 5 min. ";
        this.handlePrinterCooldown();

        this.coolingDelay = true;
      } else if (!data.printerShutdownInProgress) {
        this.printMessage = "";
        this.coolingDelay = false;
        this.countdownToDate = null;
        this.session.removeSharedObject(Constants.PRINTER_COOLDOWN_TIMER);
      }


      if (this.printData.lightOn) {
        this.lightColor = 'rgb(12, 247, 12)'
      } else {
        this.lightColor = 'red';
      }

      if (this.printData.printerConnected) {
        this.printerImg = 'assets/3d-printer-green.png'
      } else {
        this.printerImg = 'assets/3d-printer-red.png';
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

  private handlePrinterCooldown(){
    if (!this.countdownToDate) {
      this.countdownToDate = this.generalService.getModifiedDate(new Date(), "minute", 5);
      this.session.putSharedObject(Constants.PRINTER_COOLDOWN_TIMER,this.countdownToDate );
    }
  }
}
