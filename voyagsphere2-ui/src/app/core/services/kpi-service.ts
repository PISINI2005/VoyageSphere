import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { KpiDto } from '../models/Kpidto';

@Injectable({
  providedIn: 'root'
})
export class KpiService {

  private baseUrl = 'http://localhost:8080/api/v1/kpi-reports';

  constructor(private http: HttpClient) { }

  getKpi(month?: number, year?: number): Observable<any> {

    let params = new HttpParams();

    if (month) {
      params = params.set('month', month);
    }

    if (year) {
      params = params.set('year', year);
    }

    return this.http.get(this.baseUrl, { params });
  }
}