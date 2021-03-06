import { GcodeFile } from './../services/file.service';
import { PrintService, PrintServiceData } from './../services/print.service';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NgbTimeStruct } from '@ng-bootstrap/ng-bootstrap';
import { PrintMessage } from '../_model/PrintMessage';
import { faUpload } from '@fortawesome/free-solid-svg-icons';
import { FormBuilder, FormGroup } from '@angular/forms';


@Component({
  selector: 'app-printing',
  templateUrl: './printing.component.html',
  styleUrls: ['./printing.component.css']
})
export class PrintingComponent implements OnInit {

  printForm!: FormGroup;

  error: string = "";
  time: NgbTimeStruct = { hour: 0, minute: 0, second: 0 };

  printUiData?: PrintServiceData;
  loading = false;
  stoppingLoading = false;
  pausePrintLoading: boolean = false;
  printPauseBtnTxt: string = "Pause";

  faUpload = faUpload;

  constructor(
    private router: Router, private printService: PrintService,
    private formBuilder: FormBuilder) { }

  ngOnInit(): void {

    //build form
    this.printForm = this.formBuilder.group({
      frm_file_to_print: [''],
      frm_auto_power_off: [false]
    });

    this.loading = true;
    this.printService.printUiInitInfo().subscribe(data => {
      console.log("Print data: ", data);

      this.printUiData = data;
      if (data.listFiles && data.listFiles.length > 0) {
        this.printForm.setValue({
          frm_file_to_print: this.printUiData.listFiles[0].fileName,
          frm_auto_power_off: this.printUiData.autoPrinterShutdown
        });
      }

      this.loading = false;

      if (this.printUiData.printPaused){
        this.printPauseBtnTxt = "Resume";
      }

    },
      err => {
        console.log("errororo: ", err);;
        this.error = err.Message + ' ' + err.error;
        this.loading = false;
      });
  }

  start() {

    if (this.printUiData) {

      const selectedFileName = this.printForm.value.frm_file_to_print;

      if (selectedFileName) {

        const fileToPrint: GcodeFile = this.printUiData.listFiles.filter(x => x.fileName === selectedFileName)[0];

        let print = new PrintServiceData();
        print.printFile = fileToPrint;
        print.autoPrinterShutdown = this.printForm.value.frm_auto_power_off;

        if (fileToPrint) {

          // console.log("SENT TO PRINTTTTTT " , print); 

          this.loading = true;
          this.printService.startPrinting(print).subscribe(result => {
            this.loading = false;
            if (result.messageType !== "SUCCESS") {
              this.error = result.message;
            } else {
              this.printUiData.printing = true;
              this.router.navigate(['/']);
            }

          },
            err => {
              console.log(err);
              this.error = err.message + ' ' + err.error;
              this.loading = false;
            })
        } else {
          this.error = "Please select a file to print";
        }
      }else{
        this.error = "Please upload a file ";
      }
    }
  }

  stop() {
    let printInfo = new PrintMessage();
    printInfo.stoped = true;

    this.stoppingLoading = true;

    this.printService.stopPrinting().subscribe(print => {
      console.log("Stop printing message: ", print);
      // this.printService.sendPrintMessage(printInfo);
      this.router.navigate(['/']);
      this.stoppingLoading = false;
    }, err => {
      this.error = err.message + ' ' + err.error.error;
      this.stoppingLoading = false;
    });

  }

  pausePrint() {
    this.pausePrintLoading = true;

    let pauseAction = "pause";
    if (this.printUiData.printPaused) {
      pauseAction = "resume";
    }
    this.printService.pausePrint(pauseAction).subscribe(result => {
      this.pausePrintLoading = false;
      if (pauseAction === "pause") {
        this.printUiData.printPaused = true;
        this.printPauseBtnTxt = "Resume";
      } else {
        this.printUiData.printPaused = false;
        this.printPauseBtnTxt = "Pause";
      }
    },
      err => {
        this.pausePrintLoading = false;
        this.error = err.message + ' ' + err.error.error;
      });
  }



}
