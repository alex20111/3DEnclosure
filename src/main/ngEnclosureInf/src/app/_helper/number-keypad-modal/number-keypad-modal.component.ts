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

  numberValue: string = '';

  constructor(private formBuilder: FormBuilder, public activeModal: NgbActiveModal, private cfg: ConfigService) { }

  ngOnInit(): void {
    this.numbersForm = this.formBuilder.group({
      frm_numbers: ['', [Validators.required, Validators.minLength(1)]]
    });
  }

  btnEvent($event: any, btn: number){
    $event.preventDefault(); //to not sub,mit the form

    if (btn === 1 ){
      this.numberValue = `${this.numberValue}1`;
    }else if(btn === 2){
      this.numberValue = `${this.numberValue}2`;
    }else if(btn === 3){
      this.numberValue = `${this.numberValue}3`;
    }else if(btn === 4){
      this.numberValue = `${this.numberValue}4`;
    }else if(btn === 5){
      this.numberValue = `${this.numberValue}5`;
    }else if(btn === 6){
      this.numberValue = `${this.numberValue}6`;
    }else if(btn === 7){
      this.numberValue = `${this.numberValue}7`;
    }else if(btn === 8){
      this.numberValue = `${this.numberValue}8`;
    }else if(btn === 9){
      this.numberValue = `${this.numberValue}9`;
    }else if(btn === 0){
      this.numberValue = `${this.numberValue}0`;
    }
    

    this.numbersForm.controls.frm_numbers.setValue(this.numberValue);
  }
  submitForm(): void {

    
      this.activeModal.close(this.numbersForm.value.frm_numbers);


  }
 

}
