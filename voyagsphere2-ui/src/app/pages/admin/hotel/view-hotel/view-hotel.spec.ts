import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewHotel } from './view-hotel';

describe('ViewHotel', () => {
  let component: ViewHotel;
  let fixture: ComponentFixture<ViewHotel>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ViewHotel],
    }).compileComponents();

    fixture = TestBed.createComponent(ViewHotel);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
