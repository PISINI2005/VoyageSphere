import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateHotel } from './update-hotel';

describe('UpdateHotel', () => {
  let component: UpdateHotel;
  let fixture: ComponentFixture<UpdateHotel>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpdateHotel],
    }).compileComponents();

    fixture = TestBed.createComponent(UpdateHotel);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
