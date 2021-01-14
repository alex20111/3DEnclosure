import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Message } from '../_model/Message';
import { Config } from './config.service';

export interface FanSpeed {
  speed: number;
}

@Injectable({
  providedIn: 'root'
})
export class FanService {

  constructor(private http: HttpClient) { }


  getFanRmp(): Observable<any> {
    return this.http.get<any>('http://localhost:8080/web/fancontrol/extrRPM');
  }

  setFanSpeed(fanSpeed: number): Observable<Message> {

    const tempSpeed: FanSpeed = { speed: fanSpeed };

    return this.http.post<Message>('http://localhost:8080/web/fancontrol/extrSpeed', tempSpeed);
  }

  getExtrFanParam(): Observable<ExtrFanParam> {
    return this.http.get<ExtrFanParam>('http://localhost:8080/web/fancontrol/extrFanParam');

  }

  updateExtrFanAuto(cfg: Config): Observable<Message>{
    return this.http.post<Message>('http://localhost:8080/web/fancontrol/updateExtrFanAuto', cfg);
  }


}

export interface ExtrFanParam{
    fanSpeed: number;
    fanIsOnAuto: boolean;
}
