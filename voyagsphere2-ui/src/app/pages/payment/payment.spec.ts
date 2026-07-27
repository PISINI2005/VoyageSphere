import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { of, throwError } from 'rxjs';

import { PaymentComponent } from './payment';
import { PaymentService } from '../../core/services/payment';
import { AgentContextService } from '../../core/services/agent-context';
import { AuthService } from '../../core/services/auth';
import { InvoiceResponseDTO } from '../../core/models/travel.model';

describe('PaymentComponent', () => {
  let component: PaymentComponent;
  let fixture: ComponentFixture<PaymentComponent>;

  let paymentServiceSpy: jasmine.SpyObj<PaymentService>;
  let agentContextSpy: jasmine.SpyObj<AgentContextService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let router: Router;

  const mockInvoice: InvoiceResponseDTO = {
    invoiceId: 501,
    amount: 12000,
  } as InvoiceResponseDTO;

  beforeEach(async () => {
    paymentServiceSpy = jasmine.createSpyObj('PaymentService', ['getInvoicesByBooking']);
    agentContextSpy = jasmine.createSpyObj('AgentContextService', [], {
      selectedCustomerEmail: 'customer@example.com',
    });
    
    // Added 'isTravelAgent' and 'getRole' to spy methods
    authServiceSpy = jasmine.createSpyObj('AuthService', [
      'isLoggedIn', 
      'isTravelAgent', 
      'getRole'
    ]);

    // Mock return values used by template or component
    authServiceSpy.isLoggedIn.and.returnValue(true);
    authServiceSpy.isTravelAgent.and.returnValue(false);
    authServiceSpy.getRole.and.returnValue('USER');

    paymentServiceSpy.getInvoicesByBooking.and.returnValue(of([mockInvoice]));

    await TestBed.configureTestingModule({
      imports: [PaymentComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: PaymentService, useValue: paymentServiceSpy },
        { provide: AgentContextService, useValue: agentContextSpy },
        { provide: AuthService, useValue: authServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              params: { bookingId: '201' },
            },
          },
        },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

    fixture = TestBed.createComponent(PaymentComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit & loadInvoice', () => {
    it('should initialize bookingId from route params and fetch invoice successfully', () => {
      fixture.detectChanges(); // Triggers ngOnInit and template evaluation

      expect(component.bookingId).toBe(201);
      expect(paymentServiceSpy.getInvoicesByBooking).toHaveBeenCalledWith(201);
      expect(component.invoice).toEqual(mockInvoice);
      expect(component.paymentForm.invoiceId).toBe(501);
      expect(component.paymentForm.amount).toBe(12000);
      expect(component.isLoading).toBeFalsy();
    });

    it('should handle empty invoice list gracefully', () => {
      paymentServiceSpy.getInvoicesByBooking.and.returnValue(of([]));

      component.loadInvoice();

      expect(component.invoice).toBeNull();
      expect(component.isLoading).toBeFalsy();
    });

    it('should handle API failure when fetching invoice', () => {
      spyOn(console, 'error').and.callFake(() => {});
      paymentServiceSpy.getInvoicesByBooking.and.returnValue(
        throwError(() => new Error('API Failure'))
      );

      component.loadInvoice();

      expect(component.isLoading).toBeFalsy();
      expect(component.invoice).toBeNull();
    });
  });

  describe('makePayment', () => {
    it('should show alert if invoice does not exist', () => {
      const alertSpy = spyOn(window, 'alert').and.callFake(() => {});
      component.invoice = null;

      component.makePayment();

      expect(alertSpy).toHaveBeenCalledWith('Invoice not found');
      expect(router.navigate).not.toHaveBeenCalled();
    });

    it('should navigate to payment gateway with queryParams when invoice exists', () => {
      component.invoice = mockInvoice;
      component.bookingId = 201;
      component.paymentForm.paymentMethod = 'UPI';

      component.makePayment();

      expect(router.navigate).toHaveBeenCalledWith(['/payment-gateway'], {
        queryParams: {
          invoiceId: 501,
          amount: 12000,
          method: 'UPI',
          bookingId: 201,
        },
      });
    });
  });

  describe('Utility Actions', () => {
    it('should trigger alert with invoice details when sending invoice to customer', () => {
      const alertSpy = spyOn(window, 'alert').and.callFake(() => {});
      component.invoice = mockInvoice;

      component.sendInvoiceToCustomer();

      expect(alertSpy).toHaveBeenCalledWith(
        'Invoice #501 sent to customer@example.com'
      );
    });

    it('should call window.print when downloadInvoice is invoked', () => {
      const printSpy = spyOn(window, 'print').and.callFake(() => {});

      component.downloadInvoice();

      expect(printSpy).toHaveBeenCalled();
    });
  });
});