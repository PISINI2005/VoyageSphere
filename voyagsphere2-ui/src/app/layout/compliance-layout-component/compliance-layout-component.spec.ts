import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ComplianceLayoutComponent } from './compliance-layout-component';

describe('ComplianceLayoutComponent', () => {
  let component: ComplianceLayoutComponent;
  let fixture: ComponentFixture<ComplianceLayoutComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ComplianceLayoutComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ComplianceLayoutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
