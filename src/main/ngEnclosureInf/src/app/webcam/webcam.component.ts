import { Subscription } from 'rxjs';
import { GeneralService } from './../services/general.service';
import { Component, OnDestroy, OnInit } from '@angular/core';

@Component({
  selector: 'app-webcam',
  templateUrl: './webcam.component.html',
  styleUrls: ['./webcam.component.css']
})
export class WebcamComponent implements OnInit, OnDestroy {

  error: string;
  message: string;

  btnText: string = "Wait..";

  

  initConnection: boolean = false;
  connected: boolean = false;
  webcamConnect: any;
  webcamStatus: any;

  constructor(private generalService: GeneralService) { }
  ngOnDestroy(): void {

    if (this.webcamConnect) {
      clearInterval(this.webcamConnect);
    }
    if (this.webcamStatus) {
      clearInterval(this.webcamStatus);
    }
  }

  ngOnInit(): void {
    this.connectToWebcam("status");

    this.startWebcamCheck();
  }


  webcamToggle() {

    if (this.btnText === "Turn ON") {

      clearInterval(this.webcamStatus);//cear current checkup until we are conntected

      this.connectToWebcam("TurnOn");
      this.initConnection = true;  //refresh until connected.
      this.webcamConnect = setInterval(() => {
        this.connectToWebcam("status");
      }, 2000);

    } else {
      this.initConnection = true;
      this.connectToWebcam("TurnOff");
    }
  }


  connectToWebcam(command: string): void {
    console.log("Command sent: ", command);

    this.generalService.streamWebcam(command).subscribe(result => {
      console.log("Webcam result: ", result);
      if (result.message === 'Webcam cannot be reached' || result.message === 'Webcam stopped') {
        this.btnText = "Turn ON";
      } else if (result.message === 'Webcam connected' || result.message === 'Webcam Started') {
        this.btnText = "Turn OFF";
      } else {
        this.btnText = "Turn ON";
      }

      if (result.message === 'Webcam connected' && !this.connected) {
        this.initConnection = false;
        this.connected = true;
        clearInterval(this.webcamConnect);
        this.startWebcamCheck();
      } else if (result.message === 'Webcam cannot be reached' && this.connected){
        this.connected = false;
        this.initConnection = false;
      }
    },
      err => {
        this.error = err.message + ' ' + err.error.error;
        this.btnText = "Turn ON";
        this.connected = false;
        this.initConnection = false;
      })
  }

  startWebcamCheck() {
    this.webcamStatus = setInterval(() => this.connectToWebcam("status"), 6000);
  }

}
