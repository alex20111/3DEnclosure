import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Constants } from '../_model/Constants';
import { Message } from '../_model/Message';

@Injectable({
  providedIn: 'root'
})
export class GeneralService {

  constructor(private http: HttpClient) { }


  shutdownSystem(): Observable<Message>{

   return this.http.get<Message>(`http://${Constants.HOST_ADDRESS}:8080/web/general/shutdown`);

  }

  dashBoard(): Observable<DashBoard >{

    return this.http.get<DashBoard>(`http://${Constants.HOST_ADDRESS}:8080/web/general/dashboard`);
 

  }
}

export interface DashBoard{
    extrFanOnAuto: boolean;
    extracFanRPM : number;
	  extracFanSpeed : number;
	  temperature : string;
	  lightOn : boolean;
    airQualityCo2 : string;
    airQualityVoc : string;
}
