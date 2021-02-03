import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Constants } from '../_model/Constants';
import { Message } from '../_model/Message';
import { Config } from './config.service';

export interface FanSpeed {
  speed: number;
}

@Injectable({
  providedIn: 'root'
})
export class FanService {

  private subject = new BehaviorSubject<any>(null);

  constructor(private http: HttpClient) {

   }


  getFanRmp(): Observable<any> {
    return this.http.get<any>(`http://${Constants.HOST_ADDRESS}:8080/web/fancontrol/extrRPM`);
  }

  setFanSpeed(fanSpeed: number): Observable<Message> {

    const tempSpeed: FanSpeed = { speed: fanSpeed };

    return this.http.post<Message>(`http://${Constants.HOST_ADDRESS}:8080/web/fancontrol/extrSpeed`, tempSpeed);
  }

  getExtrFanParam(): Observable<ExtrFanParam> {
    return this.http.get<ExtrFanParam>(`http://${Constants.HOST_ADDRESS}:8080/web/fancontrol/extrFanParam`);

  }

  updateExtrFanAuto(cfg: Config): Observable<Message>{
    return this.http.post<Message>(`http://${Constants.HOST_ADDRESS}:8080/web/fancontrol/updateExtrFanAuto`, cfg);
  }

  sendFanAutoMode(isFanAuto: boolean){
    let exrt: ExtrFanParam = {
      fanSpeed: 0,
      fanIsOnAuto: isFanAuto
    }
    this.subject.next(exrt);
  }

  getIsFanOnAuto(): Observable<any>{
    return this.subject.asObservable();
  }
  resetFanOnAuto(): void{
    this.subject.next(null);
  }

}

export interface ExtrFanParam{
    fanSpeed: number;
    fanIsOnAuto: boolean;
}
