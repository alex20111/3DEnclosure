import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SessionService {

  private sharedObject: Map<string, Object> = new Map();

  private subject = new BehaviorSubject<any>(null);



  constructor() { 
    this.subject = new BehaviorSubject<any>(null);
  }


  putSharedObject(key: string, object: Object): void {
    this.sharedObject.set(key, object);
  }

  getSharedObject(key: string): Object {
    return this.sharedObject.get(key) as Object;
  }

  removeSharedObject(key: string): void {
    this.sharedObject.delete(key);
  }

  sendMessage(message: any) {
    this.subject.next(message);
  }

  clearMessages() {
    this.subject.next(null);
  }

  getMessage(): Observable<any> {
    return this.subject.asObservable();
  }


}
