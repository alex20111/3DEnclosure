import { NumberKeypadModalComponent } from './../_helper/number-keypad-modal/number-keypad-modal.component';
import { Component, OnInit } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { faKeyboard } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-config',
  templateUrl: './config.component.html',
  styleUrls: ['./config.component.css']
})
export class ConfigComponent implements OnInit {

  error: string = "";
  configForm!: FormGroup;

  //icons
  faKeyboard = faKeyboard;

  constructor(private formBuilder: FormBuilder,  private modalService: NgbModal) { }

  ngOnInit(): void {
    this.configForm = this.formBuilder.group({
      frm_refresh: ['', [Validators.required, Validators.minLength(1)]],
      frm_refresh1: ['', [Validators.required, Validators.minLength(1)]],
      frm_refresh2: ['', [Validators.required, Validators.minLength(1)]]
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

    }

}
