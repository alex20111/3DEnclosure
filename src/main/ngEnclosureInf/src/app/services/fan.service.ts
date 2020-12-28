import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Message } from '../_model/Message';

export interface FanSpeed{
  speed: number;
}

@Injectable({
  providedIn: 'root'
})
export class FanService {

  constructor(private http: HttpClient) { }


  getSomething(): Observable<any>{
   return this.http.get<any>('http://localhost:8080/web/temperature/enclosureTemp');

   
  }
  getFanRmp(): Observable<any>{
    return this.http.get<any>('http://localhost:8080/web/fancontrol/extrRPM');    
   }

   setFanSpeed(fanSpeed: number): Observable<Message>{

     const tempSpeed: FanSpeed = { speed: fanSpeed };

    return this.http.post<Message>('http://localhost:8080/web/fancontrol/extrSpeed', tempSpeed);
   }

   getFanSpeed(): Observable<any>{
   return this.http.get<any>('http://localhost:8080/web/fancontrol/getExtrFanSpeed');
   
   }
}
