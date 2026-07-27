import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ComplianceSidebarComponent } from './compliance-sidebar-component';

describe('ComplianceSidebarComponent', () => {
  let component: ComplianceSidebarComponent;
  let fixture: ComponentFixture<ComplianceSidebarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ComplianceSidebarComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ComplianceSidebarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
