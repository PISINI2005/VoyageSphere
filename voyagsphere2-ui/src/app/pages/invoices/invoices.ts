import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { NavbarComponent } from '../../layout/navbar/navbar';
import { PaymentService } from '../../core/services/payment';

import { InvoiceResponseDTO, PaymentResponseDTO } from '../../core/models/travel.model';

@Component({
  selector: 'app-invoices',
  standalone: true,
  imports: [
    CommonModule,
    NavbarComponent
  ],
  providers:[CurrencyPipe,DatePipe],
  templateUrl: './invoices.html',
  styleUrl: './invoices.css'
})
export class InvoicesComponent implements OnInit {

  invoices: InvoiceResponseDTO[] = [];
  currentPage = 0;
pageSize = 5;

totalPages = 0;
totalElements = 0;

isFirst = true;
isLast = true;

  selectedPayments: PaymentResponseDTO[] = [];
  showPaymentsModal: boolean = false;

  constructor(
    private paymentService: PaymentService,
    private router: Router
  ) {}

  viewPayments(invoiceId: number): void {
    this.paymentService.getPaymentsForInvoice(invoiceId).subscribe({
      next: (payments) => {
        this.selectedPayments = payments;
        console.log(payments);
        this.showPaymentsModal = true;
      },
      error: (err) => {
        console.error('Error fetching payments:', err);
        alert('Could not fetch payment history for this invoice.');
      }
    });
  }

  closePaymentsModal(): void {
    this.showPaymentsModal = false;
    this.selectedPayments = [];
  }

  ngOnInit(): void {
    this.loadInvoices();
  }

 loadInvoices(): void {

  this.paymentService
    .getMyInvoices(this.currentPage, this.pageSize)
    .subscribe({

      next: (data) => {

        this.invoices = data.content;

        this.currentPage = data.number;
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;

        this.isFirst = data.first;
        this.isLast = data.last;

        console.log(data);
      },

      error: (err) =>
        console.error('Error loading invoices:', err)
    });
}

  payNow(invoiceId: number): void {
    // For now, la we'll redirect to payment with a dummy bookingId or use the invoiceId
    // Since the current route is /payment/:bookingId, we might need to find the bookingId first.
    const invoice = this.invoices.find(i => i.invoiceId === invoiceId);
    if (invoice) {
      this.router.navigate(['/payment', invoice.bookingId]);
    }
  }

  downloadInvoice(invoice: InvoiceResponseDTO): void {

  const doc = new jsPDF();

  const user = JSON.parse(localStorage.getItem('user') || '{}');

  // Header
  doc.setFillColor(13, 110, 253);
  doc.rect(0, 0, 210, 35, "F");

  doc.setTextColor(255,255,255);
  doc.setFontSize(24);
  doc.setFont("helvetica","bold");
  doc.text("VoyageSphere",15,18);

  doc.setFontSize(11);
  doc.setFont("helvetica","normal");
  doc.text("Travel Booking Invoice",15,27);

  // Invoice title
  doc.setTextColor(0,0,0);
  doc.setFontSize(20);
  doc.setFont("helvetica","bold");
  doc.text("INVOICE",150,50);

  // Company
  doc.setFontSize(11);
  doc.setFont("helvetica","normal");

  doc.text("VoyageSphere Pvt. Ltd.",15,50);
  doc.text("Chennai, Tamil Nadu",15,57);
  doc.text("support@voyagesphere.com",15,64);
  doc.text("+91 9876543210",15,71);

  // Invoice Info
  autoTable(doc,{
    startY:80,
    theme:'grid',
    head:[['Invoice Information','']],
    body:[
      ['Invoice No',`INV-${invoice.invoiceId}`],
      ['Booking ID',invoice.bookingId],
      ['Invoice Date',new Date().toLocaleDateString()],
      ['Status',invoice.status]
    ],
    headStyles:{
      fillColor:[13,110,253]
    }
  });

  // Customer Info
  autoTable(doc,{
    startY:(doc as any).lastAutoTable.finalY+8,
    theme:'grid',
    head:[['Customer Information','']],
    body:[
      ['Customer ID',user.userId],
      ['Email',user.email],
      ['Role',user.role]
    ],
    headStyles:{
      fillColor:[25,135,84]
    }
  });

  // Booking Summary
  autoTable(doc,{
    startY:(doc as any).lastAutoTable.finalY+8,
    head:[['Description','Booking ID','Status','Amount']],
    body:[
      [
        'Travel Booking',
        invoice.bookingId,
        invoice.status,
        `₹ ${invoice.amount.toLocaleString()}`
      ]
    ],
    headStyles:{
      fillColor:[13,110,253]
    },
    foot:[
      ['', '', 'Grand Total', `₹ ${invoice.amount.toLocaleString()}`]
    ],
    footStyles:{
      fillColor:[33,37,41],
      textColor:255,
      fontStyle:'bold'
    }
  });

  // Footer
  const y=(doc as any).lastAutoTable.finalY+20;

  doc.setDrawColor(200);
  doc.line(15,y,195,y);

  doc.setFontSize(10);
  doc.setTextColor(100);

  doc.text(
    "Thank you for choosing VoyageSphere.",
    15,
    y+10
  );

  doc.text(
    "This is a computer-generated invoice and requires no signature.",
    15,
    y+18
  );

  doc.save(`Invoice-${invoice.invoiceId}.pdf`);
}

nextPage(): void {
  if (!this.isLast) {
    this.currentPage++;
    this.loadInvoices();
  }
}

previousPage(): void {
  if (!this.isFirst) {
    this.currentPage--;
    this.loadInvoices();
  }
}

goToPage(page: number): void {
  this.currentPage = page;
  this.loadInvoices();
}

get pages(): number[] {
  return Array.from(
    { length: this.totalPages },
    (_, i) => i
  );
}
}
