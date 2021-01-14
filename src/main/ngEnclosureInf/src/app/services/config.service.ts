
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Message } from '../_model/Message';


@Injectable({
  providedIn: 'root'
})
export class ConfigService {

  constructor(private http: HttpClient) { }

	loadConfig(): Observable<Config>{
		return this.http.get<Config>('http://localhost:8080/web/config/configData');
	}

	updateConfig(cfg: Config): Observable<Message>{
		return this.http.post<Message>('http://localhost:8080/web/config/updateConfig', cfg);
	}

}

export class Config{
   	id: number = -1; 	
	 lightsOn: boolean = false;	  //only on boot.. if starting turn light on. 
	 extractorAuto:  boolean = false;
	 extrPPMLimit: number		= -1;
	 encTempLimit: number		= -1;
	 fireAlarmAuto: boolean 	= false;
	 smsPhoneNumber: string 	= "";
	 arduinoSerialPort: string = '';
}
