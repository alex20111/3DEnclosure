import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ConfigService } from 'src/app/services/config.service';

@Component({
  selector: 'app-number-keypad-modal',
  templateUrl: './number-keypad-modal.component.html',
  styleUrls: ['./number-keypad-modal.component.css']
})
export class NumberKeypadModalComponent implements OnInit {


  error: string = "";  
   numbersForm!: FormGroup;
  numbers: boolean = false;

  constructor(private formBuilder: FormBuilder, public activeModal: NgbActiveModal, private cfg: ConfigService) { }

  ngOnInit(): void {
    this.numbersForm = this.formBuilder.group({
      frm_numbers: ['', [Validators.required, Validators.minLength(1)]]
    });
  }

  submitForm(): void {

    // this.error = "";

    // const validPass = this.cfg.passwordValid(this.passwordForm.value.frm_password);

    // if (validPass){
    //   this.cfg.authenticated = true;
      this.activeModal.close('ValidPassword');

    // } else {
    //   this.error = 'Password invalid';
    // }


  }

}
