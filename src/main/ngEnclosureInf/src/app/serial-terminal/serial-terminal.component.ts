import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { PiWebSocketService, SocketMessage, WsAction } from '../services/pi-web-socket.service';

@Component({
  selector: 'app-serial-terminal',
  templateUrl: './serial-terminal.component.html',
  styleUrls: ['./serial-terminal.component.css']
})
export class SerialTerminalComponent implements OnInit, OnDestroy {

  message: string = "";

  terminalForm: FormGroup;
  webSocketSubs: Subscription;

  constructor(private formBuilder: FormBuilder, private wsSocket: PiWebSocketService) { }
  ngOnDestroy(): void {

    if (this.webSocketSubs){
     this.webSocketSubs.unsubscribe();
    }
  }

  ngOnInit(): void {


    setTimeout(() => this.initWebSocket(), 800);

    this.terminalForm = this.formBuilder.group({
      frm_terminal: [''],
      frm_send_text: ['', [Validators.required]]
    });


  }

  initWebSocket() {
    this.webSocketSubs = this.wsSocket.connect().subscribe(wsReturn => {
      console.log("WebSocket result", wsReturn);

      this.webSocketData(wsReturn);
    },
      err => {
        console.log("Web Socket error!!!", err);
      });

    const regSocket = new SocketMessage();
    regSocket.action = WsAction.REGISTER_FOR_SERIAL;
    regSocket.message = "false"; //this is required to tell that this user is not the backend user.
    this.wsSocket.sendMessage(regSocket);
  }
  buildForm() {

  }

  webSocketData(data: SocketMessage) {
    let textAreaStr = this.terminalForm.controls.frm_terminal.value;

    textAreaStr = textAreaStr + data.message;
    // console.log("textAreaStr: " , textAreaStr);

    this.terminalForm.patchValue({ frm_terminal: textAreaStr });
  }

  send() {
    let sendValue = this.terminalForm.controls.frm_send_text.value;

    console.log("Sending: " , sendValue);
    const regSocket = new SocketMessage();
    regSocket.action = WsAction.SEND_TO_SERIAL_CONSOLE;
    regSocket.dataType = 'PRINTER_SERIAL_DATA_TO_BACKEND';
    regSocket.message = sendValue; //this is required to tell that this user is not the backend user.
    this.wsSocket.sendMessage(regSocket);
  }

}
