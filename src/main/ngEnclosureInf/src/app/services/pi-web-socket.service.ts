import { Injectable } from '@angular/core';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PiWebSocketService {

  myWebSocket: WebSocketSubject<SocketMessage>;
  
  constructor() { 
    this.myWebSocket = webSocket('ws://localhost:8080/printerEvents/');
  }


  connect(): Observable<any> {    

    const socketMessage = new SocketMessage();
    socketMessage.action = "REGISTER";

    this.sendMessage(socketMessage);
    return this.myWebSocket.asObservable();
  }

  sendMessage(message: SocketMessage): void {
    console.log('Sending: ', message);
    this.myWebSocket.next(message);
  }

  closeSocket(): void {
    this.myWebSocket.complete();
  }
}

export class SocketMessage{
  action: string = "SEND";
  message: string = "";
}
export interface PrintObject{
  timeInSeconds: number;
  nozzleTemp: number;
  bedTemp: number;
}
