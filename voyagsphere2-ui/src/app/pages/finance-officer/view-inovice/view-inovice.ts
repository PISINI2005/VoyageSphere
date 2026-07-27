import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { InvoiceService } from '../../../core/services/invoice-service';
import { KpiService } from '../../../core/services/kpi-service';
import { InvoiceDto } from '../../../core/models/invoicedto';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { KpiDto } from '../../../core/models/Kpidto';
import { InvoiceResponseDTO } from '../../../core/models/travel.model';

@Component({
  selector: 'app-view-invoice',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './view-inovice.html',
})
export class ViewInovice implements OnInit {
  viewinvoice: InvoiceResponseDTO[] = [];
  filteredInvoices: InvoiceResponseDTO[] = [];

  totalRevenue: number = 0;
  totalBookings: number = 0;
  totalCancellations: number = 0;

  searchInvoiceId: number | null = null;
  searchBookingId: number | null = null;
  searchUserId: number | null = null;
  selectedStatus: string = 'ALL';

  constructor(
    private invoiceService: InvoiceService,
    private kpiService: KpiService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadKpiMetrics();
    this.loadAllInvoices();
  }

  loadKpiMetrics() {
    this.kpiService.getKpi().subscribe({
      next: (kpiData: KpiDto) => {
        this.totalRevenue = kpiData.totalRevenue || 0;
        this.totalBookings = kpiData.totalBookings || 0;
        this.totalCancellations = kpiData.totalCancellations || 0;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error fetching KPI metrics:', err)
    });
  }

  loadAllInvoices() {
    this.invoiceService.getallInvoice().subscribe({
      next: (result) => {
        this.viewinvoice = result;
        this.filteredInvoices = result;
        this.cdr.detectChanges();
      },
      error: (err) => console.error(err)
    });
  }

  filterByStatus(event?: Event) {
    if (event) {
      event.preventDefault();
    }
    this.clearSearchInputs();

    if (this.selectedStatus === 'ALL') {
      this.filteredInvoices = [...this.viewinvoice];
    } else {
      this.filteredInvoices = this.viewinvoice.filter(
        (item) => item.status === this.selectedStatus
      );
    }
    this.cdr.detectChanges();
  }

  searchByInvoiceId(event?: Event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.searchInvoiceId) {
      this.showAll();
      return;
    }
    this.selectedStatus = 'ALL';
    this.searchBookingId = null;
    this.searchUserId = null;

    this.invoiceService.getbyID(this.searchInvoiceId).subscribe({
      next: (result) => {
        this.filteredInvoices = result ? [result] : [];
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.filteredInvoices = [];
        this.cdr.detectChanges();
      }
    });
  }

  searchByBookingId(event?: Event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.searchBookingId) {
      this.showAll();
      return;
    }
    this.selectedStatus = 'ALL';
    this.searchInvoiceId = null;
    this.searchUserId = null;

    this.invoiceService.getbyBookingID(this.searchBookingId).subscribe({
      next: (result) => {
        this.filteredInvoices = Array.isArray(result) ? result : [result];
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.filteredInvoices = [];
        this.cdr.detectChanges();
      }
    });
  }

  searchByUserId(event?: Event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.searchUserId) {
      this.showAll();
      return;
    }
    this.selectedStatus = 'ALL';
    this.searchInvoiceId = null;
    this.searchBookingId = null;

    this.invoiceService.getbyUserID(this.searchUserId).subscribe({
      next: (result) => {
        this.filteredInvoices = Array.isArray(result) ? result : [result];
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.filteredInvoices = [];
        this.cdr.detectChanges();
      }
    });
  }

  clearSearchInputs() {
    this.searchInvoiceId = null;
    this.searchBookingId = null;
    this.searchUserId = null;
  }

  showAll() {
    this.selectedStatus = 'ALL';
    this.clearSearchInputs();
    this.filteredInvoices = [...this.viewinvoice];
    this.cdr.detectChanges();
  }
}