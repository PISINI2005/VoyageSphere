import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ChangeDetectorRef, NO_ERRORS_SCHEMA } from '@angular/core';
import { of, throwError } from 'rxjs';

import { ViewTransport } from './view-transport';
import { TransportService } from '../../../../core/services/transport';

describe('ViewTransport', () => {
  let component: ViewTransport;
  let fixture: ComponentFixture<ViewTransport>;

  let transportServiceSpy: jasmine.SpyObj<TransportService>;

  const mockTransports = [
    {
      transportId: 1,
      mode: 'BUS',
      source: 'New York',
      destination: 'Boston',
      transportStatus: 'AVAILABLE',
    },
    {
      transportId: 2,
      mode: 'TRAIN',
      source: 'Chicago',
      destination: 'Detroit',
      transportStatus: 'INACTIVE',
    },
  ];

  beforeEach(async () => {
    transportServiceSpy = jasmine.createSpyObj('TransportService', [
      'getTransports',
      'updateTransportStatus',
    ]);

    transportServiceSpy.getTransports.and.returnValue(
      of({ content: mockTransports } as any)
    );

    await TestBed.configureTestingModule({
      imports: [ViewTransport],
      providers: [
        provideRouter([]),
        { provide: TransportService, useValue: transportServiceSpy },
        ChangeDetectorRef,
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(ViewTransport);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit & searchTransports', () => {
    it('should search transports on initialization using paginated response content', () => {
      fixture.detectChanges();

      expect(transportServiceSpy.getTransports).toHaveBeenCalledWith('', '', '');
      expect(component.transports).toEqual(mockTransports);
      expect(component.loading).toBeFalsy();
    });

    it('should handle plain array response when searching transports', () => {
      transportServiceSpy.getTransports.and.returnValue(
        of(mockTransports as any)
      );

      component.searchTransports();

      expect(component.transports).toEqual(mockTransports);
      expect(component.loading).toBeFalsy();
    });

    it('should handle error when searching transports fails', () => {
      spyOn(console, 'error').and.callFake(() => {});
      transportServiceSpy.getTransports.and.returnValue(
        throwError(() => new Error('Server Error'))
      );

      component.searchTransports();

      expect(component.loading).toBeFalsy();
      expect(component.popupTitle).toBe('Error');
      expect(component.popupMessage).toBe('Failed to load transports');
      expect(component.showPopup).toBeTruthy();
    });
  });

  describe('resetSearch', () => {
    it('should reset search form controls and re-trigger transport search', () => {
      component.source = 'New York';
      component.destination = 'Boston';
      component.status = 'AVAILABLE';

      component.resetSearch();

      expect(component.source).toBe('');
      expect(component.destination).toBe('');
      expect(component.status).toBe('');
      expect(transportServiceSpy.getTransports).toHaveBeenCalledWith('', '', '');
    });
  });

  describe('updateStatus', () => {
    const transportToUpdate = {
      transportId: 1,
      mode: 'BUS',
      source: 'New York',
      destination: 'Boston',
      transportStatus: 'AVAILABLE',
    };

    it('should successfully update transport status and display success popup', () => {
      transportServiceSpy.updateTransportStatus.and.returnValue(of({} as any));

      component.updateStatus(transportToUpdate, 'AVAILABLE');

      expect(transportServiceSpy.updateTransportStatus).toHaveBeenCalledWith(1, {
        status: 'AVAILABLE' as any,
      });
      expect(transportToUpdate.transportStatus).toBe('AVAILABLE');
      expect(component.popupTitle).toBe('Success');
      expect(component.popupMessage).toBe('Transport status updated successfully');
      expect(component.showPopup).toBeTruthy();
    });

    it('should handle update status error with server error message', () => {
      spyOn(console, 'error').and.callFake(() => {});
      const errorResponse = {
        error: { message: 'Custom API Error' },
      };

      transportServiceSpy.updateTransportStatus.and.returnValue(
        throwError(() => errorResponse)
      );

      component.updateStatus(transportToUpdate, 'INACTIVE');

      expect(component.popupTitle).toBe('Error');
      expect(component.popupMessage).toBe('Custom API Error');
      expect(component.showPopup).toBeTruthy();
    });

    it('should handle update status error with fallback message when error payload is empty', () => {
      spyOn(console, 'error').and.callFake(() => {});

      transportServiceSpy.updateTransportStatus.and.returnValue(
        throwError(() => new Error())
      );

      component.updateStatus(transportToUpdate, 'INACTIVE');

      expect(component.popupTitle).toBe('Error');
      expect(component.popupMessage).toBe('Failed to update transport status');
      expect(component.showPopup).toBeTruthy();
    });
  });

  describe('closePopup', () => {
    it('should set showPopup to false when closePopup is invoked', () => {
      component.showPopup = true;

      component.closePopup();

      expect(component.showPopup).toBeFalsy();
    });
  });
});