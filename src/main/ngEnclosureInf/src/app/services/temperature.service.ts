import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TemperatureService {

  constructor(private http: HttpClient) { }


  getEnclosureTemperature(): Observable<any>{
      return this.http.get<any>('http://localhost:8080/web/temperature/enclosureTemp');    
  
  }
}
