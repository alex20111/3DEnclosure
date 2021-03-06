import { Message } from './../_model/Message';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Constants } from '../_model/Constants';

@Injectable({
  providedIn: 'root'
})
export class FileService {

  constructor(private http: HttpClient) { }


  fileUpload(formFile: any): Observable<any>{
    return this.http.post<Message>(`http://${Constants.HOST_ADDRESS}:8080/web/file/upload`,formFile , {
      reportProgress: true,
      observe: 'events'
    });
  }

  fileList(): Observable<GcodeFile[]>{
    return this.http.get<GcodeFile[]>(`http://${Constants.HOST_ADDRESS}:8080/web/file/gcodeList`);
  }
}

export interface GcodeFile{
  fileName: string;
  fileSize?: string;
  fileFromPi: boolean;
  fileFromSd: boolean;
}
