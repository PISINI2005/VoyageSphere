import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ComplianceSidebarComponent } from '../../pages/compliance/compliance-sidebar-component/compliance-sidebar-component';



@Component({
  selector: 'app-compliance-layout',
  standalone: true,
  imports: [RouterOutlet, ComplianceSidebarComponent],
  templateUrl: './compliance-layout-component.html',
  styleUrls: ['./compliance-layout-component.css']
})

export class ComplianceLayoutComponent {}