import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Constants } from '../_model/Constants';

@Injectable({
  providedIn: 'root'
})
export class TemperatureService {

  constructor(private http: HttpClient) { }


  getEnclosureTemperature(): Observable<any>{
      return this.http.get<any>(`http://${Constants.HOST_ADDRESS}:8080/web/temperature/enclosureTemp`);    
  
  }
}
