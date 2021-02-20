import { PrintService } from './../../services/print.service';
import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { Subscription, interval } from 'rxjs';
import { PrintMessage } from 'src/app/_model/PrintMessage';

@Component({
  selector: 'app-countdown',
  templateUrl: './countdown.component.html',
  styleUrls: ['./countdown.component.css']
})
export class CountdownComponent implements OnInit,  OnDestroy , OnChanges{


  @Input()
  timerStartDate!: Date;
  private subscription!: Subscription;
  
   private savedTimer: Date;

  public dateNow = new Date();
  // public dDay = new Date('Jan 01 2021 00:00:00');
  milliSecondsInASecond = 1000;
  hoursInADay = 24;
  minutesInAnHour = 60;
  SecondsInAMinute  = 60;

  public timeDifference: number = 0;
  public secondsToDday: number = 0;
  public minutesToDday: number = 0;
  public hoursToDday: number = 0;
  public daysToDday: number = 0;

  constructor(private printService: PrintService) { }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.savedTimer != this.timerStartDate){
      if (this.subscription){
        this.subscription.unsubscribe();
      }
      this.startTimer();
    }
  }

  private getTimeDifference () {
      this.timeDifference = this.timerStartDate.getTime() - new  Date().getTime();
      this.allocateTimeUnits(this.timeDifference);
  }

private allocateTimeUnits (timeDifference: number) {
      this.secondsToDday = Math.floor((timeDifference) / (this.milliSecondsInASecond) % this.SecondsInAMinute);
      this.minutesToDday = Math.floor((timeDifference) / (this.milliSecondsInASecond * this.minutesInAnHour) % this.SecondsInAMinute);
      this.hoursToDday = Math.floor((timeDifference) / (this.milliSecondsInASecond * this.minutesInAnHour * this.SecondsInAMinute) % this.hoursInADay);
      this.daysToDday = Math.floor((timeDifference) / (this.milliSecondsInASecond * this.minutesInAnHour * this.SecondsInAMinute * this.hoursInADay));
}

  ngOnInit() {
   this.startTimer();
  }

  startTimer(){
    this.savedTimer = this.timerStartDate;
    this.getTimeDifference(); 
    
     this.subscription = interval(1000)
         .subscribe(x => { 
           this.getTimeDifference(); 
           if (this.minutesToDday === -1 && this.hoursToDday === -1 && this.daysToDday === -1 && this.secondsToDday < 0){
                   let printFinish = new PrintMessage();
             printFinish.finished = true;
            this.subscription.unsubscribe();            
           }

          //  console.log(this.daysToDday,this.hoursToDday , this.minutesToDday ,this.secondsToDday);
         });
  }

 ngOnDestroy() {
   if ( this.subscription){
    this.subscription.unsubscribe();
   }
 }

}
