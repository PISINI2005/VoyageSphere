import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PaymentService } from '../../core/services/payment';
import { PaymentDTO } from '../../core/models/travel.model';

@Component({
  selector: 'app-payment-gateway',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './payment-gateway.html',
  styleUrl: './payment-gateway.css',
})
export class PaymentGateway implements OnInit {
  paymentForm: FormGroup;
  invoiceId!: number;
  bookingId!: number;
  amount!: number;
  method!: string;
  isLoading = false;
  paymentSuccess = false;
  transactionId = '';

  constructor(
    private fb: FormBuilder,
    public route: ActivatedRoute,
    public router: Router,
    private paymentService: PaymentService
  ) {
    // Initialize form with all possible fields
    this.paymentForm = this.fb.group({
      // Card Details
      cardNumber: ['', [Validators.required, Validators.pattern('^[0-9]{16}$')]],
      cardHolder: ['', Validators.required],
      expiry: ['', [Validators.required, Validators.pattern('^(0[1-9]|1[0-2])\/[0-9]{2}$')]],
      cvv: ['', [Validators.required, Validators.pattern('^[0-9]{3}$')]],

      // UPI Details
      upiId: ['', [Validators.required, Validators.pattern('^[a-zA-Z0-9.\\-]+@[a-zA-Z0-9-]+$')]],

      // Net Banking
      bankName: ['', Validators.required],
      accountNumber: ['', [Validators.required, Validators.pattern('^[0-9]{9,18}$')]],
      ifsc: ['', [Validators.required, Validators.pattern('^[A-Z]{4}0[A-Z0-9]{6}$')]],

      // PayPal
      paypalEmail: ['', [Validators.required, Validators.email]],
      paypalPassword: ['', Validators.required],
    });
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.invoiceId = +params['invoiceId'];
      this.bookingId = +params['bookingId'];
      this.amount = +params['amount'];
      this.method = params['method'] || 'UPI';
      this.updateFormValidation();
    });
  }

  updateFormValidation() {
    // Dynamically update validators based on method
    const controls = this.paymentForm.controls;

    // Reset all validators first
    Object.values(controls).forEach(control => {
      control.clearValidators();
      control.updateValueAndValidity();
    });

    if (this.method === 'CREDIT_CARD' || this.method === 'DEBIT_CARD') {
      controls['cardNumber'].setValidators([Validators.required, Validators.pattern('^[0-9]{16}$')]);
      controls['cardHolder'].setValidators([Validators.required]);
      controls['expiry'].setValidators([Validators.required, Validators.pattern('^(0[1-9]|1[0-2])\/[0-9]{2}$')]);
      controls['cvv'].setValidators([Validators.required, Validators.pattern('^[0-9]{3}$')]);
    } else if (this.method === 'UPI') {
      controls['upiId'].setValidators([Validators.required, Validators.pattern('^[a-zA-Z0-9.\\-]+@[a-zA-Z0-9-]+$')]);
    } else if (this.method === 'NET_BANKING') {
      controls['bankName'].setValidators([Validators.required]);
      controls['accountNumber'].setValidators([Validators.required, Validators.pattern('^[0-9]{9,18}$')]);
      controls['ifsc'].setValidators([Validators.required, Validators.pattern('^[A-Z]{4}0[A-Z0-9]{6}$')]);
    } else if (this.method === 'PAYPAL') {
      controls['paypalEmail'].setValidators([Validators.required, Validators.email]);
      controls['paypalPassword'].setValidators([Validators.required]);
    }

    // Refresh validity
    Object.values(controls).forEach(control => control.updateValueAndValidity());
  }

  get qrCodeUrl(): string {
    const upiString = `upi://pay?pa=travel360@bank&pn=Travel360&am=${this.amount}&cu=INR`;
    return `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(upiString)}`;
  }

  processPayment() {
    if (this.paymentForm.invalid) {
      this.paymentForm.markAllAsTouched();
      alert('Please fill all required fields correctly.');
      return;
    }

    this.isLoading = true;

    const paymentPayload: PaymentDTO = {
      invoiceId: this.invoiceId,
      amount: this.amount,
      paymentMethod: this.method as any
    };

    this.paymentService.makePayment(paymentPayload).subscribe({
      next: () => {
        // Mimic a real payment delay for a better user experience
        setTimeout(() => {
          this.isLoading = false;
          this.transactionId = 'TXN' + Math.floor(Math.random() * 1000000000).toString().padStart(9, '0');
          this.paymentSuccess = true;

          // Give user enough time to see the "Payment Successful" screen
          setTimeout(() => {
            this.router.navigate(['/success', this.invoiceId]);
          }, 5000);
        }, 2000);
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Payment failed:', err);
        alert('Payment failed. Please try again.');
      }
    });

  }
}
