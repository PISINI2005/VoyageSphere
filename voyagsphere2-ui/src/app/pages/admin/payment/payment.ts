import { Component, OnInit } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { switchMap, tap } from 'rxjs/operators';
import { PaymentResponseDTO } from '../../../core/models/travel.model';
import { PaymentService } from '../../../core/services/payment';
import { KpiService } from '../../../core/services/kpi-service';
import { KpiDto } from '../../../core/models/Kpidto';

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [HttpClientModule, CurrencyPipe,DatePipe],
  templateUrl: './payment.html',
  styleUrl: './payment.css',
})
export class Payment implements OnInit {
  // Master cache representing the core database state
  private allPayments: PaymentResponseDTO[] = [];

  // Array bound to the HTML loop view table
  payments: PaymentResponseDTO[] = [];

  errorMessage = '';

  // Single Source of Truth Metrics (Fetched from the backend summary endpoint)
  totalRevenue = 0;
  totalRefunded = 0;
  netRevenue = 0;
  total=0;
  gross=0;

  constructor(
    private paymentService: PaymentService,
    private kpiService: KpiService
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  // Chaining calls in a single observable stream completely prevents the double-click bug
  loadDashboardData(): void {
    this.errorMessage = '';

    this.kpiService.getKpi().pipe(
      tap((kpiData: KpiDto[] | KpiDto) => {
        const report = Array.isArray(kpiData) ? kpiData[0] : kpiData;
        if (report) {

          this.totalRefunded = report.refundedAmount || 0;
          this.total = report.totalRevenue||0;
          this.gross = this.totalRefunded+this.total;

          this.netRevenue = this.total;
        }
      }),
      switchMap(() => this.paymentService.getPayments())
    ).subscribe({
      next: (paymentsData) => {
        this.allPayments = paymentsData;
        this.payments = paymentsData;
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Failed to synchronize dashboard metrics from server.';
      }
    });
  }

  applyFilters(paymentIdStr: string, selectedStatus: string): void {
    const paymentId = paymentIdStr.trim() ? Number(paymentIdStr) : null;
    this.errorMessage = '';

    if (paymentId && !isNaN(paymentId)) {
      this.paymentService.getPaymentById(paymentId).subscribe({
        next: (res) => {
          if (res) {
            if (selectedStatus && res.status !== selectedStatus) {
              this.payments = [];
              
            } else {
              this.payments = [res];
              console.log(res);
            }
          } else {
            this.payments = [];
          }
        },
        error: () => {
          this.payments = [];
          this.errorMessage = `No records found for Payment ID #${paymentId}`;
        }
      });
    }
    else {
      if (selectedStatus) {
        this.payments = this.allPayments.filter(p => p.status === selectedStatus);
      } else {
        this.payments = [...this.allPayments];
      }
    }
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