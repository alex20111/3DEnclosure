import { ConfigService } from 'src/app/services/config.service';
import { NumberKeypadModalComponent } from './../_helper/number-keypad-modal/number-keypad-modal.component';
import {  Component, HostListener, OnInit } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { faArrowDown, faKeyboard, faArrowUp } from '@fortawesome/free-solid-svg-icons';
import { Config } from '../services/config.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-config',
  templateUrl: './config.component.html',
  styleUrls: ['./config.component.css']
})
export class ConfigComponent implements OnInit {

  error: string = "";
  message: string = "";
  configForm!: FormGroup;
  configId: number = -1;
  configLoading: boolean = false;

  //icons
  faKeyboard = faKeyboard;
  faArrowDown = faArrowDown;
  faArrowUp = faArrowUp;

  //btn
  showUpButton: boolean = false;
  position: number = 0;

  constructor(private router: Router, private formBuilder: FormBuilder, private modalService: NgbModal, private configService: ConfigService) { }

  @HostListener('window:scroll', ['$event']) // for window scroll events
  onScroll(event: any) {
    if (event.target.scrollingElement.scrollTop > 10 && !this.showUpButton) {
      this.showUpButton = true;
    } else if (event.target.scrollingElement.scrollTop < 10 && this.showUpButton) {
      this.showUpButton = false;
    }
  }

  ngOnInit(): void {

    this.configLoading = true;

    this.configForm = this.formBuilder.group({
      frm_extr_auto: ['', [Validators.required]],
      frm_voc_ppm_max: ['', [Validators.minLength(1)]],
      frm_temp_max: ['', [Validators.minLength(1)]],
      frm_fire_alarm: [false],
      frm_sms_number: ['', [Validators.minLength(1)]],
      frm_light_on: [false],
      frm_arduino_serial: ['', [Validators.required, Validators.minLength(1)]]
    });

    //load config:
    this.configService.loadConfig().subscribe(cfg => {
      this.configLoading = false;
      console.log("cfg: ", cfg);

      this.configForm.setValue({
        frm_extr_auto: cfg.extractorAuto,
        frm_voc_ppm_max: cfg.extrPPMLimit,
        frm_temp_max: cfg.encTempLimit,
        frm_fire_alarm: cfg.fireAlarmAuto,
        frm_sms_number: cfg.smsPhoneNumber,
        frm_light_on: cfg.lightsOn,
        frm_arduino_serial: cfg.arduinoSerialPort
      });

      this.configId = cfg.id;
      console.log("value: ", this.configForm.value.frm_extr_auto);

    }, httpError => {
      this.configLoading = false;
      this.error = httpError.message + ' ' + httpError.error.error;
    });
  }

  // open the modal component to Rename the group
  openFormModal(boxNbr: number): void {
    console.log("box number: ", boxNbr)
    const modalRef = this.modalService.open(NumberKeypadModalComponent);

    modalRef.result.then((result) => {
      console.log("Result keyboard: ", result);

      if (boxNbr === 1) {
        this.configForm.controls.frm_voc_ppm_max.setValue(result);
      } else if (boxNbr === 2) {
        this.configForm.controls.frm_temp_max.setValue(result);
      } else if (boxNbr === 3) {
        this.configForm.controls.frm_sms_number.setValue(result);
      }

    }).catch((error) => {
      console.log("error modal: ", error)
      if (error !== 'Cross click') {
        console.error('password error : ', error);
      }
    });
  }

  submitForm() {
    console.log("submit");

    if (this.configForm.invalid) {
      console.log("Invalid");
      return;
    }

    const vals = this.configForm.value;

    let cfgPack = new Config();
    cfgPack.id = this.configId;
    cfgPack.arduinoSerialPort = vals.frm_arduino_serial;
    cfgPack.encTempLimit = vals.frm_temp_max
    cfgPack.extrPPMLimit = vals.frm_voc_ppm_max
    cfgPack.extractorAuto = vals.frm_extr_auto
    cfgPack.fireAlarmAuto = vals.frm_fire_alarm
    cfgPack.lightsOn = vals.frm_light_on
    cfgPack.smsPhoneNumber = vals.frm_sms_number

    console.log("Sent: ", cfgPack);
    this.configService.updateConfig(cfgPack).subscribe(rtr => {
      console.log("Got: ", rtr);
      this.message = rtr.message;

      this.position = 0;
      window.scrollTo({
        top: this.position,
        behavior: 'smooth'
      });
      // this.configService.configUpdateMessage = rtr.message;
      // this.router.navigate(['/']);

    }, httpError => {
      this.error = httpError.Message + ' ' + httpError.error.error;
      console.log('Config http error: ', httpError);
    });

  }
  cancelForm($event: any) {
    $event.preventDefault(); //to not sub,mit the form

    this.router.navigate(['/']);
  }

  scrollButton(direction: string) {
    console.log(this.position);

    if (direction === 'up' && this.position >= 20) {
      this.position = this.position - 20;

    } else if (direction === 'down' && this.position < 120) {
      this.position = this.position + 20;
    }

    window.scrollTo({
      top: this.position,
      behavior: 'smooth'
    });
  }
}
