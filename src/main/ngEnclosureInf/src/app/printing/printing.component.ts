import { SessionService } from './../services/session.service';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-printing',
  templateUrl: './printing.component.html',
  styleUrls: ['./printing.component.css']
})
export class PrintingComponent implements OnInit {

  constructor(private session: SessionService, private router: Router) { }

  ngOnInit(): void {
  }

  backButton(){

    let dateNow = new Date().getTime(); 
 
  var today = new Date();

  let hor = today.getHours() + (1 * 60 * 60 * 1000);

  // var myToday = new Date(today.getFullYear(), today.getMonth(), today.getDate(), today.getHours() + 1, 0, 0);
  const myDate = new Date(dateNow + 10000);

  

    this.session.putSharedObject("print", myDate);
    this.router.navigate(['/']);
  }

}
