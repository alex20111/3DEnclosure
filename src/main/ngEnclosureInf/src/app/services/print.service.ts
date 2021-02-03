import { GcodeFileList } from './file.service';
import { PrintMessage } from 'src/app/_model/PrintMessage';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Message } from '../_model/Message';
import { Constants } from '../_model/Constants';

@Injectable({
  providedIn: 'root'
})
export class PrintService {


  private subject = new BehaviorSubject<any>(null);

  constructor(private http: HttpClient) { }


  startPrinting(fileNamePrint: string): Observable<Message>{
    let print = new PrintServiceData();
    print.printFile = fileNamePrint;     

    return  this.http.post<Message>(`http://${Constants.HOST_ADDRESS}:8080/web/print/start`, print);
  }

  stopPrinting(): Observable<Message>{
    let print = new PrintServiceData();
    // print.endDate = new Date();
   

    return  this.http.post<Message>(`http://${Constants.HOST_ADDRESS}:8080/web/print/stop`, print);
  }

  printUiInitInfo(): Observable<PrintServiceData>{
    return this.http.get<PrintServiceData>(`http://${Constants.HOST_ADDRESS}:8080/web/print/initScreen`);
  }

  sendPrintMessage(message: PrintMessage){

    this.subject.next(message);
  }

  getPrintMessage(): Observable<any>{
    return this.subject.asObservable();
  }
  resetPrintMessage(): void{
    this.subject.next(null);
  }
}

export class PrintServiceData{
  printFile: string;
  listFiles?: GcodeFileList[] = [];
  printing: boolean = false;

	//time/date display
   printTimeSeconds: number = -1;
   printStarted: Date;
	 bedTemp: number = -1.0;
	 bedTempMax: number = -1.0;
	 nozzleTemp: number = -1.0;
	 nozzleTempMax: number = -1.0;	
	 printerBusy: boolean = false;	
	 lastUpdate: Date = null;

}


