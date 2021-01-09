import { NumberKeypadModalComponent } from './../_helper/number-keypad-modal/number-keypad-modal.component';
import { Component, OnInit } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { faKeyboard } from '@fortawesome/free-solid-svg-icons';
import { Config } from '../services/config.service';

@Component({
  selector: 'app-config',
  templateUrl: './config.component.html',
  styleUrls: ['./config.component.css']
})
export class ConfigComponent implements OnInit {

  error: string = "";
  configForm!: FormGroup;

  config!: Config;

  //icons
  faKeyboard = faKeyboard;

  constructor(private formBuilder: FormBuilder,  private modalService: NgbModal) { }

  ngOnInit(): void {
    this.config = new Config();
    this.configForm = this.formBuilder.group({
      frm_extr_auto: [false, [Validators.required]],
      frm_voc_ppm_max: ['', [ Validators.minLength(1)]],
      frm_temp_max: ['', [ Validators.minLength(1)]],
      frm_sms_number: ['', [Validators.minLength(1)]],
      frm_arduino_serial: ['/dev/ttyUSB0', [Validators.required, Validators.minLength(1)]]
    });
  }


    // open the modal component to Rename the group
    openFormModal(boxNbr: number): void {
      console.log("box number: " , boxNbr)
      const modalRef = this.modalService.open(NumberKeypadModalComponent);
  
      modalRef.result.then((result) => {
        if (result === 'ValidPassword') {
          // this.router.navigate(['/config']);
        }
      }).catch((error) => {
        if (error !== 'Cross click') {
          console.error('password error : ', error);
        }
      });
    }

    submitForm(){
      console.log("submit");
    }
    cancelForm($event: any){
      $event.preventDefault(); //to not sub,mit the form
      console.log("cancel form");
    }

    extrFanAuto(){
      this.config.extractorAuto = !this.config.extractorAuto;

      if(this.config.extractorAuto) {
        this.configForm.controls['frm_voc_ppm_max'].enable();
       } else {
          this.configForm.controls['frm_voc_ppm_max'].disable();
        }

    }

   
}
