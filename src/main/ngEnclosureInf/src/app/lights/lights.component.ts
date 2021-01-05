import { LightService } from './../services/light.service';
import { Component, OnInit } from '@angular/core';
import { faLightbulb } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-lights',
  templateUrl: './lights.component.html',
  styleUrls: ['./lights.component.css']
})
export class LightsComponent implements OnInit {

  lightOn: boolean = false;
  lightTest: string = "OFF";
  lightLoading: boolean = false;
  lightColor: string = 'red';

  error: string = "";
  message: string = "";

  faLightbulb = faLightbulb;

  constructor(private lightService: LightService) { }

  ngOnInit(): void {
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
          this.lightColor = 'rgb(12, 247, 12)';
        } else {
          this.lightTest = 'OFF';
          this.lightOn = false;
          this.lightColor = 'red';
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

}
