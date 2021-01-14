import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SessionService {

  private sharedObject: Map<string, Object> = new Map();

  constructor() { }


  putSharedObject(key: string, object: Object): void {
    this.sharedObject.set(key, object);
  }

  getSharedObject(key: string): Object {
    return this.sharedObject.get(key) as Object;
  }

  removeSharedObject(key: string): void {
    this.sharedObject.delete(key);
  }


}
