import { Message } from './../_model/Message';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Constants } from '../_model/Constants';

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

    return this.http.post<Message>(`http://${Constants.HOST_ADDRESS}:8080/web/light/lightControl`, status);
  }

}
