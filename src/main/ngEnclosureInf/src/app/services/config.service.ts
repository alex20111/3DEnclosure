import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {

  constructor() { }
}

export class Config{
   id: number = -1; 	
	 lightsOn: boolean = false;	  //only on boot.. if starting turn light on. 
	 extractorAuto:  boolean = false;
	 extrPPMLimit: number		= -1;
	 encTempLimit: number		= -1;
	 fireAlarmAuto: boolean 	= false;
	 smsPhoneNumber: string 	= "";
}
