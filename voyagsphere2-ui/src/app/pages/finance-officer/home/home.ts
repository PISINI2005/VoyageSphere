import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { KpiService } from '../../../core/services/kpi-service';
import { PaymentService } from '../../../core/services/payment';
import { PaymentResponseDTO } from '../../../core/models/travel.model';
import { KpiDto } from '../../../core/models/Kpidto';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterModule, HttpClientModule, CurrencyPipe, DatePipe,CommonModule],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home implements OnInit {
  officerName = 'Sarah Jenkins';
  currentDate = new Date();
  errorMessage = '';

  // KPI Stream Summary Metrics
  grossRevenue = 0;
  refundedAmount = 0;
  netRevenue = 0;
  cancellationRate = 0;
  totalBookings = 0;

  // Payments Stream Metrics 
  recentPayments: PaymentResponseDTO[] = [];

  constructor(
    private kpiService: KpiService,
    private paymentService: PaymentService
  ) {}

  ngOnInit(): void {
    this.refreshDashboardData();
  }

  // Combines all server queries into a single stream to complete everything on one single click
  refreshDashboardData(): void {
    this.errorMessage = '';
    
    forkJoin({
      kpi: this.kpiService.getKpi(),
      payments: this.paymentService.getPayments()
    }).subscribe({
      next: (result) => {
        // 1. Process KPI Aggregate Metrics
        const report: KpiDto = Array.isArray(result.kpi) ? result.kpi[0] : result.kpi;
        if (report) {
          this.grossRevenue = report.totalRevenue || 0;
          this.refundedAmount = report.refundedAmount || 0;
          this.netRevenue = this.grossRevenue - this.refundedAmount;
          this.cancellationRate = report.cancellationRate || 0;
          this.totalBookings = report.totalBookings || 0;
        }

        // 2. Process Payment Records Stream (Slice the latest 5 records for home feed)
        if (result.payments) {
          this.recentPayments = result.payments.slice(0, 5);
        }
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Failed to load synchronous financial modules.';
      }
    });
  }

  // getStatusClass(status: string): string {
  //   switch (status) {
  //     case 'SUCCESS': return 'bg-success-subtle text-success border border-success-subtle';
  //     case 'PENDING': return 'bg-warning-subtle text-warning-emphasis border border-warning-subtle';
  //     case 'FAILED': return 'bg-danger-subtle text-danger border border-danger-subtle';
  //     case 'REFUNDED': return 'bg-info-subtle text-info-emphasis border border-info-subtle';
  //     default: return 'bg-secondary-subtle';
  //   }
  // }
   getStatusClass(status: string): string {
    switch (status) {
      case 'SUCCESS': 
        return 'bg-success text-white';
      case 'PENDING': 
        return 'bg-warning text-dark'; // Warning looks best with dark text
      case 'FAILED': 
        return 'bg-danger text-white';
      case 'REFUNDED': 
        return 'bg-info text-dark'; // Info looks best with dark text
      default: 
        return 'bg-secondary text-white';
    }
  }
}