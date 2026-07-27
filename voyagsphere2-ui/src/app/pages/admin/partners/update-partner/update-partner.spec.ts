import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdatePartner } from './update-partner';

describe('UpdatePartner', () => {
  let component: UpdatePartner;
  let fixture: ComponentFixture<UpdatePartner>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpdatePartner],
    }).compileComponents();

    fixture = TestBed.createComponent(UpdatePartner);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
