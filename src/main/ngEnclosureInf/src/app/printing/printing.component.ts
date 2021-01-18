import { PrintService } from './../services/print.service';
import { SessionService } from './../services/session.service';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NgbTimeStruct } from '@ng-bootstrap/ng-bootstrap';
import { PrintMessage } from '../_model/PrintMessage';
import { Constants } from '../_model/Constants';

@Component({
  selector: 'app-printing',
  templateUrl: './printing.component.html',
  styleUrls: ['./printing.component.css']
})
export class PrintingComponent implements OnInit {

  error: string = "";
  time: NgbTimeStruct = { hour: 0, minute: 0, second: 0 };

  constructor(private session: SessionService, private router: Router, private printService: PrintService) { }

  ngOnInit(): void {

    let printObj = this.session.getSharedObject(Constants.PRINTING) as PrintMessage;

    if (printObj != null) {
      this.time.hour = printObj.hour;
      this.time.minute = printObj.minute;
      this.time.second = printObj.seconds;
    }

  }

  start() {

    let dateNow = new Date().getTime();


    if (this.time.hour === 0 && this.time.minute === 0 && this.time.second === 0) {
      //please enter a value
      this.error = "Please enter a time to print";
      return;
    }

    let newDate = (this.time.hour * 60 * 60 * 1000) + (this.time.minute * 60 * 1000) + (this.time.second * 1000);

    const myDate = new Date(dateNow + newDate);

    let printInfo = new PrintMessage();
    printInfo.date = myDate;
    printInfo.started = true;
    printInfo.hour = this.time.hour;
    printInfo.minute = this.time.minute;
    printInfo.seconds = this.time.second;

    // console.log("my date: ", myDate);

    this.printService.startPrinting(myDate).subscribe(result => {
      this.printService.sendPrintMessage(printInfo);
      this.session.putSharedObject(Constants.PRINTING, printInfo);

      this.router.navigate(['/']);
    }, err => {
      this.error = err.message + ' ' + err.error.error;
    });


  }

  stop() {
    let printInfo = new PrintMessage();
    printInfo.stoped = true;

    this.printService.stopPrinting().subscribe(print => {
      console.log("Stop printing message: " , print);
      this.printService.sendPrintMessage(printInfo);
      this.router.navigate(['/']);
    }, err => {
      this.error = err.message + ' ' + err.error.error;
    });

  }



}
