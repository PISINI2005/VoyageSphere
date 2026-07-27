import { ComponentFixture, TestBed, fakeAsync, tick, discardPeriodicTasks } from '@angular/core/testing';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { of, throwError } from 'rxjs';

import { PaymentGateway } from './payment-gateway';
import { PaymentService } from '../../core/services/payment';

describe('PaymentGateway', () => {
  let component: PaymentGateway;
  let fixture: ComponentFixture<PaymentGateway>;

  let paymentServiceSpy: jasmine.SpyObj<PaymentService>;
  let router: Router;

  beforeEach(async () => {
    localStorage.clear();

    paymentServiceSpy = jasmine.createSpyObj('PaymentService', ['makePayment']);

    await TestBed.configureTestingModule({
      imports: [PaymentGateway],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of({
              invoiceId: 101,
              bookingId: 201,
              amount: 5000,
              method: 'UPI',
            }),
          },
        },
        {
          provide: PaymentService,
          useValue: paymentServiceSpy,
        },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

    fixture = TestBed.createComponent(PaymentGateway);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize query params correctly', () => {
    expect(component.invoiceId).toBe(101);
    expect(component.bookingId).toBe(201);
    expect(component.amount).toBe(5000);
    expect(component.method).toBe('UPI');
  });

  it('should generate QR code URL', () => {
    expect(component.qrCodeUrl).toContain('create-qr-code');
    expect(component.qrCodeUrl).toContain('upi%3A%2F%2Fpay');
  });

  it('should apply UPI validators', () => {
    component.method = 'UPI';
    component.updateFormValidation();

    const control = component.paymentForm.get('upiId');

    control?.setValue('');
    expect(control?.invalid).toBeTruthy();

    control?.setValue('user@ybl');
    expect(control?.valid).toBeTruthy();
  });

  it('should apply credit card validators', () => {
    component.method = 'CREDIT_CARD';
    component.updateFormValidation();

    const cardControl = component.paymentForm.get('cardNumber');

    cardControl?.setValue('123');
    expect(cardControl?.invalid).toBeTruthy();

    cardControl?.setValue('1234567812345678');
    expect(cardControl?.valid).toBeTruthy();
  });

  it('should apply net banking validators', () => {
    component.method = 'NET_BANKING';
    component.updateFormValidation();

    const accountControl = component.paymentForm.get('accountNumber');

    accountControl?.setValue('123');
    expect(accountControl?.invalid).toBeTruthy();

    accountControl?.setValue('123456789012');
    expect(accountControl?.valid).toBeTruthy();
  });

  it('should apply paypal validators', () => {
    component.method = 'PAYPAL';
    component.updateFormValidation();

    const emailControl = component.paymentForm.get('paypalEmail');

    emailControl?.setValue('abc');
    expect(emailControl?.invalid).toBeTruthy();

    emailControl?.setValue('test@gmail.com');
    expect(emailControl?.valid).toBeTruthy();
  });

  it('should validate debit card fields', () => {
    component.method = 'DEBIT_CARD';
    component.updateFormValidation();

    component.paymentForm.patchValue({
      cardNumber: '1234567812345678',
      cardHolder: 'John Doe',
      expiry: '12/30',
      cvv: '123',
    });

    expect(component.paymentForm.get('cardNumber')?.valid).toBeTruthy();
    expect(component.paymentForm.get('cardHolder')?.valid).toBeTruthy();
    expect(component.paymentForm.get('expiry')?.valid).toBeTruthy();
    expect(component.paymentForm.get('cvv')?.valid).toBeTruthy();
  });

  it('should not process payment when form is invalid', () => {
    const alertSpy = spyOn(window, 'alert').and.callFake(() => {});

    component.paymentForm.reset();

    component.processPayment();

    expect(alertSpy).toHaveBeenCalled();
    expect(paymentServiceSpy.makePayment).not.toHaveBeenCalled();
  });

  it('should call payment service with correct payload', () => {
    component.method = 'UPI';
    component.updateFormValidation();

    component.paymentForm.patchValue({
      upiId: 'user@ybl',
    });

    expect(component.paymentForm.valid).toBeTruthy();

    paymentServiceSpy.makePayment.and.returnValue(of({} as any));

    component.processPayment();

    expect(paymentServiceSpy.makePayment).toHaveBeenCalledTimes(1);
    expect(paymentServiceSpy.makePayment).toHaveBeenCalledWith({
      invoiceId: 101,
      amount: 5000,
      paymentMethod: 'UPI',
    });
  });

  it('should handle payment failure', () => {
    const alertSpy = spyOn(window, 'alert').and.callFake(() => {});

    component.method = 'UPI';
    component.updateFormValidation();

    component.paymentForm.patchValue({
      upiId: 'user@ybl',
    });

    paymentServiceSpy.makePayment.and.returnValue(
      throwError(() => new Error('Payment Failed'))
    );

    component.processPayment();

    expect(paymentServiceSpy.makePayment).toHaveBeenCalled();
    expect(component.isLoading).toBeFalsy();
    expect(alertSpy).toHaveBeenCalled();
  });

  it('should process payment successfully', fakeAsync(() => {
    component.method = 'UPI';
    component.updateFormValidation();

    component.paymentForm.patchValue({
      upiId: 'user@ybl',
    });



paymentServiceSpy.makePayment.and.returnValue(of({} as any));

    component.processPayment();

    expect(component.isLoading).toBeTruthy();

    tick(2000);

    expect(component.paymentSuccess).toBeTruthy();
    expect(component.transactionId).toContain('TXN');

    tick(5000);

    expect(router.navigate).toHaveBeenCalled();
    discardPeriodicTasks();
  }));
});