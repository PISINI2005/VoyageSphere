import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { NavbarComponent } from '../../layout/navbar/navbar';
import { PaymentService } from '../../core/services/payment';
import { PaymentDTO, InvoiceResponseDTO } from '../../core/models/travel.model';
import { AgentContextService } from '../../core/services/agent-context';
import { AuthService } from '../../core/services/auth';

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NavbarComponent,RouterLink
  ],
  templateUrl: './payment.html',
  styleUrl: './payment.css'
})
export class PaymentComponent implements OnInit {


  // Card Payment
cardNumber: string = '';
cardHolder: string = '';
expiry: string = '';
cvv: string = '';

// UPI
upiId: string = '';

// Net Banking
bank: string = '';
accountNumber: string = '';
confirmAccountNumber: string = '';
ifsc: string = '';

// PayPal
paypalEmail: string = '';
paypalPassword: string = '';

  bookingId!: number;
  invoice: InvoiceResponseDTO | null = null;
  isLoading = true;

  paymentForm: PaymentDTO = {
    invoiceId: 0,
    amount: 0,
    paymentMethod: 'UPI'
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private paymentService: PaymentService,
    public agentContext:AgentContextService,
    public authService:AuthService
  ) {}

  ngOnInit(): void {
    this.bookingId = +this.route.snapshot.params['bookingId'];
    this.loadInvoice();
  }

  loadInvoice() {
    this.isLoading = true;
    this.paymentService.getInvoicesByBooking(this.bookingId).subscribe({
      next: (invoices) => {
        if (invoices && invoices.length > 0) {
          this.invoice = invoices[0];
          this.paymentForm.invoiceId = this.invoice.invoiceId;
          this.paymentForm.amount = this.invoice.amount;
        }
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading invoice:', err);
        this.isLoading = false;
      }
    });
  }

  makePayment() {
    if (!this.invoice) {
      alert('Invoice not found');
      return;
    }

    // Instead of calling service directly, navigate to the gateway page
    // Pass the required details in the route or via a service
    this.router.navigate(['/payment-gateway'], {
      queryParams: {
        invoiceId: this.invoice.invoiceId,
        amount: this.invoice.amount,
        method: this.paymentForm.paymentMethod,
        bookingId: this.bookingId
      }
    });
  }

  sendInvoiceToCustomer(): void {

  alert(
    `Invoice #${this.invoice?.invoiceId} sent to ${this.agentContext.selectedCustomerEmail}`
  );

}

downloadInvoice(): void {

  window.print();

}
}
