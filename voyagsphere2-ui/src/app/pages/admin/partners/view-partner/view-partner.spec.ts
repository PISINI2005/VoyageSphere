import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewPartner } from './view-partner';

describe('ViewPartner', () => {
  let component: ViewPartner;
  let fixture: ComponentFixture<ViewPartner>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ViewPartner],
    }).compileComponents();

    fixture = TestBed.createComponent(ViewPartner);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
