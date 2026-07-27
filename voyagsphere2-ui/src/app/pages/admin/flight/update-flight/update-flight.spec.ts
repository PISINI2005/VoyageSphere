import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateFlight } from './update-flight';

describe('UpdateFlight', () => {
  let component: UpdateFlight;
  let fixture: ComponentFixture<UpdateFlight>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpdateFlight],
    }).compileComponents();

    fixture = TestBed.createComponent(UpdateFlight);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
