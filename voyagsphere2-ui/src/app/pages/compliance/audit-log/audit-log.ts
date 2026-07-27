import { CommonModule, DatePipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

AuditLogService;

import { AuditLogService } from '../../../core/services/audit-log';
import { AuditLog } from '../../../core/models/compliance.model';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-audit-log-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink,DatePipe],
  templateUrl: './audit-log.html',
  styleUrls: ['./audit-log.css'],
})
export class AuditLogList implements OnInit {
  searchTerm = '';
  logs: AuditLog[] = [];
  filteredLogs: AuditLog[] = [];
  error = '';

  constructor(
    private auditLogService: AuditLogService,
    private cdr: ChangeDetectorRef,
    private authService: AuthService,
    private router: Router,
  ) {}

  logout(): void {
    this.authService.logout();
    this.router.navigate(['']);
  }

  ngOnInit(): void {
    this.fetchLogs();
  }

  fetchLogs(): void {
  this.error = '';
  this.cdr.detectChanges();

  this.auditLogService.getLogs().subscribe({
    next: (result: AuditLog[]) => {
      this.logs = (result || []).sort(
        (a, b) =>
          new Date(b.timestamp).getTime() -
          new Date(a.timestamp).getTime()
      );

      this.filteredLogs = [...this.logs];

      this.cdr.detectChanges();
    },

    error: (err) => {
      console.warn('Failed to fetch audit logs from backend', err);
      this.error = 'Failed to load audit logs';
      this.logs = [];
      this.filteredLogs = [];

      this.cdr.detectChanges();
    },
  });
}
  applySearch(): void {
    const term = this.searchTerm.trim().toLowerCase();

    if (!term) {
      this.filteredLogs = [...this.logs];
      this.cdr.detectChanges();
      return;
    }

    this.filteredLogs = this.logs.filter((log) =>
      [
        log.auditId?.toString() ?? '',
        log.action ?? '',
        log.entityType ?? '',
        log.entityId?.toString() ?? '',
        log.logType ?? '',
        log.userEmail ?? '',
      ]
        .join(' ')
        .toLowerCase()
        .includes(term),
    );

    this.cdr.detectChanges();
  }
}
