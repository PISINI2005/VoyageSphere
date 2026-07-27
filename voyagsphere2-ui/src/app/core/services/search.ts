import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class SearchService {
  private readonly baseUrl = 'http://localhost:8080/api/v1/search';

  constructor(private http: HttpClient) {}

  // FIX: Return Observable<any> instead of Observable<any[]> since it now wraps page metadata
  search(
    type: string,
    filters: {
      source?: string;
      destination?: string;
      city?: string;
      min?: number;
      max?: number;
      ratings?: number;
      category?: string;

      // Flight & Transport
      date?: string;

      // Hotel
      checkInDate?: string;
      checkOutDate?: string;

      page?: number;
      size?: number;
    },
  ): Observable<any> {
    let params = new HttpParams().set('type', type);

    if (filters.source) params = params.set('source', filters.source);

    if (filters.destination) params = params.set('destination', filters.destination);

    if (filters.city) params = params.set('city', filters.city);

    if (filters.ratings != null) params = params.set('ratings', filters.ratings.toString());

    if (filters.category) params = params.set('category', filters.category);

    // Flight / Transport
    if (filters.date) params = params.set('date', filters.date);

    // Hotel
    if (filters.checkInDate) params = params.set('checkInDate', filters.checkInDate);

    if (filters.checkOutDate) params = params.set('checkOutDate', filters.checkOutDate);

    if (filters.min != null) params = params.set('min', filters.min.toString());

    if (filters.max != null) params = params.set('max', filters.max.toString());

    if (filters.page != null) params = params.set('page', filters.page.toString());

    if (filters.size != null) params = params.set('size', filters.size.toString());

    // FIX: Swapped generic extraction type array to any wrapper
    return this.http.get<any>(this.baseUrl, { params });
  }

  getItemById(
    id: number,
    type: string,
    date?: string,
    checkInDate?: string,
    checkOutDate?: string,
  ): Observable<any> {
    let params = new HttpParams();

    // Flight / Transport
    if (date) params = params.set('date', date);

    // Hotel
    if (checkInDate) params = params.set('checkInDate', checkInDate);

    if (checkOutDate) params = params.set('checkOutDate', checkOutDate);

    // Safe path formatting for plural endpoints (e.g., package -> packages)
    let dynamicSegment = type.toLowerCase();
    if (dynamicSegment === 'package') {
      dynamicSegment = 'packages';
    } else {
      dynamicSegment = `${dynamicSegment}s`;
    }

    return this.http.get<any>(`http://localhost:8080/api/v1/${dynamicSegment}/${id}`, { params });
  }

  getPriceCalendar(
    id: number,
    type: string,
    category: string,
    startDate?: string,
    endDate?: string,
  ): Observable<any[]> {
    let params = new HttpParams();
    if (type.toUpperCase() === 'FLIGHT') {
      params = params.set('seatType', category);
    } else if (type.toUpperCase() === 'TRANSPORT') {
      params = params.set('transportClass', category);
    }

    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);

    let dynamicSegment = type.toLowerCase();
    if (dynamicSegment === 'package') {
      dynamicSegment = 'packages';
    } else {
      dynamicSegment = `${dynamicSegment}s`;
    }

    return this.http.get<any[]>(`http://localhost:8080/api/v1/${dynamicSegment}/${id}/calendar`, {
      params,
    });
  }
}
