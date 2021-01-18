import { PrintMessage } from 'src/app/_model/PrintMessage';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Message } from '../_model/Message';

@Injectable({
  providedIn: 'root'
})
export class PrintService {


  private subject = new BehaviorSubject<any>(null);

  constructor(private http: HttpClient) { }


  startPrinting(printFinished: Date): Observable<Message>{
    let print = new PrintInfo();
    print.endDate = printFinished;
    print.started = true;

    return  this.http.post<Message>('http://localhost:8080/web/print/start', print);
  }

  stopPrinting(): Observable<Message>{
    let print = new PrintInfo();
    print.endDate = new Date();
    print.started = false;

    return  this.http.post<Message>('http://localhost:8080/web/print/stop', print);
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

export class PrintInfo{
  endDate!: Date;
  started: boolean = false;
}
