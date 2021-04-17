import { Component } from '@angular/core';
import { faCog, faFan, faHome, faPowerOff, faPrint, faTerminal } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'ngEnclosureInf';

  faHome = faHome;
  faPrint = faPrint;
  faFan = faFan;
  faCog = faCog;
  faTerminal = faTerminal;
  faPowerOff= faPowerOff;
}
