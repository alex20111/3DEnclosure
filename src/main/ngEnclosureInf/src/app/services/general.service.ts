import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Message } from '../_model/Message';

@Injectable({
  providedIn: 'root'
})
export class GeneralService {

  constructor(private http: HttpClient) { }


  shutdownSystem(): Observable<Message>{

   return this.http.get<Message>('http://localhost:8080/web/general/shutdown');

  }

  dashBoard(): Observable<DashBoard | Message>{

    return this.http.get<DashBoard | Message>('http://localhost:8080/web/general/dashboard');
 

  }
}

export interface DashBoard{
    extracFanRPM : number;
	  extracFanSpeed : number;
	  temperature : string;
	  lightOn : boolean;
    airQualityCo2 : string;
    airQualityVoc : string;
}
