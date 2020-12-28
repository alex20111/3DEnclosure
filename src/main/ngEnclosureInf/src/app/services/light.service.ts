import { Message } from './../_model/Message';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface LightStatus{
  status: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class LightService {

  constructor(private http: HttpClient) { }


  switchLightState(lightOn: boolean): Observable<Message>{
    const status: LightStatus = {
      status: lightOn
    }

    return this.http.post<Message>('http://localhost:8080/web/light/lightControl', status);
  }

}
