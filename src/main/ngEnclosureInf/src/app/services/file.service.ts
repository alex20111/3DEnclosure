import { Message } from './../_model/Message';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class FileService {

  constructor(private http: HttpClient) { }


  fileUpload(formFile: any): Observable<any>{
    return this.http.post<Message>("http://localhost:8080/web/file/upload",formFile , {
      reportProgress: true,
      observe: 'events'
    });
  }

  fileList(): Observable<GcodeFileList[]>{
    return this.http.get<GcodeFileList[]>("http://localhost:8080/web/file/gcodeList");
  }
}

export interface GcodeFileList{
  fileName: string;
  fileSize?: string;
}
