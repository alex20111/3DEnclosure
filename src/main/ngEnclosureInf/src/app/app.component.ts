import { GeneralService } from './services/general.service';
import { Component } from '@angular/core';
import { faCog, faFan, faHome, faPowerOff, faPrint, faTerminal } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'ngEnclosureInf';
  message: string = "";
  error: string = "";

  faHome = faHome;
  faPrint = faPrint;
  faFan = faFan;
  faCog = faCog;
  faTerminal = faTerminal;
  faPowerOff = faPowerOff;

  btnSelected: number = 1;

  shuttingDown: boolean = false;

  constructor(private generalService: GeneralService) { }

  btnClicked(btnNbr: number) {
    this.btnSelected = btnNbr;
    if (btnNbr === 1) {
      //do something if needed.
    }
  }

  //shutdown the system.
  handleShutDown(cmd: string) {
    //Shutting down: cmd = "ShuttingDown"
    //Override shutting down: cmd = "CancelShutDown"


    this.generalService.shutdownSystem(cmd).subscribe(success => {
      this.message = success.message;
      //Interrupted
      if ("ShuttingDown" === cmd){
        this.shuttingDown = true;
      }else if ("CancelShutDown" === cmd){
        this.message = "ShutDown Cancelled";
        this.shuttingDown = false;
      }
     
    },
      httpError => {
        this.error = httpError.message + ' ' + httpError.error.st;
        this.shuttingDown = false;;
      });
  }

}
