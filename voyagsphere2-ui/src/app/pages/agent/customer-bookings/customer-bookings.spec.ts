import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CustomerBookings } from './customer-bookings';

describe('CustomerBookings', () => {
  let component: CustomerBookings;
  let fixture: ComponentFixture<CustomerBookings>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CustomerBookings],
    }).compileComponents();

    fixture = TestBed.createComponent(CustomerBookings);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
