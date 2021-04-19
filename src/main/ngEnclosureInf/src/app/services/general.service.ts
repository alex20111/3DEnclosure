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


  shutdownSystem(shuttingDown: string): Observable<Message> {

    return this.http.post<Message>(`http://${Constants.HOST_ADDRESS}:8080/web/general/shutdown`, shuttingDown);

  }

  // dashBoard(): Observable<DashBoard >{

  //   return this.http.get<DashBoard>(`http://${Constants.HOST_ADDRESS}:8080/web/general/dashboard`);


  // }

  getModifiedDate(date: Date, interval: string, units: number): Date {
    if (!(date instanceof Date))
      return undefined;
    var ret = new Date(date); //don't change original date
    var checkRollover = function () { if (ret.getDate() != date.getDate()) ret.setDate(0); };
    switch (String(interval).toLowerCase()) {
      case 'year': ret.setFullYear(ret.getFullYear() + units); checkRollover(); break;
      case 'quarter': ret.setMonth(ret.getMonth() + 3 * units); checkRollover(); break;
      case 'month': ret.setMonth(ret.getMonth() + units); checkRollover(); break;
      case 'week': ret.setDate(ret.getDate() + 7 * units); break;
      case 'day': ret.setDate(ret.getDate() + units); break;
      case 'hour': ret.setTime(ret.getTime() + units * 3600000); break;
      case 'minute': ret.setTime(ret.getTime() + units * 60000); break;
      case 'second': ret.setTime(ret.getTime() + units * 1000); break;
      default: ret = undefined; break;
    }
    return ret;
  }

}

// export interface DashBoard{
//     extrFanOnAuto: boolean;
//     extracFanRPM : number;
// 	  extracFanSpeed : number;
// 	  temperature : string;
// 	  lightOn : boolean;
//     airQualityCo2 : string;
//     airQualityVoc : string;
// }
