import { Injectable } from '@angular/core';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import { Observable } from 'rxjs';
import { Constants } from '../_model/Constants';

@Injectable({
  providedIn: 'root'
})
export class PiWebSocketService {

  myWebSocket: WebSocketSubject<SocketMessage>;
  
  constructor() { 
    this.myWebSocket = webSocket(`ws://${Constants.HOST_ADDRESS}:8080/printerEvents/`);
  }


  connect(): Observable<SocketMessage> { 

       //send websocket registration
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
  dataType: string = "NONE";
  message: string = "";
}

