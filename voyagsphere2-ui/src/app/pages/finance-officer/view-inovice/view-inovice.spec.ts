import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewInovice } from './view-inovice';

describe('ViewInovice', () => {
  let component: ViewInovice;
  let fixture: ComponentFixture<ViewInovice>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ViewInovice],
    }).compileComponents();

    fixture = TestBed.createComponent(ViewInovice);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
