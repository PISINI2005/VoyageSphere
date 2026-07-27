import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { ComplaintService } from '../../../core/services/complaint';
import { AuditLogService } from '../../../core/services/audit-log';

@Component({
  selector: 'app-compliance-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class DashboardComponent implements OnInit {

  recentComplaints: any[] = [];
  recentLogs: any[] = [];

  constructor(
    private authService: AuthService,
    private router: Router,
    private complaintService: ComplaintService,
    private auditLogService: AuditLogService
  ) {}

  ngOnInit(): void {
    this.loadRecentComplaints();
    this.loadRecentLogs();
  }

  loadRecentComplaints(): void {
    this.complaintService.getAllComplaints().subscribe({
      next: (data: any[]) => {
        this.recentComplaints = data.slice(0, 5);
      },
      error: (err) => {
        console.error('Failed to load complaints', err);
      }
    });
  }

  loadRecentLogs(): void {
    this.auditLogService.getLogs().subscribe({
      next: (data: any[]) => {
        this.recentLogs = data.slice(0, 5);
      },
      error: (err) => {
        console.error('Failed to load audit logs', err);
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['']);
  }
}