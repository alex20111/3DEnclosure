import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SerialTerminalComponent } from './serial-terminal.component';

describe('SerialTerminalComponent', () => {
  let component: SerialTerminalComponent;
  let fixture: ComponentFixture<SerialTerminalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SerialTerminalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SerialTerminalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
