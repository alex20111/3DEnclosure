import { Injectable } from '@angular/core';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import { Observable , EMPTY, timer} from 'rxjs';
import { Constants } from '../_model/Constants';
import {  catchError, tap, delayWhen, retryWhen } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class PiWebSocketService {
  
  WS_ENDPOINT: string = `ws://${Constants.HOST_ADDRESS}:8080/printerEvents/`;

  myWebSocket: WebSocketSubject<SocketMessage>;
  
  constructor() { 
    console.log("COnstrustor websocket");
    this.myWebSocket = this.getNewWebSocket();
  }

  public connect(cfg: { reconnect: boolean } = { reconnect: false }): Observable<SocketMessage> {

    if (!this.myWebSocket || this.myWebSocket.closed) {
      this.myWebSocket = this.getNewWebSocket();
      const messages = this.myWebSocket.pipe(cfg.reconnect ? this.reconnect : o => o,
        tap({
          error: error => console.log(error),
        }), catchError(_ => EMPTY))
     
    }
    return this.myWebSocket.asObservable();
  }

  /**
   * Retry a given observable by a time span
   * @param observable the observable to be retried
   */
   private reconnect(observable: Observable<any>): Observable<any> {
    return observable.pipe(retryWhen(errors => errors.pipe(tap(val => console.log('[Data Service] Try to reconnect', val)),
    delayWhen(_ => timer(2000)))));
  }

  sendMessage(message: SocketMessage): void {
    console.log('Sending: ', message);
    this.myWebSocket.next(message);
  }

  closeSocket(): void {
    this.myWebSocket.complete();
  }

  private getNewWebSocket(): WebSocketSubject<SocketMessage> {
    return webSocket({
      url: this.WS_ENDPOINT,
      openObserver: {
        next: () => {
          console.log('[DataService]: connection ok');
        }
      },
      closeObserver: {
        next: () => {
          console.log('[DataService]: connection closed');
        }
      },

    });
  }
}



export class SocketMessage{
  action: string = "SEND";
  dataType: string = "NONE";
  message: string = "";
  additionalMessage: string = "";
}

export enum WsAction {
	REGISTER = "REGISTER",
	REGISTER_FOR_SERIAL ="REGISTER_FOR_SERIAL",
	SEND_TO_SERIAL_CONSOLE = "SEND_TO_SERIAL_CONSOLE",
	CLOSE = "CLOSE", 
	SEND = "SEND", 
	PRINT_FINISHED = "PRINT_FINISHED", 
	REQUEST_DATA	= "REQUEST_DATA"
	
}


